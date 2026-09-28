package dev.capital.domain

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private val Z = BigInteger.ZERO
private val O = BigInteger.ONE
private fun BigDecimal.units() = setScale(18, RoundingMode.DOWN).unscaledValue()
private fun BigInteger.money() = BigDecimal(this,18)

data class Allocation(val byConnection: Map<String, BigDecimal>, val capacities: Map<String, BigDecimal>, val incomplete: Boolean) {
    fun goal(id: String) = byConnection.filterKeys { it.substringAfter('/') == id }.values.fold(ZERO, BigDecimal::add)
    fun bucket(id: String) = byConnection.filterKeys { it.substringBefore('/') == id }.values.fold(ZERO, BigDecimal::add)
    val total: BigDecimal get() = byConnection.values.fold(ZERO, BigDecimal::add)
}
// Exact capped equal sharing; bounded by the number of caps, not the number of currency units.
private fun share(total: BigInteger, caps: Map<String,BigInteger>): Map<String,BigInteger> {
    val result = caps.mapValues { Z }.toMutableMap()
    var left = total.min(caps.values.fold(Z,BigInteger::add))
    while (left > Z) {
        val active = caps.keys.filter { result.getValue(it) < caps.getValue(it) }.sorted()
        if (active.isEmpty()) break
        val each = left / active.size.toBigInteger()
        var extra = left % active.size.toBigInteger()
        active.forEach { key ->
            val offer = each + if(extra > Z) { extra -= O; O } else Z
            val take = offer.min(caps.getValue(key)-result.getValue(key))
            result[key] = result.getValue(key) + take; left -= take
        }
    }
    return result
}
fun Portfolio.allocate(): Allocation {
    val bucketsUsd = buckets.associate { it.id to bucketValue(it.id,"USD").units() }
    val capacity = bucketsUsd.toMutableMap()
    val goalsUsd = goals.filterNot { it.archived }.mapNotNull { g -> convert(g.target.decimal(),g.currency,"USD")?.let { g.id to it.units() } }.toMap()
    val caps = connections.filter { it.goalId in goalsUsd }.associate { c ->
        val g = goals.first { it.id == c.goalId }
        val target = goalsUsd.getValue(g.id).money()
        val amount = when(c.mode) {
            Limit.AUTO -> target
            Limit.FIXED -> convert(c.value.decimal(),g.currency,"USD") ?: ZERO
            Limit.BUCKET_PERCENT -> bucketsUsd.getValue(c.bucketId).money() * c.value.decimal() / HUNDRED
            Limit.GOAL_PERCENT -> target * c.value.decimal() / HUNDRED
        }.min(target).min(c.goalCap?.let { target * it.decimal() / HUNDRED } ?: target)
        c.key to amount.units()
    }
    val flow = caps.mapValues { Z }.toMutableMap()
    fun funded(goal: String) = flow.filterKeys { it.substringAfter('/') == goal }.values.fold(Z,BigInteger::add)
    goals.filter { it.id in goalsUsd }.groupBy { it.priority }.toSortedMap(compareByDescending { it }).values.forEach { group ->
        val ids = group.map { it.id }.toSet()
        val groupKeys = caps.keys.filter { it.substringAfter('/') in ids }.sorted()
        while (true) {
            val offers = mutableMapOf<String,BigInteger>()
            capacity.keys.sorted().forEach { b ->
                val eligible = groupKeys.filter { it.substringBefore('/') == b && funded(it.substringAfter('/')) < goalsUsd.getValue(it.substringAfter('/')) }
                offers.putAll(share(capacity.getValue(b),eligible.associateWith { caps.getValue(it)-flow.getValue(it) }))
            }
            val accepted = mutableMapOf<String,BigInteger>()
            ids.sorted().forEach { g ->
                val o = offers.filterKeys { it.substringAfter('/') == g }
                val sum = o.values.fold(Z,BigInteger::add)
                val need = goalsUsd.getValue(g)-funded(g)
                if(sum <= need) accepted.putAll(o) else if(sum > Z) {
                    val a = o.mapValues { it.value * need / sum }.toMutableMap()
                    var rem = need - a.values.fold(Z,BigInteger::add)
                    a.keys.sorted().forEach { if(rem > Z && a.getValue(it) < o.getValue(it)) { a[it] = a.getValue(it)+O; rem -= O } }
                    accepted.putAll(a)
                }
            }
            if(accepted.values.all { it == Z }) break
            accepted.forEach { (key,v) -> flow[key] = flow.getValue(key)+v; val b = key.substringBefore('/'); capacity[b] = capacity.getValue(b)-v }
        }
        // Residual paths can reassign a peer's sources without decreasing its funded total.
        while(true) {
            var augmented = false
            val targets = ids.filter { funded(it) < goalsUsd.getValue(it) }.sortedWith(compareBy<String> { funded(it) }.thenBy { it })
            for(target in targets) {
                val parent = mutableMapOf<String,String>()
                val queue = ArrayDeque<String>()
                capacity.keys.sorted().filter { capacity.getValue(it) > Z }.forEach { val n="b:$it"; parent[n]="source"; queue.add(n) }
                val end="g:$target"
                while(queue.isNotEmpty() && end !in parent) {
                    val n=queue.removeFirst(); val id=n.substring(2)
                    val next=if(n.startsWith("b:")) groupKeys.filter { it.substringBefore('/')==id && flow.getValue(it)<caps.getValue(it) }.map { "g:${it.substringAfter('/')}" }
                    else groupKeys.filter { it.substringAfter('/')==id && flow.getValue(it)>Z }.map { "b:${it.substringBefore('/')}" }
                    next.sorted().filter { it !in parent }.forEach { parent[it]=n; queue.add(it) }
                }
                if(end !in parent) continue
                var node=end; var take=goalsUsd.getValue(target)-funded(target)
                val edges=mutableListOf<Pair<String,Boolean>>()
                while(parent.getValue(node)!="source") {
                    val prev=parent.getValue(node)
                    val forward=prev.startsWith("b:")
                    val key=if(forward) "${prev.substring(2)}/${node.substring(2)}" else "${node.substring(2)}/${prev.substring(2)}"
                    take=take.min(if(forward) caps.getValue(key)-flow.getValue(key) else flow.getValue(key))
                    edges += key to forward; node=prev
                }
                val start=node.substring(2); take=take.min(capacity.getValue(start))
                edges.forEach { (key,forward) -> flow[key]=flow.getValue(key)+(if(forward) take else -take) }
                capacity[start]=capacity.getValue(start)-take
                augmented=true; break
            }
            if(!augmented) break
        }
    }
    return Allocation(flow.mapValues { it.value.money() },bucketsUsd.mapValues { it.value.money() },incomplete("USD") || goals.any { !it.archived && it.id !in goalsUsd })
}
