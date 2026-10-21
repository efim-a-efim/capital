package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import java.time.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** OANDA v20 REST: the account summary's NAV in the account's currency (fxTrade live accounts). */
object Oanda: BrokerPlugin {
    override val name="OANDA"
    override val site="https://www.oanda.com"
    override val credentials=listOf(Credential("OANDA") { tr("Access token: {0}","OANDA") })
    override val idForm=Regex("[0-9]{3}-[0-9]{3}-[0-9]{1,12}-[0-9]{3}")
    override fun idLabel()=tr("OANDA account id")
    override fun idError()=tr("Enter the OANDA account id, for example 001-001-1234567-001")
    private const val base="https://api-fxtrade.oanda.com/v3"
    private fun auth(host: BrokerHost)=mapOf("Authorization" to "Bearer ${host.required(name,name)}")
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=host.json("$base/accounts/$id/summary",headers=auth(host))["account"]?.takeIf { it !is JsonNull }?.jsonObject ?: throw ProviderFailure(tr("Missing {0} in provider response","account"))
        require(a.string("id")==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("NAV"),host.now)
    }
    override val listsFlows=true
    /** TRANSFER_FUNDS transactions: the index call lists id ranges per window of at most 365 days, then each range is read. Amounts are signed, in the account's home currency. */
    override suspend fun flows(host: BrokerHost,id: String,since: LocalDate): List<BrokerFlow> {
        val h=auth(host); val currency=read(host,id).currency; val out=mutableListOf<BrokerFlow>()
        val now=java.time.Instant.ofEpochMilli(host.now).truncatedTo(java.time.temporal.ChronoUnit.SECONDS); val today=now.atZone(java.time.ZoneOffset.UTC).toLocalDate()
        var from=since
        while(from<=today) {
            val to=minOf(from.plusDays(364),today)
            val index=host.json("$base/accounts/$id/transactions?from=${from}T00:00:00Z&to=${if(to==today) now.toString() else "${to}T23:59:59Z"}&type=TRANSFER_FUNDS&pageSize=1000",headers=h)
            for(page in (index["pages"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.content }) {
                // Pages are ready-made idrange URLs; only their id bounds are taken, the request is built here.
                val (first,last)=Regex("from=([0-9]+)&to=([0-9]+)").find(page)?.destructured ?: throw ProviderFailure(tr("Invalid provider response"))
                host.json("$base/accounts/$id/transactions/idrange?from=$first&to=$last&type=TRANSFER_FUNDS",headers=h).list("transactions").filter { it.text("type")=="TRANSFER_FUNDS" }.forEach { t ->
                    out += BrokerFlow(LocalDate.parse(t.string("time").take(10)),t.string("amount"),currency,t.string("id"))
                }
            }
            from=to.plusDays(1)
        }
        return out
    }
}
