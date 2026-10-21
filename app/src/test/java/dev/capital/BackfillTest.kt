package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class BackfillTest {
    private fun at(day: String,hour: Int)=LocalDate.parse(day).atTime(hour,0).toInstant(ZoneOffset.UTC).toEpochMilli()
    private fun portfolio(manual: String,account: String,eur: String,observed: Long)=Portfolio(
        buckets=listOf(Bucket("b","B","USD",pnl=true),Bucket("c","C","USD")),accounts=listOf(Account("al","A","Alpaca","PA123","USD",account)),
        holdings=listOf(Holding("m","b","Manual","EUR",manual),Holding("l","b","Linked","USD",account,accountId="al"),Holding("o","c","Other","EUR","99")),
        quotes=listOf(Quote("EUR",eur,"x",observed,observed)),
    )
    private val files=MemFiles().apply {
        map["capital-r1.json"]=encodeRevision(Revision(id="r1",createdAt=at("2026-01-01",10),data=portfolio("10","100","1.1",at("2026-01-01",9))))
        map["capital-r2.json"]=encodeRevision(Revision(id="r2",parents=listOf("r1"),createdAt=at("2026-01-01",15),data=portfolio("12","100","1.2",at("2026-01-01",14))))
        map["capital-r3.json"]=encodeRevision(Revision(id="r3",parents=listOf("r2"),createdAt=at("2026-01-02",9),data=portfolio("12","150","1.3",at("2026-01-02",8))))
        map["capital-r0.json"]="broken"
    }
    private val current=portfolio("12","150","1.3",at("2026-01-02",8))
    private val decode: (String)->Revision? = { runCatching { decodeRevision(it) }.getOrNull() }
    @Test fun lastRevisionPerDayGivesQuantityChangesAndDailyRates()=runBlocking {
        val out=backfill(files,decode,"b",current,LocalDate.parse("2026-01-10"))
        val h=out.history.associate { it.key to it.points }
        assertEquals(mapOf("2026-01-01" to "12"),h["qty:m"])
        assertEquals(mapOf("2026-01-01" to "100","2026-01-02" to "150"),h["qty:l"])
        assertEquals(mapOf("2026-01-01" to "1.2","2026-01-02" to "1.3"),h["rate:EUR"])
        assertEquals(setOf("qty:m","qty:l","rate:EUR"),h.keys)
        out.validate(); Unit
    }
    @Test fun storedPointsWinAndTheDayCapKeepsTheNewestDays()=runBlocking {
        val stored=current.copy(history=listOf(Series("rate:EUR",mapOf("2026-01-01" to "9")),Series("qty:m",mapOf("2025-12-01" to "5"))))
        val out=backfill(files,decode,"b",stored,LocalDate.parse("2026-01-10"))
        assertEquals("9",out.history.first { it.key=="rate:EUR" }.points["2026-01-01"])
        assertEquals(mapOf("2025-12-01" to "5","2026-01-01" to "12"),out.history.first { it.key=="qty:m" }.points)
        val capped=backfill(files,decode,"b",current,LocalDate.parse("2026-01-10"),maxDays=1)
        assertEquals(mapOf("2026-01-02" to "12"),capped.history.first { it.key=="qty:m" }.points)
        assertEquals(mapOf("2026-01-02" to "150"),capped.history.first { it.key=="qty:l" }.points)
        // only the holdings of the asked bucket; snapshots after today are ignored
        assertEquals(setOf("qty:o","rate:EUR"),backfill(files,decode,"c",current,LocalDate.parse("2026-01-10")).history.map { it.key }.toSet())
        assertTrue(backfill(files,decode,"b",current,LocalDate.parse("2025-12-31")).history.isEmpty())
    }
}
