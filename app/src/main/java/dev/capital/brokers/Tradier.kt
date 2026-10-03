package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

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
    override suspend fun accounts(host: BrokerHost): List<Pair<String,String>> {
        val profile=host.json("https://api.tradier.com/v1/user/profile",headers=auth(host)).child("profile") ?: return emptyList()
        // One account comes as an object, several as an array.
        val list=when(val a=profile["account"]) { is JsonArray -> a.mapNotNull { it as? JsonObject }; is JsonObject -> listOf(a); else -> emptyList() }
        return list.filter { it.text("status")!="closed" }.mapNotNull { a -> a.text("account_number")?.uppercase()?.let { it to label(it,it,a.text("classification"),a.text("type")) } }
    }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val body=host.json("https://api.tradier.com/v1/accounts/$id/balances",headers=auth(host))
        // The reference example nests the figures under balance.balances, the schema under balances.
        val b=body.child("balance")?.child("balances") ?: body.child("balances") ?: body.child("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balances"))
        b.text("account_number")?.let { require(it.uppercase()==id) { tr("Account mismatch") } }
        return Reading("USD",b.text("total_equity") ?: throw ProviderFailure(tr("Missing {0} in provider response","total_equity")),host.now)
    }
}
