package dev.capital.brokers

import dev.capital.data.KeyArgument
import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.*

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
}
