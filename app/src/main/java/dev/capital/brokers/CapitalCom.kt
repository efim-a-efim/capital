package dev.capital.brokers

import dev.capital.data.KeyArgument
import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import java.time.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/** Capital.com Public API: account balance (deposit plus profit and loss), with an API key, the login e-mail and the key's own password; a 10-minute session per refresh. */
object CapitalCom: BrokerPlugin {
    override val name="Capital.com"
    override val site="https://capital.com"
    override val credentials=listOf(Credential("Capital.com") { tr("API key: {0}","Capital.com") },Credential("Capital.com login") { tr("Login e-mail: {0}","Capital.com") },Credential("Capital.com key password") { tr("API key password: {0}","Capital.com") })
    override val idForm=Regex("[0-9]{1,30}")
    override fun idLabel()=tr("Account id")
    override fun idError()=tr("Choose an account or enter its id")
    override val listsAccounts=true
    private const val base="https://api-capital.backend-capital.com/api/v1"
    /** Session tokens as request headers, plus the account the session is on (`currentAccountId`, null when the body does not say). */
    private class Session(val headers: Map<String,String>,val current: String?)
    private suspend fun session(host: BrokerHost): Session {
        val body=buildJsonObject { put("identifier",host.required("Capital.com login",name)); put("password",host.required("Capital.com key password",name)); put("encryptedPassword",false) }.toString()
        val r=host.send("$base/session",body,mapOf("X-CAP-API-KEY" to host.required("Capital.com",name)))
        val cst=r.header("CST"); val token=r.header("X-SECURITY-TOKEN")
        if(cst.isNullOrBlank() || token.isNullOrBlank()) throw KeyArgument(tr("{0} did not open a session; check the API key, login and key password",name))
        return Session(mapOf("CST" to cst,"X-SECURITY-TOKEN" to token),runCatching { json.parseToJsonElement(r.body).jsonObject.text("currentAccountId") }.getOrNull())
    }
    private suspend fun list(host: BrokerHost)=host.json("$base/accounts",headers=session(host).headers).list("accounts")
    override suspend fun accounts(host: BrokerHost)=list(host).mapNotNull { a -> a.text("accountId")?.let { it to label(it,a.text("accountName"),a.text("accountType"),a.text("currency"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=list(host).find { it.text("accountId")==id } ?: throw ProviderFailure(tr("Account not found; choose it again"))
        // balance already includes open profit and loss (deposit + profitLoss).
        val b=a.child("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balance"))
        return Reading(a.text("currency") ?: throw ProviderFailure(tr("Missing {0} in provider response","currency")),b.text("balance") ?: throw ProviderFailure(tr("Missing {0} in provider response","balance")),host.now)
    }
    override val listsFlows=true
    /** PROCESSED DEPOSIT and WITHDRAWAL rows of the transaction history of the session's account. The default window is 10 minutes, so `from` and `to` are always sent; `type` takes one value, so rows are filtered here. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val s=session(host)
        // ponytail: BrokerHost has no PUT, so the session cannot switch accounts (PUT /session {accountId}); only the account the session opens on is readable.
        if(s.current!=null && s.current!=id) throw ProviderFailure(tr("{0} lists transactions of the preferred account only; make this account preferred at the broker",name))
        val today=java.time.Instant.ofEpochMilli(host.now).atZone(java.time.ZoneOffset.UTC).toLocalDate()
        return host.json("$base/history/transactions?from=${since}T00:00:00&to=${today}T23:59:59",headers=s.headers).list("transactions").mapNotNull { t ->
            val withdrawal=when(t.text("transactionType")) { "DEPOSIT" -> false; "WITHDRAWAL" -> true; else -> return@mapNotNull null }
            if(t.text("status")?.let { it!="PROCESSED" }==true) return@mapNotNull null
            val amount=t.string("size").toBigDecimalOrNull()?.abs() ?: throw ProviderFailure(tr("Invalid account value"))
            BrokerFlow(LocalDate.parse((t.text("dateUtc") ?: t.string("date")).take(10)),(if(withdrawal) amount.negate() else amount).toPlainString(),t.string("currency"),t.text("reference") ?: t.string("transactionId"))
        }.filter { it.date>=since }.sortedBy { it.date }
    }
}
