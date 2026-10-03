package dev.capital.brokers

import dev.capital.data.ProviderFailure
import dev.capital.data.json
import dev.capital.data.string
import dev.capital.domain.tr
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/** ALOR Broker OpenAPI: portfolio valuation on the Moscow Exchange, with the one-year refresh token traded for a 30-minute access token per refresh. Values are in roubles. */
object Alor: BrokerPlugin {
    override val name="ALOR"
    override val site="https://www.alorbroker.ru"
    override val credentials=listOf(Credential("ALOR") { tr("Refresh token: {0}","ALOR") })
    override val idForm=Regex("[A-Z0-9]{1,20}")
    override fun idLabel()=tr("Portfolio")
    override fun idError()=tr("Enter the portfolio, for example D12345")
    override fun normalizeId(raw: String)=raw.trim().uppercase()
    override val listsAccounts=true
    private suspend fun token(host: BrokerHost)=host.json("https://oauth.alor.ru/refresh",buildJsonObject { put("token",host.required(name,name)) }.toString()).string("AccessToken")
    override suspend fun accounts(host: BrokerHost): List<Pair<String,String>> {
        // The access token's payload names the account's portfolios, separated by spaces.
        val payload=token(host).split('.').getOrNull(1) ?: throw ProviderFailure(tr("Invalid provider response"))
        val claims=runCatching { json.parseToJsonElement(String(java.util.Base64.getUrlDecoder().decode(payload.padEnd((payload.length+3)/4*4,'=')))).jsonObject }.getOrNull() ?: throw ProviderFailure(tr("Invalid provider response"))
        return claims.text("portfolios").orEmpty().split(' ').map { it.trim().uppercase() }.filter { idForm.matches(it) }.distinct().map { it to it }
    }
    override suspend fun read(host: BrokerHost,id: String): Reading {
        val s=host.json("https://api.alor.ru/md/v2/Clients/MOEX/$id/summary",headers=mapOf("Authorization" to "Bearer ${token(host)}"))
        return Reading("RUB",s.text("portfolioLiquidationValue") ?: s.text("portfolioEvaluation") ?: throw ProviderFailure(tr("Missing {0} in provider response","portfolioLiquidationValue")),host.now)
    }
}
