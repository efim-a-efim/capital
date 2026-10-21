package dev.capital.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.TreeMap

enum class Span { ALL, YEAR, MONTH, WEEK, DAY }
/** One day of a bucket: its value and the cumulative PnL since the first day, both in the bucket currency. */
data class PnlPoint(val date: LocalDate, val value: BigDecimal, val pnl: BigDecimal)
/** PnL of one period ending today: `from` is the period start (the first day for ALL); `pnl` and `twr` are null when the data starts after `from` and the span is not ALL. `twr` is a percent. */
data class PnlPeriod(val span: Span, val from: LocalDate, val pnl: BigDecimal?, val twr: BigDecimal?)
data class PnlReport(val currency: String, val points: List<PnlPoint>, val periods: List<PnlPeriod>, val start: LocalDate?, val incomplete: Boolean)

private const val MAX_DAYS = 4000L
private fun BigDecimal.money(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
private fun Series.sorted() = TreeMap(points.entries.associate { (d, v) -> LocalDate.parse(d) to v.toBigDecimal() })
private fun Span.from(today: LocalDate, start: LocalDate) = when (this) { Span.ALL -> start; Span.YEAR -> today.minusYears(1); Span.MONTH -> today.minusMonths(1); Span.WEEK -> today.minusDays(7); Span.DAY -> today.minusDays(1) }

/** The bucket's daily values, flows and PnL from its first recorded day to `today`; empty points when nothing was recorded. */
fun Portfolio.pnl(bucketId: String, today: LocalDate = LocalDate.now()): PnlReport {
    val bucket = buckets.find { it.id == bucketId }
    val mine = holdings.filter { it.bucketId == bucketId }
    val series = history.associate { it.key to it.sorted() }
    val qty = mine.associate { it.id to series["qty:${it.id}"] }
    val byDay = flows.filter { f -> mine.any { it.id == f.holdingId } }.groupBy { LocalDate.parse(it.date) }
    val first = (qty.values.mapNotNull { it?.firstKey() } + byDay.keys).filter { it <= today }.minOrNull()
    if (bucket == null || first == null) return PnlReport(bucket?.currency ?: settings.currency, emptyList(), Span.entries.map { PnlPeriod(it, it.from(today, today), null, null) }, null, false)
    // ponytail: 4000 days (~11 years) per report, older days are cut; aggregate old years to lift it.
    val start = maxOf(first, today.minusDays(MAX_DAYS - 1))
    var incomplete = false
    fun rate(asset: String, day: LocalDate): BigDecimal? = if (asset == "USD") BigDecimal.ONE else series["rate:$asset"]?.floorEntry(day)?.value ?: price(asset)
    fun factor(asset: String, day: LocalDate): BigDecimal? = if (asset == bucket.currency) BigDecimal.ONE else rate(asset, day)?.let { a -> rate(bucket.currency, day)?.let { b -> a.divideMoney(b) } }
    val last = mutableMapOf<String, BigDecimal>()
    fun line(key: String, q: BigDecimal, asset: String, day: LocalDate): BigDecimal {
        val f = factor(asset, day) ?: run { incomplete = true; return last[key] ?: ZERO }
        return (q * f).also { last[key] = it }
    }
    fun value(day: LocalDate): BigDecimal = mine.fold(ZERO) { sum, h ->
        val q = qty[h.id]?.floorEntry(day)?.value ?: return@fold sum
        // ponytail: token quantity history is out of scope; today's token quantities are valued at the day's token rate.
        sum + line(h.id, q, h.asset, day) + h.tokens.filter { known(h, it) }.fold(ZERO) { s, t ->
            val a = tokenAsset(h.asset, t.contract)
            if ("rate:$a" !in series) incomplete = true
            s + line("${h.id}:${t.contract}", t.quantity()!!, a, day)
        }
    }
    fun flow(day: LocalDate): BigDecimal = byDay[day].orEmpty().fold(ZERO) { s, f -> s + (factor(f.currency, day)?.let { f.amount.toBigDecimal() * it } ?: run { incomplete = true; ZERO }) }
    val days = (0..ChronoUnit.DAYS.between(start, today)).map { start.plusDays(it) }
    val values = days.map(::value); val flowed = days.map(::flow)
    fun period(span: Span): PnlPeriod {
        val from = span.from(today, start)
        if (from < start && span != Span.ALL) return PnlPeriod(span, from, null, null)
        val a = ChronoUnit.DAYS.between(start, from).toInt().coerceAtLeast(0)
        val pnl = values.last() - values[a] - flowed.subList(a + 1, days.size).fold(ZERO, BigDecimal::add)
        val twr = (a + 1 until days.size).fold(BigDecimal.ONE) { p, d -> val base = values[d - 1] + flowed[d]; if (base.signum() > 0) p * values[d].divideMoney(base) else p }
        return PnlPeriod(span, from, pnl.money(), ((twr - BigDecimal.ONE) * HUNDRED).money())
    }
    var paid = ZERO
    val points = days.indices.map { i -> if (i > 0) paid += flowed[i]; PnlPoint(days[i], values[i].money(), (values[i] - values[0] - paid).money()) }
    return PnlReport(bucket.currency, points, Span.entries.map(::period), start, incomplete)
}

/** Records today's quantities of the holdings of PnL buckets (a `qty:` point when it differs from the last point) and turns quantity changes of manual and wallet holdings into flows at today's rate (source "Manual" or "Wallet", ref = the date). Linked-account holdings get a `qty:` point from the account value but no flow (their flows come from the broker). */
fun Portfolio.recordedPnl(today: LocalDate = LocalDate.now()): Portfolio {
    val day = today.toString()
    val on = buckets.filter { it.pnl }.map { it.id }.toSet()
    var series = history; var all = flows
    holdings.filter { it.bucketId in on }.forEach { h ->
        val q = (if (h.accountId != null) accounts.find { it.id == h.accountId }?.quantity else h.quantity)?.decimal() ?: return@forEach
        val key = "qty:${h.id}"
        val s = series.find { it.key == key }
        val prev = s?.points?.maxByOrNull { it.key }?.value?.toBigDecimal()
        if (prev != null && prev.compareTo(q) == 0) return@forEach
        series = if (s == null) series + Series(key, mapOf(day to q.text())) else series.map { if (it.key == key) it.copy(points = it.points + (day to q.text())) else it }
        if (prev == null || h.accountId != null) return@forEach
        // ponytail: token balance changes of a wallet are not flows; they need a token quantity history first.
        val source = if (h.address != null) "Wallet" else "Manual"
        val old = all.find { it.holdingId == h.id && it.source == source && it.ref == day }
        val amount = (old?.amount?.toBigDecimal() ?: ZERO) + (q - prev)
        all = all.filterNot { it === old } + listOfNotNull(Flow(h.id, day, amount.text(), h.asset, source, day).takeIf { amount.signum() != 0 })
    }
    return copy(history = series, flows = all)
}
