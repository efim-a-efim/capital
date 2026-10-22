package dev.capital.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

enum class Scenario { AVERAGE, OPTIMISTIC, PESSIMISTIC }
/** Annual percents. */
data class Rates(val growth: BigDecimal, val inflation: BigDecimal, val withdrawal: BigDecimal)
data class FireResult(
    val fireDate: LocalDate, val ageAtFire: Int?,
    val contribution: BigDecimal,
    val target: BigDecimal, val targetNominal: BigDecimal,
    val spend: BigDecimal, val spendNominal: BigDecimal,
    val lastsUntil: LocalDate?, val ageAtEnd: Int?,
    val lastsYears: Int?,
    val beyondLife: Boolean, val neverEnds: Boolean,
    val reachable: Boolean,
    val shortfall: BigDecimal?, val earliest: LocalDate?,
    val curve: List<Pair<LocalDate, BigDecimal>>,
    /** Monthly spend in today's money that empties the goal's current funds at life expectancy; zero before the FIRE date. */
    val spendNow: BigDecimal = BigDecimal.ZERO,
)
sealed class FireOutcome { data class Ready(val results: Map<Scenario, FireResult>) : FireOutcome(); data class Missing(val message: String) : FireOutcome() }

/** Built-in defaults (UBS Global Investment Returns Yearbook 2026; Bengen 1994 / Trinity 1998), annual percents. */
val FIRE_GROWTH = mapOf(Scenario.AVERAGE to 7, Scenario.OPTIMISTIC to 10, Scenario.PESSIMISTIC to 4)
val FIRE_INFLATION = mapOf(Scenario.AVERAGE to 3, Scenario.OPTIMISTIC to 2, Scenario.PESSIMISTIC to 5)
const val FIRE_WITHDRAWAL = 4
const val FIRE_LIFE = 95
const val FIRE_WORK = 65
private const val MONTH_DAYS = 30.4375
private const val MAX_DRAWDOWN = 2400
private fun Double.money(): BigDecimal = BigDecimal(this).setScale(2, RoundingMode.HALF_UP)

/** Rates for one scenario: the goal's Advanced override, else the fetched series in `Portfolio.rates` for the goal currency, else the built-in default. */
fun Portfolio.fireRates(goal: Goal, scenario: Scenario): Rates {
    val f = goal.fire ?: Fire()
    fun pick(override: String?, kind: String, default: Int) = override?.decimal()
        ?: rates.find { it.kind == kind && it.currency == goal.currency && it.values.isNotEmpty() && it.error == null }?.let { scenarioRates(it.values, kind)[scenario] }
        ?: BigDecimal(default)
    val (g, i) = when (scenario) { Scenario.AVERAGE -> f.growth to f.inflation; Scenario.OPTIMISTIC -> f.growthOptimistic to f.inflationOptimistic; Scenario.PESSIMISTIC -> f.growthPessimistic to f.inflationPessimistic }
    return Rates(pick(g, "growth", FIRE_GROWTH.getValue(scenario)), pick(i, "inflation", FIRE_INFLATION.getValue(scenario)), f.withdrawal?.decimal() ?: BigDecimal(FIRE_WITHDRAWAL))
}
/** Average, optimistic and pessimistic rates derived from a fetched series (year -> percent). */
fun scenarioRates(values: Map<String, String>, kind: String): Map<Scenario, BigDecimal> {
    val series = values.toSortedMap().values.map { it.toDouble() }.ifEmpty { return emptyMap() }
    if (kind == "inflation") {
        val w = series.takeLast(20)
        val rolling = if (w.size >= 5) w.windowed(5).map { it.average() } else listOf(w.average())
        return mapOf(Scenario.AVERAGE to w.average().money(), Scenario.OPTIMISTIC to rolling.min().money(), Scenario.PESSIMISTIC to rolling.max().money())
    }
    val w = series.takeLast(30)
    fun cagr(xs: List<Double>) = (xs.fold(1.0) { p, r -> p * (1 + r / 100) }.pow(1.0 / xs.size) - 1) * 100
    val rolling = if (w.size >= 10) w.windowed(10).map(::cagr) else listOf(cagr(w))
    return mapOf(Scenario.AVERAGE to cagr(w).money(), Scenario.OPTIMISTIC to rolling.max().money(), Scenario.PESSIMISTIC to rolling.min().money())
}

