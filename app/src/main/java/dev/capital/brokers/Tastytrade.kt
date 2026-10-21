package dev.capital.brokers

import dev.capital.data.KeyArgument
import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/** tastytrade Open API: net liquidating value, with an OAuth personal grant (refresh tokens never expire) traded for a 15-minute access token per refresh. */
object Tastytrade: BrokerPlugin {
    override val name="tastytrade"
    override val site="https://tastytrade.com"
    override val credentials=listOf(Credential("tastytrade") { tr("Refresh token: {0}","tastytrade") },Credential("tastytrade client secret") { tr("Client secret: {0}","tastytrade") })
    override val idForm=Regex("[A-Z0-9]{1,30}")
    override fun idLabel()=tr("Account number")
    override fun idError()=tr("Enter the account number shown by {0}","tastytrade")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private const val base="https://api.tastyworks.com"
    private val agent=mapOf("User-Agent" to "capital/2","Accept" to "application/json")
    private suspend fun auth(host: BrokerHost): Map<String,String> {
        val body=buildJsonObject { put("grant_type","refresh_token"); put("refresh_token",host.required("tastytrade",name)); put("client_secret",host.required("tastytrade client secret",name)) }.toString()
        val r=host.send("$base/oauth/token",body,agent+("Content-Type" to "application/json"),soft=setOf(400,401))
        // A wrong or revoked refresh token or client secret answers 400 invalid_grant / 401.
        if(r.status!=200) throw KeyArgument(tr("{0} rejected the refresh token or client secret; create a new grant",name))
        val token=json.parseToJsonElement(r.body).jsonObject.string("access_token")
        return agent+("Authorization" to "Bearer $token")
    }
    override suspend fun accounts(host: BrokerHost): List<Pair<String,String>> =
        host.json("$base/customers/me/accounts",headers=auth(host)).child("data")?.list("items").orEmpty().mapNotNull { it.child("account") }
            .filter { it.text("is-closed")!="true" }.mapNotNull { a -> a.text("account-number")?.uppercase()?.let { it to label(it,a.text("nickname"),a.text("account-type-name"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val d=host.json("$base/accounts/$id/balances",headers=auth(host)).child("data") ?: throw ProviderFailure(tr("Missing {0} in provider response","data"))
        d.text("account-number")?.let { require(it.uppercase()==id) { tr("Account mismatch") } }
        return Reading(d.text("currency") ?: "USD",d.text("net-liquidating-value") ?: throw ProviderFailure(tr("Missing {0} in provider response","net-liquidating-value")),host.now)
    }
    override val listsFlows=true
    /** Money Movement transactions with sub-type Deposit or Withdrawal, paged by `page-offset`; the sign comes from `net-value-effect` (Credit/Debit), else from the sub-type. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val headers=auth(host); val out=mutableListOf<BrokerFlow>(); var page=0
        while(true) {
            val r=host.json("$base/accounts/$id/transactions?start-date=$since&type=Money%20Movement&sort=Asc&per-page=250&page-offset=$page",headers=headers)
            val items=r.child("data")?.list("items").orEmpty()
            items.forEach { t ->
                if(t.text("transaction-type")!="Money Movement") return@forEach
                val sub=t.text("transaction-sub-type"); if(sub!="Deposit" && sub!="Withdrawal") return@forEach
                val date=(t.text("transaction-date") ?: t.text("executed-at"))?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@forEach
                val value=(t.text("net-value") ?: t.text("value") ?: return@forEach).trim().removePrefix("-").removePrefix("+")
                val debit=when(t.text("net-value-effect") ?: t.text("value-effect")) { "Credit" -> false; "Debit" -> true; else -> sub=="Withdrawal" }
                if(!date.isBefore(since)) out += BrokerFlow(date,if(debit) "-$value" else value,t.text("currency") ?: "USD",t.text("id") ?: return@forEach)
            }
            val pages=r.child("pagination")?.text("total-pages")?.toIntOrNull() ?: 1
            if(items.isEmpty() || page+1>=pages) return out.sortedBy { it.date }
            page++
        }
    }
    override val listsHistory=true
    /** Net liquidating value history with the smallest `time-back` covering `since`; one `close` (or `totalClose`) per day, the last entry of a day when there are several. */
    override suspend fun history(host: BrokerHost,id: String,since: LocalDate): List<Pair<LocalDate,Reading>> {
        val days=ChronoUnit.DAYS.between(since,LocalDate.now())
        val back=when { days<=1 -> "1d"; days<=7 -> "1w"; days<=31 -> "1m"; days<=92 -> "3m"; days<=184 -> "6m"; days<=366 -> "1y"; else -> "all" }
        val r=host.json("$base/accounts/$id/net-liq/history?time-back=$back",headers=auth(host))
        // The OpenAPI file types `data` as one object although the endpoint returns a series: accept an array, an object with `items`, or one object.
        val rows=when(val d=r["data"]) { is JsonArray -> d.mapNotNull { it as? JsonObject }; is JsonObject -> d["items"]?.let { i -> (i as? JsonArray)?.mapNotNull { it as? JsonObject } } ?: listOf(d); else -> emptyList() }
        return rows.mapNotNull { o ->
            val d=o.text("time")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            val v=o.text("close") ?: o.text("totalClose") ?: o.text("total-close") ?: return@mapNotNull null
            if(d.isBefore(since)) null else d to Reading("USD",v,d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        }.groupBy { it.first }.map { (_,l) -> l.last() }.sortedBy { it.first }
    }
}
