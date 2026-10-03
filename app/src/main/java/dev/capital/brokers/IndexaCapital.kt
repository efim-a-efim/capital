package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.time.LocalDate
import java.time.ZoneOffset

/** Indexa Capital REST API: portfolio total amount with the read-only token from the private area. */
object IndexaCapital: BrokerPlugin {
    override val name="Indexa Capital"
    override val site="https://indexacapital.com"
    override val credentials=listOf(Credential("Indexa Capital") { tr("Access token: {0}","Indexa Capital") })
    override val idForm=Regex("[A-Z0-9]{4,20}")
    override fun idLabel()=tr("Account number")
    override fun idError()=tr("Enter the account number shown by {0}","Indexa Capital")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private const val base="https://api.indexacapital.com"
    private fun auth(host: BrokerHost)=mapOf("X-AUTH-TOKEN" to host.required(name,name),"Accept" to "application/json")
    override suspend fun accounts(host: BrokerHost)=host.json("$base/users/me",headers=auth(host)).list("accounts")
        .filter { it.text("status")?.startsWith("cancel")!=true }.mapNotNull { a -> a.text("account_number")?.uppercase()?.let { it to label(it,it,a.text("type"),a.text("status")) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val currency=host.json("$base/accounts/$id",headers=auth(host)).let { it.text("currency") ?: it.child("account")?.text("currency") } ?: throw ProviderFailure(tr("Missing {0} in provider response","currency"))
        val p=host.json("$base/accounts/$id/portfolio",headers=auth(host)).child("portfolio") ?: throw ProviderFailure(tr("Missing {0} in provider response","portfolio"))
        p.text("account_number")?.let { require(it.uppercase()==id) { tr("Account mismatch") } }
        // The valuation date; Indexa values funds once per business day.
        val observed=p.text("date")?.let { runCatching { LocalDate.parse(it).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull() } ?: host.now
        return Reading(currency.uppercase(),p.string("total_amount"),observed)
    }
}