private fun accumulate(c0: Double, c: Double, g: Double, inf: Double, months: Int) = DoubleArray(months + 1).also { v -> v[0] = c0; for (k in 0 until months) v[k + 1] = v[k] * g + c * inf.pow(k / 12.0) }

fun Portfolio.fire(goal: Goal, today: LocalDate = LocalDate.now()): FireOutcome {
    val f = goal.fire ?: Fire()
    val birth = f.birth?.let(LocalDate::parse); val fireDate = f.fireDate?.let(LocalDate::parse)
    if (f.target == null && f.spend == null && (fireDate == null || f.contribution == null)) return FireOutcome.Missing(tr("Enter a target, a monthly spend, or both a FIRE date and a contribution"))
    if (fireDate == null && f.contribution == null) return FireOutcome.Missing(tr("Enter a FIRE date or a contribution"))
    if (birth != null && birth > today) return FireOutcome.Missing(tr("The birth date is in the future"))
    if (f.target == null && f.spend != null && (f.withdrawal?.decimal()?.signum() ?: 1) == 0) return FireOutcome.Missing(tr("The withdrawal rate must be above zero"))
    val c0 = (convert(allocate().goal(goal.id), "USD", goal.currency) ?: ZERO).toDouble()
    val monthly = f.contribution?.decimal()?.toDouble()?.times(when (f.period) { Period.WEEK -> MONTH_DAYS / 7; Period.TWO_WEEKS -> MONTH_DAYS / 14; Period.MONTH -> 1.0; Period.YEAR -> 1 / 12.0; Period.DAYS -> MONTH_DAYS / (f.days ?: 30) })
    val life = (f.life ?: FIRE_LIFE).toLong(); val work = (f.work ?: FIRE_WORK).toLong()
    return FireOutcome.Ready(Scenario.entries.associateWith { s -> solve(f, fireRates(goal, s), c0, monthly, birth, fireDate, life, work, today) })
}

