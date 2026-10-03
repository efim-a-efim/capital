package dev.capital

import dev.capital.domain.*
import dev.capital.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.Base64
import kotlin.random.Random

class CoreTest {
    private fun goal(id: String,target: String,priority: Int=0)=Goal(id,id,target,"USD","2027-01-01",priority)
    private fun portfolio(amount: String="900",goals: List<Goal> = listOf(goal("g1","1000"),goal("g2","1000")))=Portfolio(
        buckets=listOf(Bucket("b","Savings","USD")),holdings=listOf(Holding("h","b","Cash","USD",amount)),goals=goals,connections=goals.map { Connection(it.id,"b") },
    )
    private fun eq(expected: String,actual: BigDecimal) { assertEquals(0,expected.toBigDecimal().compareTo(actual)) }
    private fun rejects(block: ()->Unit) { assertThrows(Exception::class.java) { block() } }
    @Test fun moneyPrecisionAndValidation() {
        val p=portfolio("9007199254740993.123456789012345678")
        assertEquals(p,decodeRevision(encodeRevision(Revision(data=p))).data)
        eq("9007199254740993.123456789012345678",p.bucketValue("b","USD"))
        rejects { "-1".decimal() }; rejects { "NaN".decimal() }; rejects { "1e200".decimal() }
        rejects { p.copy(holdings=p.holdings+p.holdings).validate() }
        rejects { p.copy(goals=listOf(goal("g1","0"))).validate() }
        rejects { p.copy(connections=listOf(Connection("g1","b",Limit.GOAL_PERCENT,"101"))).validate() }
        assertNull(p.convert(BigDecimal.ONE,"EUR","USD"))
        eq("0",p.convert("0.00".toBigDecimal(),"EUR","USD")!!)
        eq("1",p.convert(BigDecimal.ONE,"EUR","EUR")!!)
        // Every pair converts through USD: no direct EUR/GBP or BTC/EUR rate is stored.
        val q=p.copy(quotes=listOf(Quote("EUR","1.25","x",1000,1000),Quote("GBP","1.5","x",1000,1000),Quote("BTC","50000","x",1000,1000)))
        eq("125",q.convert(BigDecimal(150),"EUR","GBP")!!); eq("40000",q.convert(BigDecimal.ONE,"BTC","EUR")!!); eq("1.5",q.convert(BigDecimal.ONE,"GBP","USD")!!)
        assertNull(q.convert(BigDecimal.ONE,"EUR","ISK"))
    }
    @Test fun equalSharingCapsPrioritiesAndResidualUnits() {
        val equal=portfolio().allocate(); eq("450",equal.goal("g1")); eq("450",equal.goal("g2"))
        val capped=portfolio(goals=listOf(goal("g1","200"),goal("g2","1000"))).allocate(); eq("200",capped.goal("g1")); eq("700",capped.goal("g2"))
        val priority=portfolio(goals=listOf(goal("g1","600",2),goal("g2","600",1))).allocate(); eq("600",priority.goal("g1")); eq("300",priority.goal("g2"))
        val tiny=portfolio("0.000000000000000001").allocate(); eq("0.000000000000000001",tiny.goal("g1")); eq("0",tiny.goal("g2"))
        val cap=portfolio("600",listOf(goal("g1","1000"))).copy(connections=listOf(Connection("g1","b",Limit.BUCKET_PERCENT,"50","20"))).allocate(); eq("200",cap.goal("g1"))
    }
    @Test fun multiBucketReroutingAndNoOverfunding() {
        val p=Portfolio(buckets=listOf(Bucket("a","A","USD"),Bucket("b","B","USD")),holdings=listOf(Holding("ha","a","A","USD","100"),Holding("hb","b","B","USD","100")),goals=listOf(goal("g1","50"),goal("g2","100")),connections=listOf(Connection("g1","a"),Connection("g2","a"),Connection("g1","b")))
        val a=p.allocate(); eq("50",a.goal("g1")); eq("100",a.goal("g2")); eq("0",a.byConnection.getValue("a/g1")); eq("50",a.byConnection.getValue("b/g1"))
        val single=p.copy(goals=listOf(goal("g1","100")),connections=listOf(Connection("g1","a"),Connection("g1","b"))).allocate()
        eq("50",single.byConnection.getValue("a/g1")); eq("50",single.byConnection.getValue("b/g1"))
    }
    @Test fun randomGraphsConserveAndIgnoreInputOrder() {
        val rng=Random(47)
        repeat(100) {
            val buckets=(0..3).map { Bucket("b$it","Bucket $it","USD") }
            val holdings=buckets.map { Holding("h${it.id}",it.id,"Cash","USD",rng.nextInt(0,500).toString()) }
            val goals=(0..4).map { goal("g$it",rng.nextInt(1,500).toString(),rng.nextInt(0,3)) }
            val connections=buckets.flatMap { b -> goals.filter { rng.nextBoolean() }.map { g -> Connection(g.id,b.id,Limit.GOAL_PERCENT,rng.nextInt(0,101).toString()) } }
            val p=Portfolio(buckets,holdings,goals,connections).validate(); val a=p.allocate()
            buckets.forEach { assertTrue(a.bucket(it.id) <= p.bucketValue(it.id,"USD")); assertTrue(a.bucket(it.id)>=ZERO) }
            goals.forEach { assertTrue(a.goal(it.id) <= it.target.decimal()) }
            connections.forEach { c -> assertTrue(a.byConnection.getValue(c.key) <= (goals.first { it.id==c.goalId }.target.decimal()*c.value.decimal()).divideMoney(HUNDRED)) }
            assertEquals(a,p.copy(buckets=buckets.reversed(),holdings=holdings.reversed(),goals=goals.reversed(),connections=connections.reversed()).allocate())
        }
    }
    @Test fun snapshotsRecoverAndPreserveBranches() {
        val root=Revision(id="root",data=portfolio())
        val left=Revision(id="left",parents=listOf("root"),data=portfolio("100"))
        val right=Revision(id="right",parents=listOf("root"),data=portfolio("200"))
        val files=listOf(root,left,right).map(::encodeRevision)
        val conflict=scanRevisions(files+"partial file")
        assertTrue(conflict.conflicted); assertEquals(1,conflict.invalid); assertFalse(conflict.missingParents)
        val resolved=Revision(id="resolved",parents=listOf("left","right"),data=left.data)
        assertEquals(listOf(resolved),scanRevisions(files+encodeRevision(resolved)).heads)
        assertTrue(scanRevisions(listOf(encodeRevision(left))).missingParents)
        val future=root.copy(schema=SCHEMA+1)
        assertThrows(FutureSchema::class.java) { decodeRevision(encodeRevision(future)) }
        rejects { decodeRevision(encodeRevision(root).replace("900","901")) }
        rejects { scanRevisions(listOf(encodeRevision(root.copy(parents=listOf("left"))),encodeRevision(left))) }
    }
    @Test fun addressChecksAndAliases() {
        assertEquals("0x52908400098527886e0f7030069857d2e4169ee7",canonicalAddress(Chain.ETH,"0x52908400098527886E0F7030069857D2E4169EE7"))
        rejects { canonicalAddress(Chain.ETH,"0x52908400098527886E0F7030069857D2E4169Ee7") }
        assertEquals("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",canonicalAddress(Chain.BTC,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"))
        rejects { canonicalAddress(Chain.BTC,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNb") }
        val tron=canonicalAddress(Chain.TRX,"T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb")
        assertEquals("41"+"0".repeat(40),tron)
        assertEquals(tron,canonicalAddress(Chain.TRX,tron.uppercase()))
        val ton="EQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAM9c"
        assertEquals("0:"+"0".repeat(64),canonicalAddress(Chain.TON,ton))
        rejects { canonicalAddress(Chain.TON,ton.dropLast(1)+"d") }
        val bytes=Base64.getUrlDecoder().decode(ton); bytes[0]=0x51
        var crc=0
        bytes.take(34).forEach { byte -> crc=crc xor ((byte.toInt() and 255) shl 8); repeat(8) { crc=if(crc and 0x8000!=0) (crc shl 1) xor 0x1021 else crc shl 1; crc=crc and 65535 } }
        bytes[34]=(crc ushr 8).toByte(); bytes[35]=crc.toByte()
        assertEquals(canonicalAddress(Chain.TON,ton),canonicalAddress(Chain.TON,Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)))
    }
    @Test fun providersDistinguishInvalidFromZeroAndMergeSafely() {
        eq("0.000001",parseBtc(json.parseToJsonElement("""{"chain_stats":{"funded_txo_sum":150,"spent_txo_sum":50},"mempool_stats":{"funded_txo_sum":9999}}""").jsonObject).decimal())
        rejects { parseBtc(json.parseToJsonElement("{}").jsonObject) }
        rejects { parseRpc(json.parseToJsonElement("""{"error":{"code":-1}}""").jsonObject) }
        eq("1",baseQuantity("1000000000000000000",Chain.ETH).decimal())
        val old=Holding("wallet","b","ETH","ETH",null,"0x"+"0".repeat(40))
        val requested=portfolio().copy(holdings=listOf(old))
        val observation=old.copy(quantity="42",fetchedAt=1000)
        assertTrue(mergeObservations(requested.copy(holdings=emptyList()),requested,listOf(observation),emptyList()).holdings.isEmpty())
        val edited=old.copy(address="0x"+"1".repeat(40))
        assertEquals(edited,mergeObservations(requested.copy(holdings=listOf(edited)),requested,listOf(observation),emptyList()).holdings.single())
        val moved=old.copy(bucketId="other",label="Renamed")
        val merged=mergeObservations(requested.copy(holdings=listOf(moved)),requested,listOf(observation),emptyList()).holdings.single()
        assertEquals("other",merged.bucketId); assertEquals("Renamed",merged.label); assertEquals("42",merged.quantity)
    }
    @Test fun providerAddressesRoundTrip() {
        val tron="TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        assertEquals(tron,providerAddress(Chain.TRX,canonicalAddress(Chain.TRX,tron)))
        val ton="EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"
        assertEquals("0:b113a994b5024a16719f69139328eb759596c38a25f59028b146fecdc3621dfe",canonicalAddress(Chain.TON,ton))
        assertEquals(ton,providerAddress(Chain.TON,canonicalAddress(Chain.TON,ton)))
    }
    @Test fun schemaOneOpensWithDefaultTokenSources() {
        val obj=json.parseToJsonElement(json.encodeToString(Revision(data=portfolio()))).jsonObject
        val data=obj.getValue("data").jsonObject; val settings=data.getValue("settings").jsonObject
        val old=JsonObject(obj+mapOf("schema" to JsonPrimitive(1),"data" to JsonObject(data+mapOf(
            "holdings" to JsonArray(data.getValue("holdings").jsonArray.map { JsonObject(it.jsonObject-"tokens"-"tokensError") }),
            "settings" to JsonObject(settings+("providers" to JsonObject(settings.getValue("providers").jsonObject.filterKeys { "tokens" !in it }))),
        ))))
        val payload=old.toString()
        val revision=decodeRevision(json.encodeToString(Envelope(payload,checksum(payload))))
        assertEquals(1,revision.schema)
        assertEquals("Blockscout",revision.data.settings.providers["ETH tokens"]); assertEquals("Off",providerChoices.getValue("TRX tokens").last())
        revision.data.validate()
    }
    @Test fun tokenAssetsAndValuation() {
        val usdt="0xdac17f958d2ee523a2206206994597c13d831ec7"
        assertTrue(validAsset(tokenAsset("ETH",usdt))); assertFalse(validAsset(tokenAsset("BTC",usdt))); assertFalse(validAsset("ETH:0xABC"))
        assertTrue(validAsset(tokenAsset("TON","0:"+"a".repeat(64))))
        val t=Token(usdt,"USDT","Tether","250000000",6); val fake=Token("0x"+"1".repeat(40),"USDT","",  "5",6)
        val h=Holding("h","b","W","ETH","0","0x"+"2".repeat(40),fetchedAt=1000,tokens=listOf(t,fake))
        val p=Portfolio(buckets=listOf(Bucket("b","B","USD")),holdings=listOf(h),quotes=listOf(Quote(tokenAsset("ETH",usdt),"1","x",1000,1000),Quote("ETH","2000","x",1000,1000)),settings=Settings("USD"))
        p.validate(); eq("250",p.bucketValue("b","USD")); assertTrue(p.known(h,t)); assertFalse(p.known(h,fake)); assertFalse(p.incomplete("USD"))
        assertTrue(p.incomplete("EUR"))
        rejects { p.copy(holdings=listOf(h.copy(tokens=listOf(t,t)))).validate() }
        rejects { p.copy(holdings=listOf(h.copy(tokens=listOf(t.copy(units="1x"))))).validate() }
        rejects { p.copy(holdings=listOf(h.copy(address=null,quantity="1"))).validate() }
        assertFalse(p.stale(2000))
        assertFalse(p.copy(quotes=p.quotes+Quote(tokenAsset("ETH","0x"+"3".repeat(40)),"1","x",1,1,"old")).stale(2000))
        assertTrue(p.copy(holdings=listOf(h.copy(tokensError="x"))).stale(2000))
    }
    @Test fun excludedTokensAreNotCounted() {
        val usdt="0xdac17f958d2ee523a2206206994597c13d831ec7"
        val t=Token(usdt,"USDT","Tether","250000000",6)
        val h=Holding("h","b","W","ETH","0","0x"+"2".repeat(40),fetchedAt=1000,tokens=listOf(t))
        val p=Portfolio(buckets=listOf(Bucket("b","B","USD")),holdings=listOf(h),quotes=listOf(Quote(tokenAsset("ETH",usdt),"1","x",1000,1000,"err")),settings=Settings("USD"))
        eq("250",p.bucketValue("b","USD")); assertTrue(p.known(h,t)); assertTrue(p.stale(2000))
        val x=p.copy(holdings=listOf(h.copy(excluded=listOf(usdt)))).validate(); val xh=x.holdings.single()
        eq("0",x.bucketValue("b","USD")); assertFalse(x.known(xh,t)); assertFalse(x.stale(2000)); assertFalse(x.incomplete("EUR","b"))
        rejects { p.copy(holdings=listOf(h.copy(excluded=listOf("0xABC")))).validate() }
        rejects { p.copy(holdings=listOf(h.copy(excluded=listOf(usdt,usdt)))).validate() }
        rejects { p.copy(holdings=listOf(Holding("h","b","W","BTC","0","1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",excluded=listOf(usdt)))).validate() }
        rejects { p.copy(holdings=listOf(Holding("m","b","M","USD","1",excluded=listOf(usdt)))).validate() }
    }
    @Test fun unconvertibleValuesAreUnavailableNotZero() {
        val usd=Holding("u","b","U","USD","100")
        val p=Portfolio(buckets=listOf(Bucket("b","B","EUR"),Bucket("e","E","EUR")),holdings=listOf(usd),settings=Settings("EUR"))
        assertNull(p.bucketValueOrNull("b","EUR")); eq("0",p.bucketValue("b","EUR"))
        eq("5",p.copy(holdings=listOf(usd,Holding("e","b","E","EUR","5"))).bucketValueOrNull("b","EUR")!!)
        eq("0",p.bucketValueOrNull("e","EUR")!!)
    }
    private fun goalOn(id: String,target: String,due: String,priority: Int=0,archived: Boolean=false)=Goal(id,id,target,"USD",due,priority,archived)
    private fun prio(p: Portfolio,id: String)=p.goals.first { it.id==id }.priority
    @Test fun rankingByDateThenOldPriorityThenIndex() {
        val p=Portfolio(goals=listOf(goalOn("late","1","2027-06-01",9),goalOn("a","1","2027-01-01",1),goalOn("b","1","2027-01-01",5),goalOn("c","1","2027-01-01",1),goalOn("old","1","2020-01-01",9,true))).ranked()
        assertEquals(listOf("late","a","b","c","old"),p.goals.map { it.id })
        assertEquals(listOf(1,3,4,2,0),p.goals.map { it.priority })
        assertEquals(p,p.ranked())
        assertEquals(setOf(1,2,3,4),p.goals.filter { !it.archived }.map { it.priority }.toSet())
    }
    @Test fun movedDueDateWithZeroPriorityRanksLastInDate() {
        val p=Portfolio(goals=listOf(goalOn("a","1","2027-01-01",2),goalOn("b","1","2027-01-01",1),goalOn("m","1","2027-01-01",0),goalOn("z","1","2027-06-01",7))).ranked()
        assertTrue(prio(p,"m")<prio(p,"b")); assertTrue(prio(p,"m")>prio(p,"z"))
    }
    @Test fun moveGoalSwapsWithinDateOnly() {
        val p=Portfolio(goals=listOf(goalOn("a","1","2027-01-01"),goalOn("b","1","2027-01-01"),goalOn("c","1","2027-01-01"),goalOn("x","1","2027-02-01"))).ranked()
        assertEquals(p,p.moveGoal("a",true)); assertEquals(p,p.moveGoal("c",false)); assertEquals(p,p.moveGoal("x",true)); assertEquals(p,p.moveGoal("x",false))
        val up=p.moveGoal("b",true); assertTrue(prio(up,"b")>prio(up,"a")); assertEquals(prio(p,"a"),prio(up,"b")); assertEquals(prio(p,"x"),prio(up,"x"))
        val down=p.moveGoal("b",false); assertTrue(prio(down,"b")<prio(down,"c")); assertEquals(down,down.ranked())
        assertEquals(listOf("a","b","c","x"),up.goals.map { it.id })
    }
    @Test fun rankedNearerDateFundedFirst() {
        val p=portfolio(goals=listOf(goalOn("A","600","2027-01-01",1),goalOn("B","600","2027-06-01",9))).ranked().allocate()
        eq("600",p.goal("A")); eq("300",p.goal("B"))
    }
    private val today=java.time.LocalDate.parse("2026-10-01")
    private fun pl(id: String,amount: String,date: String,currency: String="USD")=Planned(id,id,amount,currency,date)
    private fun plan(planned: List<Planned>,base: Portfolio=Portfolio())=base.copy(goals=listOf(goalOn("A","300","2027-01-01"),goalOn("B","400","2027-02-01"))+base.goals,planned=planned).ranked()
    @Test fun projectionTwoSalaries() {
        val p=plan(listOf(pl("s2","500","2026-12-01"),pl("s1","500","2026-11-01"))).validate(); val r=p.project(today)
        assertEquals(listOf("s1"),r.contributions.getValue("A").map { it.plannedId }); eq("300",r.contributions.getValue("A")[0].usd)
        assertEquals(listOf("s1","s2"),r.contributions.getValue("B").map { it.plannedId }); eq("200",r.contributions.getValue("B")[0].usd); eq("200",r.contributions.getValue("B")[1].usd)
        assertEquals(mapOf("A" to "2026-11-01","B" to "2026-12-01"),r.closes); eq("300",r.final.getValue("A")); eq("400",r.final.getValue("B")); assertFalse(r.incomplete)
        eq("0",r.now.getValue("A")); eq("400",r.targets.getValue("B"))
    }
    @Test fun projectionPartialArchivedAndSameDate() {
        val r=plan(listOf(pl("s1","500","2026-11-01"))).project(today)
        assertNull(r.closes["B"]); assertEquals("2026-11-01",r.reached["B"]); eq("200",r.final.getValue("B"))
        val arch=plan(listOf(pl("old","500","2026-09-30"),pl("t","300","2026-10-01"))).project(today)
        assertEquals(listOf("t"),arch.contributions.getValue("A").map { it.plannedId }); assertNull(arch.contributions["B"]); assertEquals("2026-10-01",arch.closes["A"])
        val same=plan(listOf(pl("x","300","2026-11-01"),pl("y","400","2026-11-01"))).project(today)
        assertEquals("x",same.contributions.getValue("A")[0].plannedId); assertEquals(listOf("y"),same.contributions.getValue("B").map { it.plannedId }); assertEquals("2026-11-01",same.closes["B"])
    }
    @Test fun plannedArchivesByDateAndReactivatesOnEdit() {
        val old=pl("s","300","2026-09-30"); assertTrue(old.archived(today)); assertFalse(pl("t","300","2026-10-01").archived(today))
        val before=plan(listOf(old)).validate().project(today)
        assertNull(before.contributions["A"]); assertNull(before.closes["A"])
        val after=plan(listOf(old.copy(date="2026-10-02"))).validate().project(today)
        assertEquals(listOf("s"),after.contributions.getValue("A").map { it.plannedId }); assertEquals("2026-10-02",after.closes["A"])
    }
    @Test fun plannedTopsUpOnlyWhatBucketsLeaveOpen() {
        // A is funded by its bucket, so the whole planned amount goes to the next goal, not shared with A.
        val base=Portfolio(buckets=listOf(Bucket("b","B","USD")),holdings=listOf(Holding("h","b","Cash","USD","900")),connections=listOf(Connection("A","b")))
        val p=base.copy(goals=listOf(goalOn("A","600","2027-01-01"),goalOn("G","80","2027-01-01"),goalOn("T","350","2027-01-01")),planned=listOf(pl("s","100","2026-11-01"))).ranked().validate()
        val r=p.project(today)
        eq("600",r.now.getValue("A")); assertNull(r.contributions["A"])
        eq("80",r.final.getValue("G")); assertEquals("2026-11-01",r.closes["G"]); eq("20",r.final.getValue("T")); assertNull(r.closes["T"])
        val a=p.allocate(BigDecimal(100)); eq("600",a.byConnection.getValue("b/A")); eq("0",a.byConnection.getValue("~planned/A")); assertEquals(p.allocate().goal("A"),a.goal("A"))
    }
    @Test fun projectionUnconvertibleAndExistingFunds() {
        val r=plan(listOf(pl("e","500","2026-11-01","EUR"),pl("s","500","2026-11-02"))).project(today)
        assertTrue(r.incomplete); assertEquals("s",r.contributions.getValue("A")[0].plannedId)
        val base=Portfolio(buckets=listOf(Bucket("b","B","USD")),holdings=listOf(Holding("h","b","Cash","USD","100")),connections=listOf(Connection("A","b")))
        val e=plan(listOf(pl("s","500","2026-11-01")),base).project(today)
        eq("100",e.now.getValue("A")); eq("200",e.contributions.getValue("A")[0].usd); eq("300",e.final.getValue("A"))
    }
    @Test fun plannedDoesNotChangeAllocationOrValue() {
        val base=portfolio("900",listOf(goalOn("g1","600","2027-01-01"),goalOn("g2","600","2027-06-01"))).ranked()
        val withPlan=base.copy(planned=listOf(pl("s","500","2026-11-01")))
        assertEquals(base.allocate(),withPlan.allocate()); assertEquals(base.allocate(),base.allocate(ZERO))
        withPlan.project(today); assertEquals(base.allocate(),withPlan.allocate())
        eq("900",withPlan.bucketValue("b","USD")); assertFalse(withPlan.incomplete("USD")); withPlan.validate()
        assertTrue(base.allocate(BigDecimal(50)).byConnection.keys.any { it.startsWith("~planned/") })
        rejects { withPlan.copy(planned=listOf(pl("s","0","2026-11-01"))).validate() }
        rejects { withPlan.copy(planned=listOf(pl("s","1","2026-11-01","ETH:0x"+"1".repeat(40)))).validate() }
        rejects { withPlan.copy(planned=listOf(pl("s","1","2026-13-01"))).validate() }
        rejects { withPlan.copy(planned=listOf(pl("s","1","2026-11-01"),pl("s","1","2026-11-01"))).validate() }
        rejects { withPlan.copy(buckets=listOf(Bucket("~planned","x","USD"))).validate() }
    }
    // ranked (distinct priorities) only: with equal-priority sharing the total can drop as extra grows (unranked, seed 53, run 1, g0, extra 50)
    @Test fun fundedTotalsGrowMonotonicallyWithExtra() {
        val rng=Random(53)
        repeat(100) {
            val buckets=(0..3).map { Bucket("b$it","Bucket $it","USD") }
            val holdings=buckets.map { Holding("h${it.id}",it.id,"Cash","USD",rng.nextInt(0,500).toString()) }
            val goals=(0..4).map { goal("g$it",rng.nextInt(1,500).toString(),rng.nextInt(0,3)) }
            val connections=buckets.flatMap { b -> goals.filter { rng.nextBoolean() }.map { g -> Connection(g.id,b.id,Limit.GOAL_PERCENT,rng.nextInt(0,101).toString()) } }
            val p=Portfolio(buckets,holdings,goals,connections).ranked().validate()
            var last=goals.associate { it.id to ZERO }
            listOf(0,50,200,1000).forEach { x ->
                val a=p.allocate(BigDecimal(x))
                goals.forEach { g -> assertTrue("$it ${g.id} x=$x",a.goal(g.id)>=last.getValue(g.id)); assertTrue(a.goal(g.id)<=g.target.decimal()) }
                last=goals.associate { it.id to a.goal(it.id) }
            }
        }
    }
    private fun oldPayload(schema: Int,p: Portfolio): String {
        val obj=json.parseToJsonElement(json.encodeToString(Revision(data=p))).jsonObject
        val data=obj.getValue("data").jsonObject; val settings=data.getValue("settings").jsonObject
        var d=data-"planned"
        if(schema==1) d=JsonObject(d+mapOf("holdings" to JsonArray(d.getValue("holdings").jsonArray.map { JsonObject(it.jsonObject-"tokens"-"tokensError") }),"settings" to JsonObject(settings+("providers" to JsonObject(settings.getValue("providers").jsonObject.filterKeys { "tokens" !in it })))))
        val payload=JsonObject(obj+mapOf("schema" to JsonPrimitive(schema),"data" to JsonObject(d))).toString()
        return json.encodeToString(Envelope(payload,checksum(payload)))
    }
    @Test fun oldSchemasConvertByRanking() {
        val p=portfolio(goals=listOf(goalOn("a","1","2027-01-01",1),goalOn("b","1","2027-01-01",5),goalOn("late","1","2027-06-01",9)))
        listOf(1,2).forEach { v ->
            val text=oldPayload(v,p); val r=decodeRevision(text)
            assertEquals(v,r.schema); assertTrue(r.data.planned.isEmpty())
            assertTrue(prio(r.data,"b")>prio(r.data,"a")); assertTrue(prio(r.data,"a")>prio(r.data,"late"))
        }
        val text=encodeRevision(Revision(data=p)); assertEquals(9,prio(decodeRevision(text).data,"late"))
        assertThrows(FutureSchema::class.java) { decodeRevision(oldPayload(SCHEMA+1,p)) }
    }
    @Test fun accountsLinkIntoOneBucketAndMirrorTheirValue() {
        val ib=Account("ib","Broker","Interactive Brokers","123456")
        val oanda=Account("oa","Forex","OANDA","001-001-1234567-001","EUR","1200.5",5,6)
        val p=portfolio().copy(accounts=listOf(ib,oanda),holdings=listOf(Holding("h1","b","Broker","USD",null,accountId="ib"),Holding("h2","b","Forex","USD",null,accountId="oa"))).linked().validate()
        val h2=p.holdings.first { it.id=="h2" }
        assertEquals("EUR",h2.asset); assertEquals("1200.5",h2.quantity); assertEquals("OANDA",h2.source); assertEquals(6L,h2.fetchedAt)
        assertEquals("USD",p.holdings.first { it.id=="h1" }.asset); assertEquals("Not refreshed",p.holdings.first { it.id=="h1" }.source)
        assertTrue(p.incomplete("USD"))
        eq("1200.5",p.copy(holdings=p.holdings.filter { it.id=="h2" },quotes=listOf(Quote("EUR","1","t",1,1))).bucketValueOrNull("b","EUR")!!)
        assertTrue(p.stale()); assertFalse(portfolio().copy(accounts=listOf(oanda.copy(fetchedAt=System.currentTimeMillis()))).stale())
        rejects { p.copy(holdings=p.holdings+Holding("h3","b","Again","USD",null,accountId="oa")).linked().validate() }
        rejects { p.copy(holdings=p.holdings+Holding("h3","b","Gone","USD",null,accountId="zz")).validate() }
        rejects { p.copy(holdings=p.holdings.map { if(it.id=="h2") it.copy(quantity="1") else it }).validate() }
        rejects { p.copy(accounts=p.accounts+ib.copy(id="ib2")).validate() }
        rejects { p.copy(accounts=listOf(ib.copy(address="12 34"))).validate() }
        rejects { p.copy(accounts=listOf(oanda.copy(address="123456"))).validate() }
        rejects { p.copy(accounts=listOf(oanda.copy(asset="BTC"))).validate() }
        rejects { p.copy(accounts=listOf(ib.copy(broker="Robinhood"))).validate() }
        rejects { p.copy(accounts=listOf(ib.copy(name=" "))).validate() }
        rejects { portfolio().copy(holdings=listOf(Holding("h","b","Old","USD",null,"123456",broker="Interactive Brokers"))).validate() }
        assertEquals(listOf("oa"),p.deleteAccount("ib").accounts.map { it.id }); assertEquals(listOf("h2"),p.deleteAccount("ib").holdings.map { it.id })
        // Dust: below the threshold in the default currency the linked holding counts as 0; without a rate nothing is ignored.
        val dust=p.copy(settings=Settings("USD"),accounts=p.accounts.map { if(it.id=="oa") it.copy(ignoreBelow="1500") else it },quotes=listOf(Quote("EUR","1.1","t",1,1))).linked().validate()
        assertTrue(dust.ignored(dust.accounts.first { it.id=="oa" })); assertEquals("0",dust.holdings.first { it.id=="h2" }.quantity)
        assertEquals("1200.5",dust.copy(accounts=dust.accounts.map { it.copy(ignoreBelow="1000") }).linked().holdings.first { it.id=="h2" }.quantity)
        assertEquals("1200.5",dust.copy(quotes=emptyList()).linked().holdings.first { it.id=="h2" }.quantity)
        rejects { dust.copy(accounts=dust.accounts.map { it.copy(ignoreBelow="-1") }).validate() }
        assertEquals("001-001-1234567-001",accountId("OANDA"," 001-001-1234567-001 "))
        rejects { accountId("OANDA","0010011234567001") }
        rejects { accountId(null,"1") }
        assertEquals("8b5f262d-4bb9-365d-888a-202bd3b15fa1",accountId("SnapTrade","8B5F262D-4BB9-365D-888A-202BD3B15FA1"))
        assertEquals("42",accountId("Trading 212","42")); rejects { accountId("Trading 212","4-2") }
        assertEquals(listOf("Interactive Brokers","OANDA","Trading 212","SnapTrade"),brokerChoices); assertEquals(brokerChoices,brokerCredentials.keys.toList())
        rejects { portfolio().copy(accounts=listOf(Account("s","S","Saxo","live"))).validate() }
    }
    @Test fun schemaFiveBrokerHoldingsBecomeAccounts() {
        val p=portfolio().copy(holdings=listOf(Holding("w","b","W","ETH","0","0x"+"2".repeat(40)),Holding("ib","b","Broker","EUR","12.5","123456",observedAt=1,fetchedAt=2,source="Interactive Brokers",broker="Interactive Brokers"),Holding("oa","b","Forex","USD",null,"001-001-1234567-001",broker="OANDA")))
        val obj=json.parseToJsonElement(json.encodeToString(Revision(data=p))).jsonObject
        val old=JsonObject(obj+mapOf("schema" to JsonPrimitive(5),"data" to JsonObject(obj.getValue("data").jsonObject-"accounts")))
        val payload=old.toString()
        val r=decodeRevision(json.encodeToString(Envelope(payload,checksum(payload))))
        assertEquals(5,r.schema); assertEquals(2,r.data.accounts.size)
        val ib=r.data.accounts.first { it.broker=="Interactive Brokers" }; val oa=r.data.accounts.first { it.broker=="OANDA" }
        assertEquals("123456",ib.address); assertEquals("EUR",ib.asset); assertEquals("12.5",ib.quantity); assertEquals(2L,ib.fetchedAt); assertNull(oa.asset)
        val linked=r.data.holdings.first { it.id=="ib" }
        assertEquals(ib.id,linked.accountId); assertNull(linked.broker); assertNull(linked.address); assertEquals("12.5",linked.quantity)
        assertEquals("USD",r.data.holdings.first { it.id=="oa" }.asset); assertNull(r.data.holdings.first { it.id=="w" }.accountId)
        r.data.validate()
    }
    @Test fun schemaFourOpensWithoutBrokers() {
        val p=portfolio().copy(holdings=listOf(Holding("w","b","W","ETH","0","0x"+"2".repeat(40))))
        val obj=json.parseToJsonElement(json.encodeToString(Revision(data=p))).jsonObject
        val data=obj.getValue("data").jsonObject
        val old=JsonObject(obj+mapOf("schema" to JsonPrimitive(4),"data" to JsonObject(data+("holdings" to JsonArray(data.getValue("holdings").jsonArray.map { JsonObject(it.jsonObject-"broker") })))))
        val payload=old.toString()
        val r=decodeRevision(json.encodeToString(Envelope(payload,checksum(payload))))
        assertEquals(4,r.schema); assertNull(r.data.holdings.single().broker)
        assertEquals(6,SCHEMA)
    }
    @Test fun schemaThreeOpensWithPortfolioDefaults() {
        val p=portfolio().let { it.copy(buckets=listOf(Bucket("b","Savings","USD",true,mapOf("USD" to "100"),true))) }
        val obj=json.parseToJsonElement(json.encodeToString(Revision(data=p))).jsonObject
        val data=obj.getValue("data").jsonObject
        val old=JsonObject(obj+mapOf("schema" to JsonPrimitive(3),"data" to JsonObject(data+("buckets" to JsonArray(data.getValue("buckets").jsonArray.map { JsonObject(it.jsonObject-"portfolio"-"targets"-"allowSells") })))))
        val payload=old.toString()
        val r=decodeRevision(json.encodeToString(Envelope(payload,checksum(payload))))
        assertEquals(3,r.schema); assertEquals(Bucket("b","Savings","USD"),r.data.buckets.single())
        assertFalse(r.data.buckets.single().portfolio); assertTrue(r.data.buckets.single().targets.isEmpty()); assertFalse(r.data.buckets.single().allowSells)
    }
}
