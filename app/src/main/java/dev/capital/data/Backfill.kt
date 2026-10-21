package dev.capital.data

import dev.capital.domain.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun day(epochMillis: Long) = Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()
/** What a day's last snapshot tells about a bucket: quantities of its holdings by id and the quotes it carried (asset to observed day and USD price). */
private class Seen(val createdAt: Long, val quantities: Map<String,String>, val quotes: List<Triple<String,String,String>>)

/**
 * Rebuilds the history of a bucket whose Investment PnL was just switched on from the folder's past snapshots (see add-bucket-pnl):
 * the last snapshot of each UTC day gives a `qty:` point per holding of the bucket when its quantity changed, and a `rate:` point per
 * quote at the day the quote was observed when that day has none. Files are read and decoded one at a time; unreadable files are skipped.
 * Stored points win over rebuilt ones. Only the newest `maxDays` days are read.
 */
suspend fun backfill(files: SnapshotFiles, decode: (String)->Revision?, bucketId: String, current: Portfolio, today: LocalDate, maxDays: Int = 4000): Portfolio {
    val ids=current.holdings.filter { it.bucketId==bucketId }.map { it.id }.toSet()
    val days=sortedMapOf<String,Seen>()
    for(name in files.list()) {
        val r=runCatching { decode(files.read(name)) }.getOrNull() ?: continue
        val at=day(r.createdAt)
        if(at>today.toString() || (days[at]?.createdAt ?: Long.MIN_VALUE)>=r.createdAt) continue
        val accounts=r.data.accounts.associateBy { it.id }
        val quantities=r.data.holdings.filter { it.id in ids }.mapNotNull { h -> (h.accountId?.let { accounts[it]?.quantity } ?: h.quantity)?.let { h.id to it } }.toMap()
        days[at]=Seen(r.createdAt,quantities,r.data.quotes.filter { it.error==null && it.asset!="USD" }.map { Triple(it.asset,day(it.observedAt),it.usd) })
        if(days.size>maxDays) days.remove(days.firstKey())
    }
    val points=mutableMapOf<String,MutableMap<String,String>>()
    val previous=mutableMapOf<String,java.math.BigDecimal>()
    for((at,seen) in days) {
        for((id,q) in seen.quantities) {
            val value=q.toBigDecimal()
            if(previous[id]?.compareTo(value)==0) continue
            previous[id]=value; points.getOrPut("qty:$id") { mutableMapOf() }[at]=q
        }
        for((asset,observed,usd) in seen.quotes) points.getOrPut("rate:$asset") { mutableMapOf() }.putIfAbsent(observed,usd)
    }
    val stored=current.history.associateBy { it.key }
    val union=(stored.keys+points.keys).map { key -> Series(key,(points[key].orEmpty()+stored[key]?.points.orEmpty()).toSortedMap().entries.toList().takeLast(4000).associate { it.key to it.value }) }
    return current.copy(history=union)
}
