package dev.capital.brokers

import dev.capital.data.string
import dev.capital.domain.tr

/** Alpaca Trading API (live): the account's equity, read with the user's API key id and secret. One account per key. */
object Alpaca: BrokerPlugin {
    override val name="Alpaca"
    override val site="https://alpaca.markets"
    override val credentials=listOf(Credential("Alpaca") { tr("API key: {0}","Alpaca") },Credential("Alpaca secret") { tr("API secret: {0}","Alpaca") })
    override val idForm=Regex("[A-Z0-9]{1,30}")
    override fun idLabel()=tr("Account number")
    override fun idError()=tr("Enter the account number shown by {0}","Alpaca")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private suspend fun account(host: BrokerHost)=host.json("https://api.alpaca.markets/v2/account",headers=mapOf("APCA-API-KEY-ID" to host.required("Alpaca",name),"APCA-API-SECRET-KEY" to host.required("Alpaca secret",name)))
    override suspend fun accounts(host: BrokerHost)=account(host).let { a -> listOf(a.string("account_number").uppercase() to label(a.string("account_number"),a.text("account_number"),a.text("currency"))) }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val a=account(host)
        require(a.string("account_number").uppercase()==id) { tr("Account mismatch") }
        return Reading(a.string("currency"),a.string("equity"),host.now)
    }
}