// ponytail: whole months from today; a FIRE date inside the current month counts as today.
private fun solve(f: Fire, rates: Rates, c0: Double, monthly: Double?, birth: LocalDate?, fireDate: LocalDate?, life: Long, work: Long, today: LocalDate): FireResult {
    val g = (1 + rates.growth.toDouble() / 100).pow(1 / 12.0); val inf = 1 + rates.inflation.toDouble() / 100; val w = rates.withdrawal.toDouble() / 100
    val raise = f.contributionGrowth?.decimal()?.toDouble()?.let { 1 + it / 100 } ?: inf
    var target = f.target?.decimal()?.toDouble() ?: f.spend?.decimal()?.toDouble()?.let { it * 12 / w }
    var spend = f.spend?.decimal()?.toDouble() ?: target?.let { it * w / 12 }
    fun months(d: LocalDate) = ChronoUnit.MONTHS.between(today, d).toInt().coerceAtLeast(0)
    fun deflate(v: Double, t: Int) = v / inf.pow(t / 12.0)
    fun nominal(t: Int) = target!! * inf.pow(t / 12.0)
    fun age(d: LocalDate) = birth?.let { ChronoUnit.YEARS.between(it, d).toInt() }
    var n = fireDate?.let(::months)
    val span = maxOf(n ?: 0, months(birth?.plusYears(life) ?: (fireDate ?: today).plusYears(50)))
    var acc = accumulate(c0, monthly ?: 0.0, g, raise, span)
    var contribution = monthly
    var reachable = true; var draw = true; var shortfall: Double? = null; var earliest: LocalDate? = null
    when {
        n == null -> {
            // The earliest month from which the spend lasts to the horizon (life expectancy, or 50 years without a birth date) and, when the target was given, the target is reached.
            val given = f.target != null
            fun survives(k: Int): Boolean { val end = birth?.let { months(it.plusYears(life)) } ?: (k + 600); var v = acc[k]; for (t in k until end) { v = v * g - spend!! * inf.pow(t / 12.0); if (v < 0) return false }; return true }
            // Not later than the age until which the user can work (birth + work), else the whole horizon.
            val last = birth?.let { minOf(months(it.plusYears(work)), span) } ?: span
            n = (0..last).firstOrNull { (!given || acc[it] >= nominal(it)) && survives(it) } ?: run { draw = false; reachable = false; shortfall = if (given) deflate(nominal(last) - acc[last], last) else null; last }
            if (!given && reachable) target = deflate(acc[n!!], n!!)
        }
        contribution == null -> {
            val denom = (0 until n).sumOf { k -> g.pow(n - 1 - k) * raise.pow(k / 12.0) }
            contribution = if (denom > 0) ((nominal(n) - c0 * g.pow(n)) / denom).coerceAtLeast(0.0) else 0.0
            acc = accumulate(c0, contribution, g, raise, span)
        }
        target == null -> { target = deflate(acc[n], n); spend = target * w / 12 }
        else -> {
            shortfall = deflate((nominal(n) - acc[n]).coerceAtLeast(0.0), n); reachable = shortfall.money().signum() == 0
            earliest = (0..span).firstOrNull { acc[it] >= nominal(it) }?.let { today.plusMonths(it.toLong()) }
        }
    }
    val t = target!!; val s = spend!!
    // A FIRE date in the past means the spend period has started: the plan runs from today with the current funds.
    val fire = if (fireDate != null && fireDate < today) fireDate else today.plusMonths(n.toLong())
    val horizon = if (!draw) fire else birth?.plusYears(life) ?: fire.plusYears(50)
    val path = acc.take(n + 1).toMutableList()
    var exhausted: Int? = null
    if (draw) { var v = acc[n]; for (k in n until n + MAX_DRAWDOWN) { v = v * g - s * inf.pow(k / 12.0); path += v; if (v <= 0) { exhausted = k + 1; break } } }
    val neverEnds = draw && exhausted == null && deflate(path.last(), path.lastIndex) >= deflate(acc[n], n)
    // ponytail: money that outlives the 200-year simulation but shrinks in real terms is reported as lasting 200 years.
    val lastsUntil = if (neverEnds) null else today.plusMonths((if (!draw) n else exhausted ?: path.lastIndex).toLong())
    // Spend period: the current funds (live market value) paid out in equal real amounts until life expectancy (50 years from FIRE without a birth date), at least over one year.
    val left = months(birth?.plusYears(life) ?: fire.plusYears(50)).coerceAtLeast(12)
    val spendNow = if (n > 0 || c0 <= 0) 0.0 else c0 / (0 until left).sumOf { k -> inf.pow(k / 12.0) / g.pow(k + 1) }
    val curveEnd = minOf(exhausted ?: Int.MAX_VALUE, maxOf(months(horizon), n), path.lastIndex, MAX_DRAWDOWN - 1)
    return FireResult(
        fireDate = fire, ageAtFire = age(fire), contribution = (contribution ?: 0.0).money(),
        target = t.money(), targetNominal = (t * inf.pow(n / 12.0)).money(), spend = s.money(), spendNominal = (s * inf.pow(n / 12.0)).money(),
        lastsUntil = lastsUntil, ageAtEnd = lastsUntil?.let(::age), lastsYears = lastsUntil?.let { ChronoUnit.YEARS.between(fire, it).toInt() },
        beyondLife = lastsUntil != null && lastsUntil >= horizon, neverEnds = neverEnds, reachable = reachable,
        shortfall = shortfall?.money(), earliest = earliest,
        curve = (0..curveEnd).map { today.plusMonths(it.toLong()) to deflate(path[it], it).money() },
        spendNow = spendNow.money(),
    )
}

/** Rewrites `target` and `due` of FIRE goals whose `Fire.target` or `Fire.fireDate` is blank with the average-scenario result. */
fun Portfolio.solvedFire(today: LocalDate = LocalDate.now()): Portfolio = copy(goals = goals.map { g ->
    val f = g.fire ?: return@map g
    if (f.target != null && f.fireDate != null) return@map g
    // ponytail: the start capital is the previous allocation, so the fixed point is reached on the next edit.
    val r = (fire(g, today) as? FireOutcome.Ready)?.results?.getValue(Scenario.AVERAGE) ?: return@map g
    g.copy(target = if (f.target == null && r.target.signum() > 0) r.target.text() else g.target, due = if (f.fireDate == null) r.fireDate.toString() else g.due)
})
