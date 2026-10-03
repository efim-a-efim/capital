package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.math.BigDecimal

/** T-Invest API (T-Bank, formerly Tinkoff), REST gateway: total portfolio value with a read-only token (valid three months from its last use). */
object TInvest: BrokerPlugin {
    override val name="T-Invest"
    override val site="https://www.tbank.ru/invest/"
    override val credentials=listOf(Credential("T-Invest") { tr("Access token: {0}","T-Invest") })
    override val idForm=Regex("[A-Za-z0-9_-]{1,40}")
    override fun idLabel()=tr("Account id")
    override fun idError()=tr("Choose an account or enter its id")
    override val listsAccounts=true
    private const val base="https://invest-public-api.tbank.ru/rest/tinkoff.public.invest.api.contract.v1"
    private fun auth(host: BrokerHost)=mapOf("Authorization" to "Bearer ${host.required(name,name)}")
    override suspend fun accounts(host: BrokerHost)=host.json("$base.UsersService/GetAccounts","""{"status":"ACCOUNT_STATUS_OPEN"}""",auth(host)).list("accounts")
        .mapNotNull { a -> a.text("id")?.let { it to label(it,a.text("name"),a.text("type")?.removePrefix("ACCOUNT_TYPE_"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val p=host.json("$base.OperationsService/GetPortfolio","""{"accountId":"$id","currency":"RUB"}""",auth(host))
        p.text("accountId")?.let { require(it==id) { tr("Account mismatch") } }
        val m=p.child("totalAmountPortfolio") ?: throw ProviderFailure(tr("Missing {0} in provider response","totalAmountPortfolio"))
        // MoneyValue: integer units plus nano (billionths); both carry the sign.
        val units=m.text("units")?.toBigDecimalOrNull() ?: BigDecimal.ZERO; val nano=m.text("nano")?.toLongOrNull() ?: 0
        if(nano !in -999_999_999..999_999_999) throw ProviderFailure(tr("Invalid account value"))
        return Reading(m.string("currency").uppercase(),units.add(BigDecimal.valueOf(nano,9)).toPlainString(),host.now)
    }
}
