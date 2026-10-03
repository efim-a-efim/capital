package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject

/** OANDA v20 REST: the account summary's NAV in the account's currency (fxTrade live accounts). */
object Oanda: BrokerPlugin {
    override val name="OANDA"
    override val site="https://www.oanda.com"
    override val credentials=listOf(Credential("OANDA") { tr("Access token: {0}","OANDA") })
    override val idForm=Regex("[0-9]{3}-[0-9]{3}-[0-9]{1,12}-[0-9]{3}")
    override fun idLabel()=tr("OANDA account id")
    override fun idError()=tr("Enter the OANDA account id, for example 001-001-1234567-001")
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=host.json("https://api-fxtrade.oanda.com/v3/accounts/$id/summary",headers=mapOf("Authorization" to "Bearer ${host.required(name,name)}"))["account"]?.takeIf { it !is JsonNull }?.jsonObject ?: throw ProviderFailure(tr("Missing {0} in provider response","account"))
        require(a.string("id")==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("NAV"),host.now)
    }
}
