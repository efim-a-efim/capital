package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/** Tradier Brokerage API: total equity of an account with the personal API token (tokens from the settings page never expire). Amounts are USD. */
object Tradier: BrokerPlugin {
    override val name="Tradier"
    override val site="https://tradier.com"
    override val credentials=listOf(Credential("Tradier") { tr("Access token: {0}","Tradier") })
    override val idForm=Regex("[A-Z0-9]{1,30}")
    override fun idLabel()=tr("Account number")
    override fun idError()=tr("Enter the account number shown by {0}","Tradier")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private fun auth(host: BrokerHost)=mapOf("Authorization" to "Bearer ${host.required(name,name)}","Accept" to "application/json")
    /** Tradier JSON: one item comes as an object, several as an array. */
    private fun JsonObject.items(key: String)=when(val a=get(key)) { is JsonArray -> a.mapNotNull { it as? JsonObject }; is JsonObject -> listOf(a); else -> emptyList() }
    override suspend fun accounts(host: BrokerHost): List<Pair<String,String>> {
        val profile=host.json("https://api.tradier.com/v1/user/profile",headers=auth(host)).child("profile") ?: return emptyList()
        return profile.items("account").filter { it.text("status")!="closed" }.mapNotNull { a -> a.text("account_number")?.uppercase()?.let { it to label(it,it,a.text("classification"),a.text("type")) } }
    }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val body=host.json("https://api.tradier.com/v1/accounts/$id/balances",headers=auth(host))
        // The reference example nests the figures under balance.balances, the schema under balances.
        val b=body.child("balance")?.child("balances") ?: body.child("balances") ?: body.child("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balances"))
        b.text("account_number")?.let { require(it.uppercase()==id) { tr("Account mismatch") } }
        return Reading("USD",b.text("total_equity") ?: throw ProviderFailure(tr("Missing {0} in provider response","total_equity")),host.now)
    }
    override val listsFlows=true
    /** Account history events of type ach, wire and check (external money), paged per type; there is no date filter and no event id, so the ref is built from the row. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val out=mutableListOf<BrokerFlow>()
        for(type in listOf("ach","wire","check")) {
            var page=1
            while(true) {
                val h=host.json("https://api.tradier.com/v1/accounts/$id/history?type=$type&limit=100&page=$page",headers=auth(host)).child("history") ?: break
                // The guide nests details under a key named after the type (e.g. "ach": {...}); the reference shows a flat event. Type and amount sit on the event in both.
                h.items("event").forEach { e ->
                    val date=e.text("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@forEach
                    val amount=e.text("amount") ?: return@forEach
                    if(e.text("type")==type && !date.isBefore(since)) out += BrokerFlow(date,amount,"USD","$type $date $amount")
                }
                if(page>=(h.text("total_pages")?.toIntOrNull() ?: 1)) break
                page++
            }
        }
        // Same-day rows with equal amounts are indistinguishable: number them so the refs stay unique and stable.
        return out.sortedBy { it.date }.groupBy { it.ref }.values.flatMap { g -> g.mapIndexed { i,f -> if(i==0) f else f.copy(ref="${f.ref} #${i+1}") } }.sortedBy { it.date }
    }
    override val listsHistory=true
    /** `historical-balances` with the smallest `period` covering `since`; `balances[].value` per trading day (whether it equals total_equity is not stated; `total_equity` is accepted too). */
    override suspend fun history(host: BrokerHost,id: String,since: LocalDate): List<Pair<LocalDate,Reading>> {
        val days=ChronoUnit.DAYS.between(since,LocalDate.now())
        val period=when { days<=7 -> "WEEK"; days<=31 -> "MONTH"; days<=366 -> "YEAR"; days<=3*366 -> "YEAR_3"; days<=5*366 -> "YEAR_5"; else -> "ALL" }
        val b=host.json("https://api.tradier.com/v1/accounts/$id/historical-balances?period=$period",headers=auth(host))
        return (b.child("balances")?.items("balance") ?: b.items("balances")).mapNotNull { r ->
            val d=r.text("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            val v=r.text("value") ?: r.text("total_equity") ?: return@mapNotNull null
            if(d.isBefore(since)) null else d to Reading("USD",v,d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        }.groupBy { it.first }.map { (_,l) -> l.last() }.sortedBy { it.first }
    }
}
