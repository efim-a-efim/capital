package dev.capital

import dev.capital.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class RebalanceTest {
    private val codes=listOf("USD","EUR","GBP","CHF","JPY","CAD")
    // Every fiat quote is 1 USD, so values equal quantities in the USD base currency.
    private fun pf(values: Map<String,String>,targets: Map<String,String>,sells: Boolean=false)=Portfolio(
        buckets=listOf(Bucket("b","B","USD",true,targets,sells)),holdings=values.map { (a,v) -> Holding("h$a","b",a,a,v) },
        quotes=codes.filter { it!="USD" }.map { Quote(it,"1","x",1000,1000) },settings=Settings("USD"),
    ).validate()
    private fun eq(expected: String,actual: BigDecimal?) { assertEquals("$expected vs $actual",0,expected.toBigDecimal().compareTo(actual)) }
    private fun rejects(block: ()->Unit) { assertThrows(Exception::class.java) { block() } }
    private fun Plan.buy(asset: String)=trades.first { it.asset==asset }
    @Test fun newMoneyFixesDrift() {
        val plan=pf(mapOf("USD" to "7000","EUR" to "3000"),mapOf("USD" to "60","EUR" to "40")).rebalance("b","2000".toBigDecimal())
        eq("200",plan.buy("USD").amount); eq("1800",plan.buy("EUR").amount); eq("60",plan.buy("USD").resultPercent); eq("40",plan.buy("EUR").resultPercent); eq("14000",plan.total.add(BigDecimal.valueOf(2000)))
        assertNull(plan.unavailable)
    }
    @Test fun notEnoughMoneyWithoutAndWithSells() {
        val p=pf(mapOf("USD" to "9000","EUR" to "1000"),mapOf("USD" to "50","EUR" to "50"))
        val buys=p.rebalance("b","1000".toBigDecimal())
        eq("0",buys.buy("USD").amount); eq("1000",buys.buy("EUR").amount); assertTrue(buys.buy("USD").resultPercent>buys.buy("USD").target)
        val sells=pf(mapOf("USD" to "9000","EUR" to "1000"),mapOf("USD" to "50","EUR" to "50"),true).rebalance("b","1000".toBigDecimal())
        eq("-3500",sells.buy("USD").amount); eq("4500",sells.buy("EUR").amount); eq("50",sells.buy("USD").resultPercent)
    }
    @Test fun zeroAmountAndSingleAndUnheldAndUntargeted() {
        val zero=pf(mapOf("USD" to "9000","EUR" to "1000"),mapOf("USD" to "50","EUR" to "50")).rebalance("b",BigDecimal.ZERO)
        zero.trades.forEach { eq("0",it.amount) }; eq("90",zero.buy("USD").resultPercent)
        val single=pf(mapOf("USD" to "500"),mapOf("USD" to "100")).rebalance("b","100".toBigDecimal())
        eq("100",single.buy("USD").amount); eq("100",single.buy("USD").resultPercent)
        val unheld=pf(mapOf("USD" to "100"),mapOf("USD" to "50","EUR" to "50")).rebalance("b","100".toBigDecimal())
        eq("0",unheld.buy("USD").amount); eq("100",unheld.buy("EUR").amount)
        val untargeted=pf(mapOf("USD" to "100","EUR" to "100"),mapOf("USD" to "100"))
        val buys=untargeted.rebalance("b","50".toBigDecimal()); eq("50",buys.buy("USD").amount); eq("0",buys.buy("EUR").amount)
        val sold=untargeted.copy(buckets=listOf(untargeted.buckets.single().copy(allowSells=true))).rebalance("b","50".toBigDecimal())
        eq("150",sold.buy("USD").amount); eq("-100",sold.buy("EUR").amount)
    }
    private class Case(val p: Portfolio,val amount: BigDecimal,val c: Map<String,BigDecimal>,val t: Map<String,BigDecimal>)
    private fun random(rng: Random,sells: Boolean): Case {
        val assets=codes.shuffled(rng).take(rng.nextInt(2,7))
        val cuts=(List(assets.size-1) { rng.nextInt(0,101) }+0+100).sorted()
        val targets=assets.indices.associate { assets[it] to (cuts[it+1]-cuts[it]) }
        val values=assets.associateWith { rng.nextInt(0,100001) }
        val amount=rng.nextInt(0,50001).toBigDecimal()
        return Case(pf(values.mapValues { it.value.toString() },targets.mapValues { it.value.toString() },sells),amount,values.mapValues { it.value.toBigDecimal() },targets.mapValues { it.value.toBigDecimal() })
    }
    @Test fun randomPortfoliosSumExactlyAndNeverSellWithoutPermission() {
        val rng=Random(61)
        repeat(200) {
            val k=random(rng,false); val plan=k.p.rebalance("b",k.amount)
            assertEquals(0,k.amount.compareTo(plan.trades.fold(BigDecimal.ZERO) { s,t -> s+t.amount })); plan.trades.forEach { assertTrue("$it",it.amount.signum()>=0) }
            val s=random(rng,true); val sp=s.p.rebalance("b",s.amount)
            assertEquals(0,s.amount.compareTo(sp.trades.fold(BigDecimal.ZERO) { a,t -> a+t.amount }))
        }
    }
    @Test fun buyOnlyPlanBeatsRandomAlternatives() {
        val rng=Random(67)
        repeat(200) { run ->
            val k=random(rng,false); val plan=k.p.rebalance("b",k.amount)
            val total=k.c.values.fold(BigDecimal.ZERO,BigDecimal::add)+k.amount
            fun objective(b: Map<String,BigDecimal>)=k.c.keys.fold(BigDecimal.ZERO) { s,a -> s+(k.c.getValue(a)+b.getValue(a)-k.t.getValue(a)*total/HUNDRED).pow(2) }
            val best=objective(plan.trades.associate { it.asset to it.amount })
            repeat(50) {
                val w=k.c.keys.associateWith { rng.nextInt(0,10) }.let { m -> if(m.values.sum()==0) m+(m.keys.first() to 1) else m }
                val alt=objective(w.mapValues { k.amount*it.value.toBigDecimal().divideMoney(w.values.sum().toBigDecimal()) })
                assertTrue("run $run plan $best alt $alt",best<=alt+alt*BigDecimal("1e-9")+BigDecimal("1e-9"))
            }
        }
    }
    @Test fun quantitiesUseConversionFromBaseCurrency() {
        val q=Quote("EUR","1.25","x",1000,1000); val btc=Quote("BTC","50000","x",1000,1000)
        val p=Portfolio(buckets=listOf(Bucket("b","B","EUR",true,mapOf("USD" to "50","BTC" to "50"))),holdings=listOf(Holding("u","b","U","USD","1000"),Holding("c","b","C","BTC","0.01")),quotes=listOf(q,btc),settings=Settings("EUR")).validate()
        val plan=p.rebalance("b","200".toBigDecimal())
        eq("200",plan.buy("BTC").amount); eq("0.005",plan.buy("BTC").quantity); eq("0",plan.buy("USD").amount)
        val sold=p.copy(buckets=listOf(p.buckets.single().copy(allowSells=true))).rebalance("b","200".toBigDecimal())
        eq("-100",sold.buy("USD").amount); eq("-125",sold.buy("USD").quantity); eq("300",sold.buy("BTC").amount); eq("0.0075",sold.buy("BTC").quantity)
        assertEquals("Portfolio mode is off",p.copy(buckets=listOf(p.buckets.single().copy(portfolio=false))).rebalance("b","1".toBigDecimal()).unavailable)
    }
    @Test fun weightsCombineAndIgnoreUnknownAndExcludedTokens() {
        val usdt="0xdac17f958d2ee523a2206206994597c13d831ec7"; val unknown="0x"+"1".repeat(40); val gone="0x"+"3".repeat(40); val token=tokenAsset("ETH",usdt)
        val h=Holding("w","b","W","ETH","0","0x"+"2".repeat(40),fetchedAt=1000,tokens=listOf(Token(usdt,"USDT","Tether","250000000",6),Token(unknown,"X","","5",6),Token(gone,"G","","7",6)),excluded=listOf(gone))
        val p=Portfolio(buckets=listOf(Bucket("b","B","USD",true,mapOf("USD" to "50",token to "50"))),holdings=listOf(h,Holding("m1","b","M","USD","100"),Holding("m2","b","M","USD","150")),
            quotes=listOf(Quote(token,"1","x",1000,1000),Quote("ETH","2000","x",1000,1000),Quote(tokenAsset("ETH",gone),"1","x",1000,1000)),settings=Settings("USD")).validate()
        val w=p.weights("b")
        assertEquals(setOf("USD","ETH",token),w.rows.map { it.asset }.toSet()); eq("500",w.total)
        eq("250",w.rows.first { it.asset=="USD" }.value); eq("50",w.rows.first { it.asset=="USD" }.real); eq("50",w.rows.first { it.asset==token }.real); eq("0",w.rows.first { it.asset=="ETH" }.target)
    }
    @Test fun weightsUnavailableNameTheAsset() {
        val b=Bucket("b","B","USD",true,mapOf("USD" to "100"))
        val eur=Portfolio(buckets=listOf(b),holdings=listOf(Holding("h","b","E","EUR","100")),settings=Settings("USD")).weights("b")
        assertTrue(eur.rows.isEmpty()); assertEquals(listOf("EUR"),eur.missing)
        val none=Portfolio(buckets=listOf(b),holdings=listOf(Holding("w","b","W","ETH",null,"0x"+"2".repeat(40))),settings=Settings("USD")).weights("b")
        assertTrue(none.rows.isEmpty()); assertEquals(listOf("ETH"),none.missing)
        val token=tokenAsset("ETH","0x"+"4".repeat(40))
        val flagged=Portfolio(buckets=listOf(b.copy(targets=mapOf(token to "100"))),settings=Settings("USD")).weights("b")
        assertTrue(flagged.rows.isEmpty()); assertEquals(listOf(token),flagged.flagged)
    }
    @Test fun targetValidation() {
        val base=Portfolio(buckets=listOf(Bucket("b","B","USD")))
        fun v(portfolio: Boolean,t: Map<String,String>)=base.copy(buckets=listOf(Bucket("b","B","USD",portfolio,t))).validate()
        v(true,mapOf("USD" to "60","EUR" to "40")); v(true,mapOf("USD" to "33.33","EUR" to "66.67"))
        rejects { v(true,mapOf("USD" to "60","EUR" to "35")) }; rejects { v(true,mapOf("USD" to "99.999","EUR" to "0.001")) }
        rejects { v(true,mapOf("USD" to "101")) }; rejects { v(true,mapOf("USD" to "-1","EUR" to "101")) }; rejects { v(true,mapOf("XXXX" to "100")) }
        v(false,mapOf("USD" to "60","EUR" to "35")); rejects { v(false,mapOf("USD" to "100.5")) }
        assertEquals(mapOf("USD" to "60","EUR" to "35"),v(false,mapOf("USD" to "60","EUR" to "35")).buckets.single().targets)
        assertTrue(assertThrows(Exception::class.java) { v(true,mapOf("USD" to "60","EUR" to "35")) }.message!!.contains("Now 95%"))
    }
}
