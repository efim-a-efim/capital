package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.domain.tr
import java.time.LocalDate
import java.time.ZoneOffset
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
    private fun headers(host: BrokerHost)=mapOf("x-api-key" to host.required("eToro",name),"x-user-key" to host.required("eToro user key",name),"x-request-id" to UUID.randomUUID().toString())
    private suspend fun balances(host: BrokerHost)=host.json("https://public-api.etoro.com/api/v1/balances?includeSubAccounts=true",headers=headers(host)).list("balances")
    override suspend fun accounts(host: BrokerHost)=balances(host).mapNotNull { a -> a.text("accountId")?.let { it to label(it,a.text("accountType"),a.text("subType"),a.text("currency"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=balances(host).find { it.text("accountId")==id } ?: throw ProviderFailure(tr("Account not found; choose it again"))
        return Reading(a.text("currency") ?: throw ProviderFailure(tr("Missing {0} in provider response","currency")),a.text("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balance")),host.now)
    }
    override val listsHistory=true
    /** End-of-day balance snapshots of the last 12 months (the API keeps no more): `snapshots[].accountSnapshots[]` of the chosen account, `total` in its native `currency`. */
    override suspend fun history(host: BrokerHost,id: String,since: LocalDate): List<Pair<LocalDate,Reading>> {
        val today=LocalDate.now(); val from=maxOf(since,today.minusDays(364))
        val h=host.json("https://public-api.etoro.com/api/v1/balances/history?fromDate=$from&toDate=$today&includeSubAccounts=true",headers=headers(host))
        return h.list("snapshots").mapNotNull { s ->
            val d=s.text("date")?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return@mapNotNull null
            val a=s.list("accountSnapshots").find { it.text("accountId")==id } ?: return@mapNotNull null
            val total=a.text("total") ?: return@mapNotNull null
            if(d.isBefore(since)) null else d to Reading(a.text("currency") ?: return@mapNotNull null,total,d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        }.groupBy { it.first }.map { (_,l) -> l.last() }.sortedBy { it.first }
    }
}
