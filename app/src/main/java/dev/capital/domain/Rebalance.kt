package dev.capital.domain

import java.math.BigDecimal
import java.math.RoundingMode

data class Weight(val asset: String, val value: BigDecimal, val real: BigDecimal, val target: BigDecimal)
/** `missing` holds asset ids that cannot be valued and `flagged` target tokens that are unknown or excluded; when either is not empty `rows` is empty. */
data class Weights(val rows: List<Weight>, val total: BigDecimal, val missing: List<String>, val flagged: List<String>)
data class Trade(val asset: String, val amount: BigDecimal, val quantity: BigDecimal?, val resultValue: BigDecimal, val resultPercent: BigDecimal, val target: BigDecimal)
data class Plan(val trades: List<Trade>, val total: BigDecimal, val unavailable: String?)

private fun BigDecimal.percentOf(total: BigDecimal): BigDecimal = if (total.signum() == 0) ZERO.setScale(2) else multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP)

fun Portfolio.weights(bucketId: String): Weights {
    val base = settings.currency
    val targets = buckets.find { it.id == bucketId }?.targets.orEmpty().mapValues { it.value.decimal() }
    val qty = mutableMapOf<String, BigDecimal>(); val missing = mutableSetOf<String>(); val flagged = mutableSetOf<String>()
    fun add(asset: String, q: BigDecimal) { qty[asset] = (qty[asset] ?: ZERO) + q }
    holdings.filter { it.bucketId == bucketId }.forEach { h ->
        if (h.quantity == null) missing.add(h.asset) else add(h.asset, h.quantity.decimal())
        h.tokens.filter { known(h, it) }.forEach { add(tokenAsset(h.asset, it.contract), it.quantity()!!) }
    }
    val values = mutableMapOf<String, BigDecimal>()
    qty.forEach { (a, q) -> val v = convert(q, a, base); if (v == null) missing.add(a) else values[a] = v }
    targets.keys.filter { it !in qty }.forEach { a ->
        if (price(a) != null || a == base) values[a] = ZERO else if (':' in a) flagged.add(a) else missing.add(a)
    }
    if (missing.isNotEmpty() || flagged.isNotEmpty()) return Weights(emptyList(), ZERO, missing.sorted(), flagged.sorted())
    val total = values.values.fold(ZERO, BigDecimal::add)
    val rows = values.map { (a, v) -> Weight(a, v, v.percentOf(total), targets[a] ?: ZERO) }
        .sortedWith(compareByDescending<Weight> { it.target }.thenByDescending { it.value }.thenBy { it.asset })
    return Weights(rows, total, emptyList(), emptyList())
}

/** Exact water-filling on 18-decimal amounts; the rounding remainder goes to the largest deficit so amounts sum to `amount` exactly. */
fun Portfolio.rebalance(bucketId: String, amount: BigDecimal): Plan {
    require(amount.signum() >= 0) { tr("Amount must not be negative") }
    fun unavailable(reason: String) = Plan(emptyList(), ZERO, reason)
    val bucket = buckets.find { it.id == bucketId }
    if (bucket?.portfolio != true) return unavailable(tr("Portfolio mode is off"))
    val w = weights(bucketId)
    if (w.rows.isEmpty()) return unavailable(if (w.missing.isEmpty() && w.flagged.isEmpty()) tr("No targets") else tr("No value for {0}", (w.missing + w.flagged).joinToString()))
    val base = settings.currency
    val total = w.total + amount
    val deficit = w.rows.map { it.target.divideMoney(HUNDRED) * total - it.value }
    val order = deficit.indices.sortedWith(compareByDescending<Int> { deficit[it] }.thenBy { w.rows[it].asset })
    val buys = MutableList(deficit.size) { ZERO }
    if (bucket.allowSells) deficit.forEachIndexed { i, d -> buys[i] = d.setScale(18, RoundingMode.DOWN) }
    else if (amount.signum() > 0) {
        // Largest k whose k-th deficit is above the water level (sum of top k deficits - amount) / k; tested without division.
        var sum = ZERO; var k = 0; var kSum = ZERO
        order.forEachIndexed { j, i -> sum += deficit[i]; if (deficit[i] * (j + 1).toBigDecimal() > sum - amount) { k = j + 1; kSum = sum } }
        // Rounded down, so no buy goes negative and the sum never exceeds the amount.
        for (j in 0 until k) order[j].let { buys[it] = (deficit[it] * k.toBigDecimal() - kSum + amount).divide(k.toBigDecimal(), 18, RoundingMode.DOWN) }
    }
    buys[order[0]] += amount - buys.fold(ZERO, BigDecimal::add)
    val trades = w.rows.mapIndexed { i, r ->
        val a = buys[i]
        val q = convert(a.abs(), base, r.asset)?.let { if (a.signum() < 0) it.negate() else it }
        (r.value + a).let { Trade(r.asset, a, q, it, it.percentOf(total), r.target) }
    }
    return Plan(trades, total, null)
}
