package dev.capital.brokers

import dev.capital.data.string
import dev.capital.domain.tr

/** Trading 212 Public API: the account summary's total value in the account's primary currency. */
object Trading212: BrokerPlugin {
    override val name="Trading 212"
    override val site="https://www.trading212.com"
    override val credentials=listOf(Credential("Trading 212") { tr("API key: Trading 212") },Credential("Trading 212 secret") { tr("API secret: Trading 212") })
    override val idForm=Regex("[0-9]{1,20}")
    override fun idLabel()=tr("Trading 212 account number")
    override fun idError()=tr("Enter the numeric Trading 212 account number")
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val basic=java.util.Base64.getEncoder().encodeToString("${host.required("Trading 212",name)}:${host.required("Trading 212 secret",name)}".toByteArray())
        val a=host.json("https://live.trading212.com/api/v0/equity/account/summary",headers=mapOf("Authorization" to "Basic $basic"))
        require(a.string("id")==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("totalValue"),host.now)
    }
}
