package dev.capital

import dev.capital.domain.*
import dev.capital.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
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
        val future=root.copy(schema=2)
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
}
