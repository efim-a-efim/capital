package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class FireTest {
    private val today: LocalDate = LocalDate.of(2026, 1, 1)
    private val zero = Fire(growth = "0", growthOptimistic = "0", growthPessimistic = "0", inflation = "0", inflationOptimistic = "0", inflationPessimistic = "0")
    private fun goal(fire: Fire?, currency: String = "USD") = Goal("g", "FIRE", "1000000", currency, "2030-01-01", fire = fire)
    private fun portfolio(fire: Fire?, cash: String = "0", rates: List<Rate> = emptyList()) = Portfolio(
        buckets = listOf(Bucket("b", "Savings", "USD")), holdings = listOf(Holding("h", "b", "Cash", "USD", cash)), goals = listOf(goal(fire)), connections = listOf(Connection("g", "b")), rates = rates,
    )
    private fun solve(fire: Fire, cash: String = "0") = (portfolio(fire, cash).fire(goal(fire), today) as FireOutcome.Ready).results.getValue(Scenario.AVERAGE)
    private fun missing(fire: Fire) = (portfolio(fire).fire(goal(fire), today) as FireOutcome.Missing).message
    private fun eq(expected: String, actual: BigDecimal) { assertEquals(0, expected.toBigDecimal().compareTo(actual)) }
    private fun rejects(block: () -> Unit) { assertThrows(Exception::class.java) { block() } }
    private fun rates(g: Int, i: Int, w: Int = 4) = Rates(BigDecimal(g), BigDecimal(i), BigDecimal(w))

    @Test fun defaultRates() {
        val p = Portfolio()
        assertEquals(rates(7, 3), p.fireRates(goal(Fire()), Scenario.AVERAGE))
        assertEquals(rates(10, 2), p.fireRates(goal(Fire()), Scenario.OPTIMISTIC))
        assertEquals(rates(4, 5), p.fireRates(goal(Fire()), Scenario.PESSIMISTIC))
    }
    @Test fun overrideWins() {
        val g = goal(Fire(growth = "5", inflationPessimistic = "9", withdrawal = "3.5"))
        assertEquals(Rates(BigDecimal(5), BigDecimal(3), "3.5".toBigDecimal()), Portfolio().fireRates(g, Scenario.AVERAGE))
        assertEquals(Rates(BigDecimal(10), BigDecimal(2), "3.5".toBigDecimal()), Portfolio().fireRates(g, Scenario.OPTIMISTIC))
        assertEquals(Rates(BigDecimal(4), BigDecimal(9), "3.5".toBigDecimal()), Portfolio().fireRates(g, Scenario.PESSIMISTIC))
    }
    @Test fun fetchedSeriesWinsOverDefault() {
        val inflation = (2000..2001).associate { "$it" to "9" } + (2002..2011).associate { "$it" to "2" } + (2012..2021).associate { "$it" to "4" }
        val growth = (2011..2020).associate { "$it" to "0" } + ("2021" to "100")
        val s = scenarioRates(inflation, "inflation")
        eq("3", s.getValue(Scenario.AVERAGE)); eq("2", s.getValue(Scenario.OPTIMISTIC)); eq("4", s.getValue(Scenario.PESSIMISTIC))
        val r = scenarioRates(growth, "growth")
        eq("6.50", r.getValue(Scenario.AVERAGE)); eq("7.18", r.getValue(Scenario.OPTIMISTIC)); eq("0", r.getValue(Scenario.PESSIMISTIC))
        eq("10", scenarioRates(mapOf("2020" to "10", "2021" to "10"), "growth").getValue(Scenario.PESSIMISTIC))
        val fetched = listOf(Rate("inflation", "USD", inflation, "x", 1, 1), Rate("growth", "USD", growth, "x", 1, 1), Rate("inflation", "EUR", inflation, "x", 1, 1, error = "down"))
        val p = Portfolio(rates = fetched)
        assertEquals(Rates("6.50".toBigDecimal(), BigDecimal(3).setScale(2), BigDecimal(4)), p.fireRates(goal(Fire()), Scenario.AVERAGE))
        assertEquals(Rates("0.00".toBigDecimal(), BigDecimal(4).setScale(2), BigDecimal(4)), p.fireRates(goal(Fire()), Scenario.PESSIMISTIC))
        eq("5", p.fireRates(goal(Fire(growth = "5")), Scenario.AVERAGE).growth)
        assertEquals(rates(7, 3), p.fireRates(goal(Fire(), "EUR"), Scenario.AVERAGE))
    }
    @Test fun derivesFireDate() {
        val r = solve(zero.copy(contribution = "1000", target = "120000"))
        assertEquals(today.plusYears(10), r.fireDate); eq("1000", r.contribution); eq("120000", r.targetNominal); eq("400", r.spend); eq("400", r.spendNominal)
        assertTrue(r.reachable); assertNull(r.shortfall); assertNull(r.ageAtFire)
        assertEquals(421, r.curve.size); eq("120000", r.curve[120].second); assertTrue(r.curve.last().second.signum() <= 0)
        assertEquals(today.plusYears(10), solve(zero.copy(contribution = "12000", period = Period.YEAR, target = "120000")).fireDate)
        val miss = solve(zero.copy(contribution = "100", target = "120000"))
        assertFalse(miss.reachable); assertEquals(today.plusYears(50), miss.fireDate); eq("60000", miss.shortfall!!); assertNull(miss.earliest); assertEquals(miss.fireDate, miss.lastsUntil); assertEquals(601, miss.curve.size)
    }
    @Test fun derivesContribution() {
        val r = solve(zero.copy(fireDate = today.plusYears(10).toString(), target = "120000"))
        eq("1000", r.contribution); assertEquals(today.plusYears(10), r.fireDate); assertTrue(r.reachable)
        eq("0", solve(zero.copy(fireDate = today.plusYears(10).toString(), target = "120000"), cash = "200000").contribution)
    }
    @Test fun derivesTargetFromSpend() {
        val r = solve(zero.copy(contribution = "1000", spend = "400"))
        eq("120000", r.target); eq("400", r.spend); assertEquals(today.plusYears(10), r.fireDate)
    }
    @Test fun derivesTargetFromDateAndContribution() {
        val r = solve(zero.copy(contribution = "1000", fireDate = today.plusYears(10).toString()))
        eq("120000", r.target); eq("400", r.spend); assertTrue(r.reachable)
    }
    @Test fun keepsGivenDateAndShowsShortfall() {
        val short = solve(zero.copy(contribution = "1000", fireDate = today.plusYears(5).toString(), target = "120000"))
        assertEquals(today.plusYears(5), short.fireDate); assertFalse(short.reachable); eq("60000", short.shortfall!!); assertEquals(today.plusYears(10), short.earliest)
        val ok = solve(zero.copy(contribution = "1000", fireDate = today.plusYears(12).toString(), target = "120000"))
        assertTrue(ok.reachable); eq("0", ok.shortfall!!); assertEquals(today.plusYears(10), ok.earliest)
    }
    @Test fun missingMessages() {
        assertEquals("Enter a target, a monthly spend, or both a FIRE date and a contribution", missing(Fire(contribution = "1")))
        assertEquals("Enter a target, a monthly spend, or both a FIRE date and a contribution", missing(Fire(fireDate = "2030-01-01")))
        assertEquals("Enter a FIRE date or a contribution", missing(Fire(target = "1")))
        assertEquals("The FIRE date is in the past", missing(Fire(target = "1", fireDate = "2025-12-31")))
        assertEquals("The birth date is in the future", missing(Fire(target = "1", contribution = "1", birth = "2026-01-02")))
        assertEquals("The withdrawal rate must be above zero", missing(Fire(spend = "1", contribution = "1", withdrawal = "0")))
    }
    @Test fun closedFormContributionMatchesSimulation() {
        val date = today.plusYears(10).toString()
        val c = solve(Fire(fireDate = date, target = "120000"), cash = "5000").contribution
        val r = solve(Fire(fireDate = date, contribution = c.text()), cash = "5000")
        assertTrue((r.target - BigDecimal(120000)).abs() < BigDecimal.ONE)
    }
    @Test fun neverEnds() {
        val r = solve(zero.copy(growth = "10", contribution = "1000", target = "120000"))
        assertTrue(r.neverEnds); assertNull(r.lastsUntil); assertNull(r.lastsYears); assertNull(r.ageAtEnd); assertFalse(r.beyondLife)
        assertTrue(r.fireDate < today.plusYears(10)); assertEquals(ChronoUnit.MONTHS.between(today, r.fireDate.plusYears(50)) + 1, r.curve.size.toLong())
    }
    @Test fun beyondLife() {
        val r = solve(zero.copy(birth = today.minusYears(30).toString(), contribution = "1000", target = "120000", spend = "100"))
        assertEquals(today.plusYears(10), r.fireDate); assertEquals(40, r.ageAtFire); assertFalse(r.neverEnds)
        assertEquals(today.plusYears(110), r.lastsUntil); assertEquals(100, r.lastsYears); assertTrue(r.beyondLife); assertEquals(65 * 12 + 1, r.curve.size)
    }
    @Test fun lastsUntilWithAge() {
        val r = solve(zero.copy(birth = today.minusYears(30).toString(), contribution = "1000", target = "120000", spend = "1000"))
        assertEquals(40, r.ageAtFire); assertEquals(today.plusYears(20), r.lastsUntil); assertEquals(50, r.ageAtEnd); assertEquals(10, r.lastsYears)
        assertFalse(r.beyondLife); assertFalse(r.neverEnds); assertEquals(241, r.curve.size)
    }
    @Test fun solvedFireWritesDerivedTargetAndDue() {
        val derived = portfolio(zero.copy(contribution = "1000", spend = "400")).solvedFire(today).goals.single()
        assertEquals("120000", derived.target); assertEquals(today.plusYears(10).toString(), derived.due)
        val given = portfolio(zero.copy(contribution = "1000", target = "500", fireDate = "2040-01-01")).solvedFire(today).goals.single()
        assertEquals("1000000", given.target); assertEquals("2030-01-01", given.due)
        val missing = portfolio(Fire(contribution = "1000")).solvedFire(today).goals.single()
        assertEquals("1000000", missing.target); assertEquals("2030-01-01", missing.due)
        assertEquals("1000000", portfolio(null).solvedFire(today).goals.single().target)
    }
    @Test fun schemaSevenRoundTripAndValidation() {
        assertEquals(7, SCHEMA)
        val p = portfolio(Fire(birth = "1990-05-05", fireDate = "2040-01-01", contribution = "250", period = Period.DAYS, days = 10, spend = "1500", target = "450000", growth = "6", withdrawal = "3.5", life = 90)).validate()
        assertEquals(p, decodeRevision(encodeRevision(Revision(data = p))).data)
        rejects { portfolio(Fire(period = Period.DAYS)).validate() }
        rejects { portfolio(Fire(withdrawal = "101")).validate() }
    }
}
