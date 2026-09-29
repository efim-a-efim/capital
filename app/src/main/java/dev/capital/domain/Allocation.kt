package dev.capital.domain

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.LocalDate

const val PLANNED_BUCKET = "~planned"

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
fun Portfolio.allocate(extra: BigDecimal = ZERO): Allocation {
    val bucketsUsd0 = buckets.associate { it.id to bucketValue(it.id,"USD").units() }
    val bucketsUsd = if(extra.signum() > 0) bucketsUsd0 + (PLANNED_BUCKET to extra.units()) else bucketsUsd0
    val capacity = bucketsUsd.toMutableMap()
    val goalsUsd = goals.filterNot { it.archived }.mapNotNull { g -> convert(g.target.decimal(),g.currency,"USD")?.let { g.id to it.units() } }.toMap()
    val caps = (connections.filter { it.goalId in goalsUsd }.associate { c ->
        val g = goals.first { it.id == c.goalId }
        val target = goalsUsd.getValue(g.id).money()
        val amount = when(c.mode) {
            Limit.AUTO -> target
            Limit.FIXED -> convert(c.value.decimal(),g.currency,"USD") ?: ZERO
            Limit.BUCKET_PERCENT -> bucketsUsd.getValue(c.bucketId).money() * c.value.decimal() / HUNDRED
            Limit.GOAL_PERCENT -> target * c.value.decimal() / HUNDRED
        }.min(target).min(c.goalCap?.let { target * it.decimal() / HUNDRED } ?: target)
        c.key to amount.units()
    } + if(extra.signum() > 0) goalsUsd.map { (g,t) -> "$PLANNED_BUCKET/$g" to t } else emptyList())
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

data class Contribution(val plannedId: String, val usd: BigDecimal)
data class Projection(
    val now: Map<String, BigDecimal>, val contributions: Map<String, List<Contribution>>,
    val closes: Map<String, String>, val reached: Map<String, String>,
    val final: Map<String, BigDecimal>, val targets: Map<String, BigDecimal>, val incomplete: Boolean,
)
// ponytail: one full allocation per planned saving; incremental residual flow if lists reach hundreds.
fun Portfolio.project(today: LocalDate = LocalDate.now()): Projection {
    val base = allocate()
    val targets = goals.filterNot { it.archived }.mapNotNull { g -> convert(g.target.decimal(), g.currency, "USD")?.let { g.id to it.units().money() } }.toMap()
    val now = targets.keys.associateWith { base.goal(it) }
    val prev = now.toMutableMap(); val done = targets.keys.filter { now.getValue(it) >= targets.getValue(it) }.toMutableSet()
    val contributions = mutableMapOf<String, MutableList<Contribution>>(); val closes = mutableMapOf<String, String>(); val reached = mutableMapOf<String, String>()
    var incomplete = base.incomplete; var cumulative = ZERO
    planned.withIndex().filterNot { it.value.archived(today) }.sortedWith(compareBy<IndexedValue<Planned>> { it.value.date }.thenBy { it.index }).map { it.value }.forEach { s ->
        val usd = convert(s.amount.decimal(), s.currency, "USD") ?: run { incomplete = true; return@forEach }
        cumulative += usd
        val a = allocate(cumulative)
        targets.forEach { (g, target) ->
            val funded = a.goal(g)
            // ponytail: totals are assumed non-decreasing as funds grow (checked by a test); a negative delta is clamped to zero.
            val delta = funded - prev.getValue(g)
            if (delta.signum() > 0) { contributions.getOrPut(g) { mutableListOf() }.add(Contribution(s.id, delta)); reached[g] = s.date }
            if (funded >= target && done.add(g)) closes[g] = s.date
            prev[g] = funded.max(prev.getValue(g))
        }
    }
    return Projection(now, contributions, closes, reached, prev, targets, incomplete)
}
