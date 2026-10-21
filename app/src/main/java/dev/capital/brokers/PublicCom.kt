package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.time.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Public.com Individual API: total account value, with the long-lived secret key traded for a short access token per refresh. Amounts are USD. */
object PublicCom: BrokerPlugin {
    override val name="Public.com"
    override val site="https://public.com"
    override val credentials=listOf(Credential("Public.com") { tr("Secret key: {0}","Public.com") })
    override val idForm=Regex("[A-Za-z0-9_-]{1,64}")
    override fun idLabel()=tr("Account id")
    override fun idError()=tr("Choose an account or enter its id")
    override val listsAccounts=true
    private const val base="https://api.public.com"
    private suspend fun auth(host: BrokerHost): Map<String,String> {
        val body=buildJsonObject { put("validityInMinutes",5); put("secret",host.required(name,name)) }.toString()
        return mapOf("Authorization" to "Bearer ${host.json("$base/userapiauthservice/personal/access-tokens",body).string("accessToken")}")
    }
    override suspend fun accounts(host: BrokerHost)=host.json("$base/userapigateway/trading/account",headers=auth(host)).list("accounts")
        .mapNotNull { a -> a.text("accountId")?.let { it to label(it,a.text("accountType"),a.text("brokerageAccountType"),it) } }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val p=host.json("$base/userapigateway/trading/$id/portfolio/v2",headers=auth(host))
        p.text("accountId")?.let { require(it==id) { tr("Account mismatch") } }
        val total=p.text("totalAccountValue") ?: p.list("equity").takeIf { it.isNotEmpty() }?.sumOf { it.text("value")?.toBigDecimalOrNull() ?: throw ProviderFailure(tr("Invalid account value")) }?.toPlainString()
            ?: throw ProviderFailure(tr("Missing {0} in provider response","totalAccountValue"))
        return Reading("USD",total,host.now)
    }
    override val listsFlows=true
    /** MONEY_MOVEMENT events with sub-type DEPOSIT or WITHDRAWAL from the account history, paged by `nextToken`; `direction` gives the sign when present. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val h=auth(host); val out=mutableListOf<BrokerFlow>(); var token: String?=null; var pages=0
        do {
            val page=host.json("$base/userapigateway/trading/$id/history?start=${since}T00:00:00Z&pageSize=100${token?.let { "&nextToken=${java.net.URLEncoder.encode(it,"UTF-8")}" }.orEmpty()}",headers=h)
            for(t in page.list("transactions")) {
                if(t.text("type")!="MONEY_MOVEMENT") continue
                val outgoing=when(t.text("direction") ?: t.text("subType")) { "INCOMING","DEPOSIT" -> false; "OUTGOING","WITHDRAWAL" -> true; else -> continue }
                if(t.text("subType") !in setOf("DEPOSIT","WITHDRAWAL")) continue
                val amount=t.string("netAmount").toBigDecimalOrNull()?.abs() ?: throw ProviderFailure(tr("Invalid account value"))
                out += BrokerFlow(LocalDate.parse(t.string("timestamp").take(10)),(if(outgoing) amount.negate() else amount).toPlainString(),"USD",t.string("id"))
            }
            token=page.text("nextToken")?.takeIf { it.isNotBlank() }
        } while(token!=null && ++pages<100)
        return out.sortedBy { it.date }
    }
}
