package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.time.LocalDate

/** Trading 212 Public API: the account summary's total value in the account's primary currency. */
object Trading212: BrokerPlugin {
    override val name="Trading 212"
    override val site="https://www.trading212.com"
    override val credentials=listOf(Credential("Trading 212") { tr("API key: Trading 212") },Credential("Trading 212 secret") { tr("API secret: Trading 212") })
    override val idForm=Regex("[0-9]{1,20}")
    override fun idLabel()=tr("Trading 212 account number")
    override fun idError()=tr("Enter the numeric Trading 212 account number")
    private const val base="https://live.trading212.com"
    private fun auth(host: BrokerHost)=mapOf("Authorization" to "Basic ${java.util.Base64.getEncoder().encodeToString("${host.required("Trading 212",name)}:${host.required("Trading 212 secret",name)}".toByteArray())}")
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=host.json("$base/api/v0/equity/account/summary",headers=auth(host))
        require(a.string("id")==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("totalValue"),host.now)
    }
    override val listsFlows=true
    /** DEPOSIT and WITHDRAW items of the transaction history, cursor-paged through `nextPagePath`. The sign of WITHDRAW amounts is undocumented, so the type decides it. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val h=auth(host); val out=mutableListOf<BrokerFlow>(); var currency: String?=null
        var path: String?="/api/v0/equity/history/transactions?time=${since}T00:00:00Z&limit=50"; var pages=0
        while(path!=null && pages++<200) {
            val page=host.json("$base$path",headers=h)
            for(t in page.list("items")) {
                val withdrawal=when(t.text("type")) { "DEPOSIT" -> false; "WITHDRAW" -> true; else -> continue }
                val date=LocalDate.parse(t.string("dateTime").take(10)); if(date<since) continue
                val amount=t.string("amount").toBigDecimalOrNull()?.abs() ?: throw ProviderFailure(tr("Invalid account value"))
                out += BrokerFlow(date,(if(withdrawal) amount.negate() else amount).toPlainString(),t.text("currency") ?: currency ?: read(host,id).currency.also { currency=it },t.string("reference"))
            }
            path=page.text("nextPagePath")?.takeIf { it.isNotBlank() }?.also { require(it.startsWith("/api/v0/")) { tr("Invalid provider response") } }
        }
        return out.sortedBy { it.date }
    }
}
