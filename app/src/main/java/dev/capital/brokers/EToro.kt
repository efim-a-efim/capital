package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import java.util.UUID

/** eToro Public API: per-account balance (total value including positions for trading accounts) with the user's public API key and user key. */
object EToro: BrokerPlugin {
    override val name="eToro"
    override val site="https://www.etoro.com"
    override val credentials=listOf(Credential("eToro") { tr("Public API key: {0}","eToro") },Credential("eToro user key") { tr("User key: {0}","eToro") })
    override val idForm=Regex("[A-Za-z0-9_-]{1,64}")
    override fun idLabel()=tr("Account id")
    override fun idError()=tr("Choose an account or enter its id")
    override val listsAccounts=true
    private suspend fun balances(host: BrokerHost)=host.json("https://public-api.etoro.com/api/v1/balances?includeSubAccounts=true",
        headers=mapOf("x-api-key" to host.required("eToro",name),"x-user-key" to host.required("eToro user key",name),"x-request-id" to UUID.randomUUID().toString())).list("balances")
    override suspend fun accounts(host: BrokerHost)=balances(host).mapNotNull { a -> a.text("accountId")?.let { it to label(it,a.text("accountType"),a.text("subType"),a.text("currency"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=balances(host).find { it.text("accountId")==id } ?: throw ProviderFailure(tr("Account not found; choose it again"))
        return Reading(a.text("currency") ?: throw ProviderFailure(tr("Missing {0} in provider response","currency")),a.text("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balance")),host.now)
    }
}
