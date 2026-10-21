package dev.capital.brokers

import dev.capital.data.KeyArgument
import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.*

/** SnapTrade Personal (aggregator): signed requests with the user's own client id and consumer key; account list, Connection Portal link, account total. */
object SnapTrade: BrokerPlugin {
    override val name="SnapTrade"
    override val site="https://snaptrade.com"
    override val credentials=listOf(Credential("SnapTrade") { tr("Client id: SnapTrade") },Credential("SnapTrade consumer key") { tr("Consumer key: SnapTrade") })
    override val idForm=Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    override fun idLabel()=tr("SnapTrade account id")
    override fun idError()=tr("Choose a SnapTrade account or enter its id")
    override fun normalizeId(raw: String)=raw.trim().lowercase()
    override val listsAccounts=true
    override fun noAccounts()=tr("No accounts connected yet. Connect a brokerage through SnapTrade first.")
    override fun connectLabel()=tr("Connect a brokerage through SnapTrade")
    private fun JsonObject.opt(key: String)=get(key)?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
    /** HMAC-SHA256 of the canonical payload with the consumer key. GET when `post` is false. */
    private suspend fun snap(host: BrokerHost,path: String,post: Boolean=false,params: String=""): String {
        val client=host.required("SnapTrade",name); val secret=host.required("SnapTrade consumer key",name)
        if(!client.matches(Regex("[A-Za-z0-9_-]{1,100}"))) throw KeyArgument(tr("Invalid SnapTrade client id"))
        // The signature covers the query string exactly as sent, so `params` (already `&k=v` pairs) are appended before signing.
        val query="clientId=$client&timestamp=${host.now/1000}$params"
        // The only POST sends an empty body, which SnapTrade signs as null content.
        val canonical="""{"content":null,"path":"/api/v1$path","query":"$query"}"""
        val mac=javax.crypto.Mac.getInstance("HmacSHA256").apply { init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(),"HmacSHA256")) }
        val signature=java.util.Base64.getEncoder().encodeToString(mac.doFinal(canonical.toByteArray()))
        return host.text("https://api.snaptrade.com/api/v1$path?$query",if(post) "{}" else null,mapOf("Signature" to signature))
    }
    /** Connection Portal URL for the user's own SnapTrade account; valid 5 minutes. */
    override suspend fun connect(host: BrokerHost): String = json.parseToJsonElement(snap(host,"/snapTrade/login",post=true)).jsonObject.string("redirectURI").also { require(it.startsWith("https://")) { tr("Invalid provider response") } }
    override suspend fun accounts(host: BrokerHost): List<Pair<String,String>> = json.parseToJsonElement(snap(host,"/accounts")).jsonArray.map { it.jsonObject }.mapNotNull { a ->
        val id=a.opt("id")?.lowercase() ?: return@mapNotNull null
        id to listOfNotNull(a.opt("institution_name"),a.opt("name"),a.opt("number")).map { it.clean() }.filter { it.isNotBlank() }.distinct().joinToString(" · ").ifBlank { id }
    }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=json.parseToJsonElement(snap(host,"/accounts/$id")).jsonObject
        require(a.string("id").lowercase()==id) { tr("Account mismatch") }
        val t=a["balance"]?.takeIf { it !is JsonNull }?.jsonObject?.get("total")?.takeIf { it !is JsonNull }?.jsonObject ?: throw ProviderFailure(tr("SnapTrade has no total value for this account yet; sync the connection and retry"))
        return Reading(t.string("currency"),t.string("amount"),host.now)
    }
    override val listsFlows=true
    /** CONTRIBUTION (in) and WITHDRAWAL (out) activities since `since`, offset-paged; the activity's own currency, `trade_date` else `settlement_date`. */
    override suspend fun flows(host: BrokerHost,id: String,since: java.time.LocalDate): List<BrokerFlow> {
        val out=mutableListOf<BrokerFlow>(); var offset=0
        while(true) {
            val page=json.parseToJsonElement(snap(host,"/accounts/$id/activities",params="&startDate=$since&type=CONTRIBUTION,WITHDRAWAL&limit=1000&offset=$offset")).jsonObject
            val items=page.list("data")
            for(a in items) {
                val withdrawal=when(a.text("type")) { "CONTRIBUTION" -> false; "WITHDRAWAL" -> true; else -> continue }
                val amount=a.string("amount").toBigDecimalOrNull()?.abs() ?: throw ProviderFailure(tr("Invalid account value"))
                val currency=a.child("currency")?.text("code") ?: throw ProviderFailure(tr("Missing {0} in provider response","currency"))
                out += BrokerFlow(java.time.LocalDate.parse((a.text("trade_date") ?: a.string("settlement_date")).take(10)),(if(withdrawal) amount.negate() else amount).toPlainString(),currency,a.string("id"))
            }
            offset += items.size
            if(items.isEmpty() || offset>=(page.child("pagination")?.text("total")?.toIntOrNull() ?: 0)) break
        }
        return out.sortedBy { it.date }
    }
}
