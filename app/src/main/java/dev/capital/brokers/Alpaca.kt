package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Alpaca Trading API (live): the account's equity, read with the user's API key id and secret. One account per key. */
object Alpaca: BrokerPlugin {
    override val name="Alpaca"
    override val site="https://alpaca.markets"
    override val credentials=listOf(Credential("Alpaca") { tr("API key: {0}","Alpaca") },Credential("Alpaca secret") { tr("API secret: {0}","Alpaca") })
    override val idForm=Regex("[A-Z0-9]{1,30}")
    override fun idLabel()=tr("Account number")
    override fun idError()=tr("Enter the account number shown by {0}","Alpaca")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private const val base="https://api.alpaca.markets/v2/account"
    private fun auth(host: BrokerHost)=mapOf("APCA-API-KEY-ID" to host.required("Alpaca",name),"APCA-API-SECRET-KEY" to host.required("Alpaca secret",name))
    private suspend fun account(host: BrokerHost)=host.json(base,headers=auth(host))
    override suspend fun accounts(host: BrokerHost)=account(host).let { a -> listOf(a.string("account_number").uppercase() to label(a.string("account_number"),a.text("account_number"),a.text("currency"))) }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=account(host)
        require(a.string("account_number").uppercase()==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("equity"),host.now)
    }
    override val listsFlows=true
    /** Non-trade activities CSD (deposit, +) and CSW (withdrawal, -), paged by the id of the last activity; `net_amount` carries the sign. One USD account per key. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val out=mutableListOf<BrokerFlow>(); var token=""
        // ponytail: `after` filters by creation date and its inclusiveness is not documented, so ask from the day before and filter by `date` here.
        repeat(100) {
            val url="$base/activities?activity_types=CSD,CSW&after=${since.minusDays(1)}&direction=asc&page_size=100"+(if(token.isBlank()) "" else "&page_token=$token")
            val page=(json.parseToJsonElement(host.text(url,headers=auth(host))) as? JsonArray)?.mapNotNull { it as? JsonObject } ?: throw ProviderFailure(tr("Invalid provider response"))
            page.forEach { a ->
                val date=a.text("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@forEach
                if(!date.isBefore(since)) out += BrokerFlow(date,a.string("net_amount"),"USD",a.string("id"))
            }
            token=page.lastOrNull()?.text("id").orEmpty()
            if(page.size<100 || token.isBlank()) return out
        }
        return out
    }
    override val listsHistory=true
    /** Daily equity from the portfolio history: `timestamp[]` (epoch seconds, left-labelled at 00:00 UTC) with `equity[]`; `start` instead of `period`, which has no documented maximum. */
    override suspend fun history(host: BrokerHost,id: String,since: LocalDate): List<Pair<LocalDate,Reading>> {
        val h=host.json("$base/portfolio/history?start=$since&timeframe=1D",headers=auth(host))
        val times=(h["timestamp"] as? JsonArray).orEmpty().map { (it as? JsonPrimitive)?.content?.toLongOrNull() }
        val equity=(h["equity"] as? JsonArray).orEmpty().map { (it as? JsonPrimitive)?.takeIf { p -> p !is JsonNull }?.content }
        return times.zip(equity).mapNotNull { (t,e) -> if(t==null || e==null) null else Instant.ofEpochSecond(t).atZone(ZoneOffset.UTC).toLocalDate().let { d -> d to Reading("USD",e,t*1000) } }
            .filter { !it.first.isBefore(since) }.groupBy { it.first }.map { (_,l) -> l.last() }.sortedBy { it.first }
    }
}
