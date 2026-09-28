package dev.capital.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Currency
import java.util.UUID

fun id() = UUID.randomUUID().toString()
val ZERO: BigDecimal = BigDecimal.ZERO
val HUNDRED: BigDecimal = BigDecimal(100)
fun String.decimal(): BigDecimal {
    require(length in 1..100 && matches(Regex("[0-9]+(\\.[0-9]{1,18})?"))) { "Enter a nonnegative number (up to 18 decimals)" }
    return toBigDecimal().also { require(it.precision() <= 60) { "Amount is too large" } }
}
fun BigDecimal.text(): String = stripTrailingZeros().toPlainString()
fun BigDecimal.divideMoney(other: BigDecimal) = divide(other, 18, RoundingMode.DOWN)
fun validAsset(asset: String): Boolean = asset in Chain.entries.map { it.name } || runCatching { Currency.getInstance(asset) }.isSuccess
fun assetLabel(asset: String) = if (asset == "TON") "TON / GRAM" else asset
@Serializable enum class Chain(val decimals: Int) { BTC(8), ETH(18), TON(9), TRX(6) }
@Serializable enum class Limit { AUTO, FIXED, BUCKET_PERCENT, GOAL_PERCENT }
@Serializable data class Bucket(val id: String = id(), val name: String, val currency: String = "EUR")
@Serializable data class Holding(
    val id: String = id(), val bucketId: String, val label: String, val asset: String,
    val quantity: String? = "0", val address: String? = null,
    val observedAt: Long? = null, val fetchedAt: Long? = null, val source: String = "Manual",
    val error: String? = null,
)
@Serializable data class Goal(
    val id: String = id(), val name: String, val target: String, val currency: String,
    val due: String, val priority: Int = 0, val archived: Boolean = false,
)
@Serializable data class Connection(
    val goalId: String, val bucketId: String, val mode: Limit = Limit.AUTO,
    val value: String = "0", val goalCap: String? = null,
) { val key: String get() = "$bucketId/$goalId" }
@Serializable data class Quote(
    val asset: String, val usd: String, val source: String, val observedAt: Long,
    val fetchedAt: Long, val error: String? = null,
)
val providerChoices = linkedMapOf(
    "BTC" to listOf("Blockstream", "mempool.space"),
    "ETH" to listOf("PublicNode", "Alchemy"),
    "TON" to listOf("TON Center", "TonAPI"),
    "TRX" to listOf("TronGrid", "PublicNode"),
    "Crypto" to listOf("CoinGecko", "CoinPaprika"),
    "Fiat" to listOf("Frankfurter", "ECB"),
)
@Serializable data class Settings(
    val currency: String = "EUR", val theme: String = "System",
    val providers: Map<String, String> = providerChoices.mapValues { it.value.first() },
)
@Serializable data class Portfolio(
    val buckets: List<Bucket> = emptyList(), val holdings: List<Holding> = emptyList(),
    val goals: List<Goal> = emptyList(), val connections: List<Connection> = emptyList(),
    val quotes: List<Quote> = emptyList(), val settings: Settings = Settings(),
) {
    fun validate(): Portfolio {
        require(buckets.size <= 1000 && holdings.size <= 10000 && goals.size <= 1000 && connections.size <= 10000 && quotes.size <= 1000) { "File exceeds personal portfolio limits" }
        fun unique(ids: List<String>) { require(ids.distinct().size == ids.size && ids.all { it.isNotBlank() && it.length <= 100 && '/' !in it }) { "Duplicate or invalid identifiers" } }
        unique(buckets.map { it.id }); unique(holdings.map { it.id }); unique(goals.map { it.id })
        require(validAsset(settings.currency) && settings.theme in listOf("System", "Light", "Dark")) { "Invalid settings" }
        require(settings.providers.keys == providerChoices.keys && settings.providers.all { (k,v) -> v in providerChoices.getValue(k) }) { "Unsupported provider" }
        val owners = mutableSetOf<String>()
        buckets.forEach { require(it.name.isNotBlank() && it.name.length <= 120 && validAsset(it.currency)) { "Invalid bucket" } }
        holdings.forEach { h ->
            require(buckets.any { it.id == h.bucketId } && h.label.isNotBlank() && h.label.length <= 120 && validAsset(h.asset)) { "Invalid holding" }
            h.quantity?.decimal()
            if (h.address != null) {
                val chain = Chain.valueOf(h.asset)
                require(owners.add("${chain.name}:${canonicalAddress(chain, h.address)}")) { "Wallet already belongs to a bucket" }
                h.quantity?.let { require(it.decimal().stripTrailingZeros().scale() <= chain.decimals) { "Invalid native precision" } }
            } else require(h.quantity != null) { "Manual balance required" }
        }
        goals.forEach {
            require(it.name.isNotBlank() && it.name.length <= 120 && it.target.decimal() > ZERO && validAsset(it.currency)) { "Invalid goal" }
            LocalDate.parse(it.due)
        }
        require(connections.map { it.key }.distinct().size == connections.size) { "Duplicate connection" }
        connections.forEach {
            require(buckets.any { b -> b.id == it.bucketId } && goals.any { g -> g.id == it.goalId }) { "Missing connection owner" }
            val v = it.value.decimal()
            if (it.mode in listOf(Limit.BUCKET_PERCENT, Limit.GOAL_PERCENT)) require(v <= HUNDRED) { "Percentage must be 0–100" }
            it.goalCap?.let { cap -> require(cap.decimal() <= HUNDRED) { "Goal cap must be 0–100" } }
        }
        require(quotes.map { it.asset }.distinct().size == quotes.size) { "Duplicate quote" }
        quotes.forEach { require(validAsset(it.asset) && it.usd.decimal() > ZERO && it.observedAt > 0 && it.fetchedAt > 0) { "Invalid quote" } }
        return this
    }
    fun price(asset: String): BigDecimal? = if (asset == "USD") BigDecimal.ONE else quotes.find { it.asset == asset }?.usd?.decimal()
    fun convert(amount: BigDecimal, from: String, to: String): BigDecimal? {
        if (from == to || amount.signum() == 0) return amount
        return price(from)?.let { f -> price(to)?.let { t -> amount.multiply(f).divideMoney(t) } }
    }
    fun bucketValue(bucketId: String, currency: String): BigDecimal = holdings.filter { it.bucketId == bucketId }.fold(ZERO) { sum,h -> sum + (h.quantity?.let { convert(it.decimal(), h.asset, currency) } ?: ZERO) }
    fun incomplete(currency: String) = holdings.any { it.quantity == null || convert(it.quantity.decimal(), it.asset, currency) == null }
    fun stale(now: Long = System.currentTimeMillis()): Boolean = holdings.any { it.error != null || (it.address != null && (it.fetchedAt == null || now - it.fetchedAt > 86_400_000)) } || quotes.any { it.error != null || now - it.observedAt > (if (it.asset in Chain.entries.map { c -> c.name }) 86_400_000L else 604_800_000L) }
    fun deleteBucket(id: String) = copy(buckets = buckets.filterNot { it.id == id }, holdings = holdings.filterNot { it.bucketId == id }, connections = connections.filterNot { it.bucketId == id })
    fun deleteGoal(id: String) = copy(goals = goals.filterNot { it.id == id }, connections = connections.filterNot { it.goalId == id })
}
fun baseQuantity(units: String, chain: Chain): String {
    require(units.length <= 80 && units.matches(Regex("[0-9]+"))) { "Invalid balance response" }
    return BigDecimal(BigInteger(units), chain.decimals).text()
}
