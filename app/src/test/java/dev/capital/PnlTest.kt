package dev.capital

import dev.capital.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PnlTest {
    private val today: LocalDate = LocalDate.of(2026, 10, 22)
    private fun d(back: Long) = today.minusDays(back).toString()
    private fun qty(id: String, vararg points: Pair<String, String>) = Series("qty:$id", points.toMap())
    private fun rate(asset: String, vararg points: Pair<String, String>) = Series("rate:$asset", points.toMap())
    private fun portfolio(currency: String, holding: Holding, history: List<Series>, flows: List<Flow> = emptyList(), accounts: List<Account> = emptyList(), quotes: List<Quote> = emptyList()) =
        Portfolio(buckets = listOf(Bucket("b", "Invest", currency, pnl = true)), holdings = listOf(holding), history = history, flows = flows, accounts = accounts, quotes = quotes)
    private fun eq(expected: String, actual: BigDecimal?) { assertNotNull(actual); assertEquals(0, expected.toBigDecimal().compareTo(actual)) }
    private fun PnlReport.period(span: Span) = periods.first { it.span == span }

    @Test fun sameValueIsZeroPnl() {
        val r = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1000"), listOf(qty("h", d(21) to "1000"))).pnl("b", today)
        assertEquals("EUR", r.currency); assertEquals(today.minusDays(21), r.start); assertFalse(r.incomplete); assertEquals(22, r.points.size)
        r.points.forEach { eq("1000", it.value); eq("0", it.pnl) }
        eq("0", r.period(Span.ALL).pnl); eq("0", r.period(Span.ALL).twr); assertEquals(today.minusDays(21), r.period(Span.ALL).from)
        eq("0", r.period(Span.WEEK).pnl); eq("0", r.period(Span.DAY).twr)
    }
    @Test fun priceDoubles() {
        val r = portfolio("USD", Holding("h", "b", "Coin", "BTC", "1"), listOf(qty("h", d(21) to "1"), rate("BTC", d(21) to "1000", d(0) to "2000"))).pnl("b", today)
        assertFalse(r.incomplete); eq("1000", r.points[10].value); eq("2000", r.points.last().value); eq("1000", r.points.last().pnl)
        eq("1000", r.period(Span.ALL).pnl); eq("100.00", r.period(Span.ALL).twr); eq("1000", r.period(Span.DAY).pnl); eq("100.00", r.period(Span.DAY).twr)
    }
    @Test fun depositIsAFlowNotProfit() {
        val r = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1500"), listOf(qty("h", d(21) to "1000", d(16) to "1500")), listOf(Flow("h", d(16), "500", "EUR", "Manual", d(16)))).pnl("b", today)
        eq("1000", r.points[4].value); eq("1500", r.points[5].value); r.points.forEach { eq("0", it.pnl) }
        eq("0", r.period(Span.ALL).pnl); eq("0", r.period(Span.ALL).twr); eq("0", r.period(Span.WEEK).pnl)
    }
    @Test fun linkedAccountExcludesBrokerDeposit() {
        val account = Account("a", "Broker", "X", "1", asset = "EUR", quantity = "11200")
        val h = Holding("h", "b", "Broker", "EUR", "11200", accountId = "a")
        val r = portfolio("EUR", h, listOf(qty("h", d(30) to "10000", d(20) to "11000", d(0) to "11200")), listOf(Flow("h", d(20), "1000", "EUR", "X", "dep1")), accounts = listOf(account)).pnl("b", today)
        eq("200", r.period(Span.ALL).pnl); eq("1.82", r.period(Span.ALL).twr); eq("200", r.period(Span.MONTH).pnl); eq("200", r.points.last().pnl)
    }
    @Test fun periodsBeforeStartAreNull() {
        val r = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1000"), listOf(qty("h", d(5) to "1000"))).pnl("b", today)
        listOf(Span.YEAR, Span.MONTH, Span.WEEK).forEach { s -> val p = r.period(s); assertNull(p.pnl); assertNull(p.twr) }
        assertEquals(today.minusYears(1), r.period(Span.YEAR).from); assertEquals(today.minusDays(7), r.period(Span.WEEK).from)
        eq("0", r.period(Span.DAY).pnl); eq("0", r.period(Span.ALL).pnl)
        val empty = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1000"), emptyList()).pnl("b", today)
        assertTrue(empty.points.isEmpty()); assertNull(empty.start); assertEquals(5, empty.periods.size); empty.periods.forEach { assertNull(it.pnl) }
    }
    @Test fun twrWithMidPeriodFlow() {
        // 1000 -> 1100 (+10 %), deposit 1000 then 2310 (+10 % on 2100), then 2541 (+10 %): 1.1^3 - 1 = 33.1 %.
        val r = portfolio("USD", Holding("h", "b", "Cash", "USD", "2541"), listOf(qty("h", d(3) to "1000", d(2) to "1100", d(1) to "2310", d(0) to "2541")), listOf(Flow("h", d(1), "1000", "USD", "Manual", d(1)))).pnl("b", today)
        eq("541", r.period(Span.ALL).pnl); eq("33.10", r.period(Span.ALL).twr); eq("231", r.period(Span.DAY).pnl); eq("10.00", r.period(Span.DAY).twr)
    }
    @Test fun usdBucketConvertsEurHoldingWithTheDaysRate() {
        val r = portfolio("USD", Holding("h", "b", "Cash", "EUR", "1000"), listOf(qty("h", d(10) to "1000"), rate("EUR", d(10) to "1.0", d(0) to "1.2"))).pnl("b", today)
        assertFalse(r.incomplete); eq("1000", r.points[5].value); eq("1200", r.points.last().value); eq("200", r.period(Span.ALL).pnl); eq("20.00", r.period(Span.ALL).twr)
    }
    @Test fun missingRateMarksIncomplete() {
        val r = portfolio("USD", Holding("h", "b", "Coin", "BTC", "1"), listOf(qty("h", d(3) to "1"))).pnl("b", today)
        assertTrue(r.incomplete); r.points.forEach { eq("0", it.value) }
        val quoted = portfolio("USD", Holding("h", "b", "Coin", "BTC", "1"), listOf(qty("h", d(3) to "1")), quotes = listOf(Quote("BTC", "50000", "x", 1, 1))).pnl("b", today)
        assertFalse(quoted.incomplete); eq("50000", quoted.points.first().value)
        val gap = portfolio("USD", Holding("h", "b", "Coin", "BTC", "1"), listOf(qty("h", d(3) to "1"), rate("BTC", d(1) to "100"))).pnl("b", today)
        assertTrue(gap.incomplete); eq("0", gap.points[0].value); eq("100", gap.points[2].value)
    }
    @Test fun recordedPnlAppendsOnChangeAndMakesFlows() {
        val p0 = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1000"), emptyList())
        val p1 = p0.recordedPnl(today)
        assertEquals(listOf(Series("qty:h", mapOf(d(0) to "1000"))), p1.history); assertTrue(p1.flows.isEmpty())
        assertEquals(p1, p1.recordedPnl(today))
        val p2 = p1.copy(holdings = listOf(p1.holdings[0].copy(quantity = "1500"))).recordedPnl(today)
        assertEquals(mapOf(d(0) to "1500"), p2.history[0].points); assertEquals(listOf(Flow("h", d(0), "500", "EUR", "Manual", d(0))), p2.flows)
        val p3 = p2.copy(holdings = listOf(p2.holdings[0].copy(quantity = "1200"))).recordedPnl(today)
        assertEquals(mapOf(d(0) to "1200"), p3.history[0].points); assertEquals(listOf(Flow("h", d(0), "200", "EUR", "Manual", d(0))), p3.flows)
        val p4 = p3.copy(holdings = listOf(p3.holdings[0].copy(quantity = "1000"))).recordedPnl(today)
        assertTrue(p4.flows.isEmpty())
        val tomorrow = p3.copy(holdings = listOf(p3.holdings[0].copy(quantity = "1100"))).recordedPnl(today.plusDays(1))
        assertEquals(mapOf(d(0) to "1200", today.plusDays(1).toString() to "1100"), tomorrow.history[0].points)
        assertEquals(Flow("h", today.plusDays(1).toString(), "-100", "EUR", "Manual", today.plusDays(1).toString()), tomorrow.flows[1])
    }
    @Test fun recordedPnlWalletAndLinked() {
        val wallet = portfolio("EUR", Holding("w", "b", "Wallet", "BTC", "2", address = "bc1q"), listOf(qty("w", d(3) to "3"))).recordedPnl(today)
        assertEquals(listOf(Flow("w", d(0), "-1", "BTC", "Wallet", d(0))), wallet.flows); assertEquals("2", wallet.history[0].points.getValue(d(0)))
        val account = Account("a", "Broker", "X", "1", asset = "EUR", quantity = "150")
        val linked = portfolio("EUR", Holding("h", "b", "Broker", "EUR", "100", accountId = "a"), listOf(qty("h", d(3) to "100")), accounts = listOf(account)).recordedPnl(today)
        assertTrue(linked.flows.isEmpty()); assertEquals("150", linked.history[0].points.getValue(d(0)))
        val unknown = portfolio("EUR", Holding("w", "b", "Wallet", "BTC", null, address = "bc1q"), emptyList()).recordedPnl(today)
        assertTrue(unknown.history.isEmpty())
    }
    @Test fun pnlOffRecordsNothing() {
        val p = portfolio("EUR", Holding("h", "b", "Cash", "EUR", "1000"), emptyList())
        val off = p.copy(buckets = listOf(p.buckets[0].copy(pnl = false))).recordedPnl(today)
        assertTrue(off.history.isEmpty()); assertTrue(off.flows.isEmpty())
    }
}
