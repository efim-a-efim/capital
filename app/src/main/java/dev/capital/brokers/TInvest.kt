package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add

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
    /** MoneyValue: integer units plus nano (billionths); both carry the sign. */
    private fun money(m: JsonObject): BigDecimal {
        val units=m.text("units")?.toBigDecimalOrNull() ?: BigDecimal.ZERO; val nano=m.text("nano")?.toLongOrNull() ?: 0
        if(nano !in -999_999_999..999_999_999) throw ProviderFailure(tr("Invalid account value"))
        return units.add(BigDecimal.valueOf(nano,9))
    }
    override suspend fun accounts(host: BrokerHost)=host.json("$base.UsersService/GetAccounts","""{"status":"ACCOUNT_STATUS_OPEN"}""",auth(host)).list("accounts")
        .mapNotNull { a -> a.text("id")?.let { it to label(it,a.text("name"),a.text("type")?.removePrefix("ACCOUNT_TYPE_"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val p=host.json("$base.OperationsService/GetPortfolio","""{"accountId":"$id","currency":"RUB"}""",auth(host))
        p.text("accountId")?.let { require(it==id) { tr("Account mismatch") } }
        val m=p.child("totalAmountPortfolio") ?: throw ProviderFailure(tr("Missing {0} in provider response","totalAmountPortfolio"))
        return Reading(m.string("currency").uppercase(),money(m).toPlainString(),host.now)
    }
    override val listsFlows=true
    /** Executed cash deposits (INPUT*, INP_MULTI) and withdrawals (OUTPUT*, OUT_MULTI) from GetOperationsByCursor; the sign of `payment` is undocumented, so the type decides it. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val out=mutableListOf<BrokerFlow>(); var cursor=""; var pages=0; var more: Boolean
        do {
            val body=buildJsonObject {
                put("accountId",id); put("from","${since}T00:00:00Z"); put("limit",1000); put("cursor",cursor); put("state","OPERATION_STATE_EXECUTED")
                putJsonArray("operationTypes") { listOf("INPUT","OUTPUT","INPUT_SWIFT","OUTPUT_SWIFT","INPUT_ACQUIRING","OUTPUT_ACQUIRING","INP_MULTI","OUT_MULTI").forEach { add("OPERATION_TYPE_$it") } }
            }.toString()
            val p=host.json("$base.OperationsService/GetOperationsByCursor",body,auth(host))
            for(o in p.list("items")) {
                val type=o.text("type")?.removePrefix("OPERATION_TYPE_") ?: continue
                val outgoing=if(type.startsWith("INP")) false else if(type.startsWith("OUT")) true else continue
                val m=o.child("payment") ?: throw ProviderFailure(tr("Missing {0} in provider response","payment"))
                val amount=money(m).abs()
                out += BrokerFlow(LocalDate.parse(o.string("date").take(10)),(if(outgoing) amount.negate() else amount).toPlainString(),m.string("currency").uppercase(),o.string("id"))
            }
            cursor=p.text("nextCursor").orEmpty(); more=p.text("hasNext")=="true" && cursor.isNotBlank()
        } while(more && ++pages<100)
        return out.sortedBy { it.date }
    }
}
