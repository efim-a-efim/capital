package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import java.time.Instant

/** Akahu (New Zealand aggregator, personal app): balance of a connected account such as Sharesies, Hatch, Kernel or a KiwiSaver scheme. */
object Akahu: BrokerPlugin {
    override val name="Akahu"
    override val site="https://www.akahu.nz"
    override val credentials=listOf(Credential("Akahu") { tr("User access token: {0}","Akahu") },Credential("Akahu app token") { tr("App ID token: {0}","Akahu") })
    override val idForm=Regex("acc_[a-z0-9]{20,40}")
    override fun idLabel()=tr("Account id")
    override fun idError()=tr("Choose an account or enter its id")
    override fun normalizeId(raw: String)=raw.trim().lowercase()
    override val listsAccounts=true
    private suspend fun list(host: BrokerHost)=host.json("https://api.akahu.io/v1/accounts",headers=mapOf("Authorization" to "Bearer ${host.required("Akahu",name)}","X-Akahu-Id" to host.required("Akahu app token",name))).list("items")
    override suspend fun accounts(host: BrokerHost)=list(host).mapNotNull { a -> a.text("_id")?.let { it to label(it,a.child("connection")?.text("name"),a.text("name"),a.text("type")) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=list(host).find { it.text("_id")==id } ?: throw ProviderFailure(tr("Account not found; choose it again"))
        val b=a.child("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balance"))
        val observed=a.child("refreshed")?.text("balance")?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() } ?: host.now
        return Reading(b.text("currency") ?: throw ProviderFailure(tr("Missing {0} in provider response","currency")),b.text("current") ?: throw ProviderFailure(tr("Missing {0} in provider response","current")),observed)
    }
}
