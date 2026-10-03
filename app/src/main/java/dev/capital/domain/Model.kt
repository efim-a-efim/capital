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
    require(length in 1..100 && matches(Regex("[0-9]+(\\.[0-9]{1,18})?"))) { tr("Enter a nonnegative number (up to 18 decimals)") }
    return toBigDecimal().also { require(it.precision() <= 60) { tr("Amount is too large") } }
}
fun BigDecimal.text(): String = stripTrailingZeros().toPlainString()
fun BigDecimal.divideMoney(other: BigDecimal) = divide(other, 18, RoundingMode.DOWN)
fun validAsset(asset: String): Boolean = asset in Chain.entries.map { it.name } || runCatching { Currency.getInstance(asset) }.isSuccess || validToken(asset)
private fun validToken(asset: String): Boolean {
    val (c, contract) = asset.split(":", limit = 2).takeIf { it.size == 2 } ?: return false
    val chain = Chain.entries.find { it.name == c }?.takeIf { it != Chain.BTC } ?: return false
    return runCatching { canonicalAddress(chain, contract) == contract }.getOrDefault(false)
}
fun tokenAsset(chain: String, contract: String) = "$chain:$contract"
fun assetLabel(asset: String) = if (asset == "TON") "TON / GRAM" else asset
@Serializable enum class Chain(val decimals: Int) { BTC(8), ETH(18), TON(9), TRX(6) }
@Serializable enum class Limit { AUTO, FIXED, BUCKET_PERCENT, GOAL_PERCENT }
@Serializable data class Bucket(
    val id: String = id(), val name: String, val currency: String = "EUR",
    val portfolio: Boolean = false, val targets: Map<String, String> = emptyMap(), val allowSells: Boolean = false,
)
@Serializable data class Holding(
    val id: String = id(), val bucketId: String, val label: String, val asset: String,
    val quantity: String? = "0", val address: String? = null,
    val observedAt: Long? = null, val fetchedAt: Long? = null, val source: String = "Manual",
    val error: String? = null, val tokens: List<Token> = emptyList(), val tokensError: String? = null,
    val excluded: List<String> = emptyList(),
    /** Schema 5 only: broker of an account holding. Snapshots are migrated to `accountId` on open; validate() rejects it. */
    val broker: String? = null,
    /** Link to a broker account; `asset`, `quantity`, times, `source` and `error` then mirror that account (see [Portfolio.linked]). */
    val accountId: String? = null,
)
/** A read-only connection to a broker account: `address` is the account or query id, `asset` and `quantity` its last total value in its base currency. */
@Serializable data class Account(
    val id: String = id(), val name: String, val broker: String, val address: String,
    val asset: String? = null, val quantity: String? = null, val observedAt: Long? = null, val fetchedAt: Long? = null, val error: String? = null,
    /** When set, a balance below this amount in the default currency counts as 0 in the linked bucket (dust left in forgotten accounts). */
    val ignoreBelow: String? = null,
)
val brokerChoices = listOf("Interactive Brokers", "OANDA", "Trading 212", "SnapTrade")
/** Credentials entered in Settings per broker; the first is the token or key, the rest its companions. Names are Secrets keys. */
val brokerCredentials = linkedMapOf(
    "Interactive Brokers" to listOf("Interactive Brokers"), "OANDA" to listOf("OANDA"),
    "Trading 212" to listOf("Trading 212", "Trading 212 secret"), "SnapTrade" to listOf("SnapTrade", "SnapTrade consumer key"),
)
private val accountIdForms = mapOf(
    "Interactive Brokers" to Regex("[0-9]{1,20}"), "OANDA" to Regex("[0-9]{3}-[0-9]{3}-[0-9]{1,12}-[0-9]{3}"), "Trading 212" to Regex("[0-9]{1,20}"),
    "SnapTrade" to Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
)
/** Trimmed id in the broker's own form: Flex Query id, OANDA account id, Trading 212 account number, SnapTrade account id. */
fun accountId(broker: String?, raw: String): String {
    val form = accountIdForms[broker] ?: throw IllegalArgumentException(tr("Choose a supported broker"))
    return raw.trim().let { if (broker == "SnapTrade") it.lowercase() else it }.also {
        require(form.matches(it)) { when (broker) {
            "OANDA" -> tr("Enter the OANDA account id, for example 001-001-1234567-001")
            "Trading 212" -> tr("Enter the numeric Trading 212 account number")
            "SnapTrade" -> tr("Choose a SnapTrade account or enter its id")
            else -> tr("Enter the numeric Flex Query id")
        } }
    }
}
@Serializable data class Token(
    val contract: String, val symbol: String = "", val name: String = "", val units: String,
    val decimals: Int? = null, val checkedAt: Long? = null,
) {
    fun quantity(): BigDecimal? = decimals?.takeIf { it in 0..36 }?.let { BigDecimal(BigInteger(units), it).setScale(18, RoundingMode.DOWN) }
}
@Serializable data class Goal(
    val id: String = id(), val name: String, val target: String, val currency: String,
    val due: String, val priority: Int = 0, val archived: Boolean = false,
)
@Serializable data class Planned(val id: String = id(), val name: String, val amount: String, val currency: String, val date: String)
fun Planned.archived(today: LocalDate = LocalDate.now()) = LocalDate.parse(date) < today
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
    "ETH tokens" to listOf("Blockscout", "Ethplorer", "Off"),
    "TON tokens" to listOf("TON Center", "TonAPI", "Off"),
    "TRX tokens" to listOf("TronGrid", "Off"),
    "Crypto" to listOf("DefiLlama", "CoinGecko", "CoinPaprika"),
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
    val planned: List<Planned> = emptyList(), val accounts: List<Account> = emptyList(),
) {
    fun validate(): Portfolio {
        require(buckets.size <= 1000 && holdings.size <= 10000 && goals.size <= 1000 && connections.size <= 10000 && quotes.size <= 5000 && planned.size <= 1000 && accounts.size <= 1000) { tr("File exceeds personal portfolio limits") }
        fun unique(ids: List<String>) { require(ids.distinct().size == ids.size && ids.all { it.isNotBlank() && it.length <= 100 && '/' !in it }) { tr("Duplicate or invalid identifiers") } }
        unique(buckets.map { it.id }); unique(holdings.map { it.id }); unique(goals.map { it.id }); unique(planned.map { it.id }); unique(accounts.map { it.id })
        require(buckets.none { it.id == PLANNED_BUCKET } && planned.none { it.id == PLANNED_BUCKET }) { tr("Duplicate or invalid identifiers") }
        require(validAsset(settings.currency) && settings.theme in listOf("System", "Light", "Dark")) { tr("Invalid settings") }
        require(settings.providers.keys == providerChoices.keys && settings.providers.all { (k,v) -> v in providerChoices.getValue(k) }) { tr("Unsupported provider") }
        val owners = mutableSetOf<String>()
        accounts.forEach { a ->
            require(a.name.isNotBlank() && a.name.length <= 120 && accountId(a.broker, a.address) == a.address) { tr("Invalid broker account") }
            require(a.asset == null || (a.asset.matches(Regex("[A-Z]{3}")) && validAsset(a.asset) && a.asset !in Chain.entries.map { it.name })) { tr("Invalid broker account") }
            a.quantity?.decimal(); a.ignoreBelow?.decimal()
            require(owners.add("${a.broker}:${a.address}")) { tr("Account already exists") }
        }
        buckets.forEach { require(it.name.isNotBlank() && it.name.length <= 120 && validAsset(it.currency)) { tr("Invalid bucket") }
            require(it.targets.size <= 200) { tr("At most 200 targets") }
            it.targets.forEach { (k, v) ->
                require(validAsset(k)) { tr("Invalid target asset") }
                require(v.matches(Regex("[0-9]{1,3}(\\.[0-9]{1,2})?")) && v.toBigDecimal() <= HUNDRED) { if (':' in k) tr("Target for a token must be 0–100 with up to two decimals") else tr("Target for {0} must be 0–100 with up to two decimals", k) }
            }
            if (it.portfolio) it.targets.values.fold(ZERO) { s, v -> s + v.toBigDecimal() }.let { sum -> require(sum.compareTo(HUNDRED) == 0) { tr("Targets must total 100%. Now {0}%.", sum.text()) } }
        }
        holdings.forEach { h ->
            require(buckets.any { it.id == h.bucketId } && h.label.isNotBlank() && h.label.length <= 120 && validAsset(h.asset)) { tr("Invalid holding") }
            h.quantity?.decimal()
            require(h.broker == null) { tr("Invalid account holding") }
            require((h.tokens.isEmpty() && h.excluded.isEmpty()) || (h.address != null && h.accountId == null && h.asset != Chain.BTC.name)) { tr("Tokens need an ETH, TON or TRX wallet") }
            if (h.accountId != null) {
                val a = accounts.find { it.id == h.accountId } ?: throw IllegalArgumentException(tr("Invalid account holding"))
                require(h.address == null && (h.quantity == a.quantity || (a.ignoreBelow != null && h.quantity == "0")) && (a.asset == null || h.asset == a.asset) && ':' !in h.asset && h.asset !in Chain.entries.map { it.name }) { tr("Invalid account holding") }
                require(owners.add("link:${a.id}")) { tr("Account already belongs to a bucket") }
            } else if (h.address != null) {
                val chain = Chain.valueOf(h.asset)
                require(h.tokens.size <= 100 && h.tokens.map { it.contract }.distinct().size == h.tokens.size) { tr("Invalid tokens") }
                fun canonical(c: String) = runCatching { canonicalAddress(chain, c) == c }.getOrDefault(false)
                require(h.excluded.size <= 1000 && h.excluded.distinct().size == h.excluded.size && h.excluded.all { canonical(it) }) { tr("Invalid excluded tokens") }
                h.tokens.forEach { t -> require(canonical(t.contract) && t.units.matches(Regex("[0-9]{1,80}")) && t.symbol.length <= 40 && t.name.length <= 40 && (t.decimals ?: 0) in 0..36) { tr("Invalid token") } }
                require(owners.add("${chain.name}:${canonicalAddress(chain, h.address)}")) { tr("Wallet already belongs to a bucket") }
                h.quantity?.let { require(it.decimal().stripTrailingZeros().scale() <= chain.decimals) { tr("Invalid native precision") } }
            } else require(h.quantity != null) { tr("Manual balance required") }
        }
        goals.forEach {
            require(it.name.isNotBlank() && it.name.length <= 120 && it.target.decimal() > ZERO && validAsset(it.currency)) { tr("Invalid goal") }
            LocalDate.parse(it.due)
        }
        planned.forEach {
            require(it.name.isNotBlank() && it.name.length <= 120 && it.amount.decimal() > ZERO && validAsset(it.currency) && ':' !in it.currency) { tr("Invalid planned saving") }
            LocalDate.parse(it.date)
        }
        require(connections.map { it.key }.distinct().size == connections.size) { tr("Duplicate connection") }
        connections.forEach {
            require(buckets.any { b -> b.id == it.bucketId } && goals.any { g -> g.id == it.goalId }) { tr("Missing connection owner") }
            val v = it.value.decimal()
            if (it.mode in listOf(Limit.BUCKET_PERCENT, Limit.GOAL_PERCENT)) require(v <= HUNDRED) { tr("Percentage must be 0–100") }
            it.goalCap?.let { cap -> require(cap.decimal() <= HUNDRED) { tr("Goal cap must be 0–100") } }
        }
        require(quotes.map { it.asset }.distinct().size == quotes.size) { tr("Duplicate quote") }
        quotes.forEach { require(validAsset(it.asset) && it.usd.decimal() > ZERO && it.observedAt > 0 && it.fetchedAt > 0) { tr("Invalid quote") } }
        return this
    }
    fun price(asset: String): BigDecimal? = if (asset == "USD") BigDecimal.ONE else quotes.find { it.asset == asset }?.usd?.decimal()
    fun convert(amount: BigDecimal, from: String, to: String): BigDecimal? {
        if (from == to || amount.signum() == 0) return amount
        return price(from)?.let { f -> price(to)?.let { t -> amount.multiply(f).divideMoney(t) } }
    }
    // ponytail: price() is a linear scan per token; index quotes by asset if portfolios reach thousands of tokens.
    fun known(h: Holding, t: Token): Boolean = t.contract !in h.excluded && t.quantity() != null && price(tokenAsset(h.asset, t.contract)) != null
    private fun tokenValue(h: Holding, currency: String): BigDecimal? = h.tokens.filter { known(h, it) }.fold(ZERO as BigDecimal?) { sum, t -> sum?.let { s -> convert(t.quantity()!!, tokenAsset(h.asset, t.contract), currency)?.let { s + it } } }
    fun bucketValue(bucketId: String, currency: String): BigDecimal = holdings.filter { it.bucketId == bucketId }.fold(ZERO) { sum,h -> sum + (h.quantity?.let { convert(it.decimal(), h.asset, currency) } ?: ZERO) + h.tokens.filter { known(h, it) }.fold(ZERO) { s, t -> s + (convert(t.quantity()!!, tokenAsset(h.asset, t.contract), currency) ?: ZERO) } }
    /** Null when something in the bucket cannot be converted and nothing else adds value; otherwise the (possibly partial) sum. */
    fun bucketValueOrNull(bucketId: String, currency: String): BigDecimal? = bucketValue(bucketId, currency).takeUnless { it.signum() == 0 && incomplete(currency, bucketId) }
    fun incomplete(currency: String, bucketId: String? = null) = holdings.any { (bucketId == null || it.bucketId == bucketId) && (it.quantity == null || convert(it.quantity.decimal(), it.asset, currency) == null || tokenValue(it, currency) == null) }
    /** Holdings linked to a broker account mirror its value, times and error; the currency falls back to the bucket's until the first refresh. */
    fun linked(): Portfolio = copy(holdings = holdings.map { h ->
        val a = h.accountId?.let { id -> accounts.find { it.id == id } } ?: return@map h
        h.copy(asset = a.asset ?: buckets.find { it.id == h.bucketId }?.currency?.takeIf { ':' !in it && it !in Chain.entries.map { c -> c.name } } ?: "USD", quantity = if (ignored(a)) "0" else a.quantity, address = null, broker = null,
            observedAt = a.observedAt, fetchedAt = a.fetchedAt, source = if (a.fetchedAt == null) "Not refreshed" else a.broker, error = a.error, tokens = emptyList(), tokensError = null, excluded = emptyList())
    })
    /** True when the account's last value, converted to the default currency, is below its threshold; unknown rates never ignore. */
    fun ignored(a: Account): Boolean {
        val floor = a.ignoreBelow?.decimal() ?: return false
        val value = a.quantity?.decimal() ?: return false
        return convert(value, a.asset ?: return false, settings.currency)?.let { it < floor } ?: false
    }
    fun stale(now: Long = System.currentTimeMillis()): Boolean {
        val used = holdings.flatMap { h -> h.tokens.filter { it.contract !in h.excluded }.map { tokenAsset(h.asset, it.contract) } }.toSet()
        return accounts.any { it.error != null || it.fetchedAt == null || now - it.fetchedAt > 86_400_000 } || holdings.any { it.error != null || it.tokensError != null || (it.address != null && (it.fetchedAt == null || now - it.fetchedAt > 86_400_000)) } ||
            quotes.any { (':' !in it.asset || it.asset in used) && (it.error != null || now - it.observedAt > (if (it.asset in Chain.entries.map { c -> c.name } || ':' in it.asset) 86_400_000L else 604_800_000L)) }
    }
    fun deleteBucket(id: String) = copy(buckets = buckets.filterNot { it.id == id }, holdings = holdings.filterNot { it.bucketId == id }, connections = connections.filterNot { it.bucketId == id })
    fun deleteAccount(id: String) = copy(accounts = accounts.filterNot { it.id == id }, holdings = holdings.filterNot { it.accountId == id })
    fun deleteGoal(id: String) = copy(goals = goals.filterNot { it.id == id }, connections = connections.filterNot { it.goalId == id })
}
// Active goals by (due, old priority desc, list index) get priority count..1; archived get 0. List order is untouched.
fun Portfolio.ranked(): Portfolio {
    val order = goals.withIndex().filter { !it.value.archived }.sortedWith(compareBy<IndexedValue<Goal>> { it.value.due }.thenByDescending { it.value.priority }.thenBy { it.index }).map { it.value.id }
    val rank = order.withIndex().associate { (i, id) -> id to order.size - i }
    return copy(goals = goals.map { it.copy(priority = rank[it.id] ?: 0) })
}
fun Portfolio.moveGoal(id: String, up: Boolean): Portfolio {
    val g = goals.find { it.id == id && !it.archived } ?: return this
    val row = goals.withIndex().filter { !it.value.archived && it.value.due == g.due }.sortedWith(compareByDescending<IndexedValue<Goal>> { it.value.priority }.thenBy { it.index }).map { it.value }
    val at = row.indexOfFirst { it.id == id }
    val other = row.getOrNull(if (up) at - 1 else at + 1) ?: return this
    return copy(goals = goals.map { when (it.id) { g.id -> it.copy(priority = other.priority); other.id -> it.copy(priority = g.priority); else -> it } }).ranked()
}
/** Schema 5 kept broker accounts inside holdings; each becomes a broker account plus a holding linked to it. */
fun Portfolio.withAccounts(): Portfolio {
    val moved = holdings.filter { it.broker != null }.associate { h -> h.id to Account(name = h.label, broker = h.broker!!, address = h.address.orEmpty(), asset = h.asset.takeIf { h.fetchedAt != null }, quantity = h.quantity, observedAt = h.observedAt, fetchedAt = h.fetchedAt, error = h.error) }
    return copy(accounts = accounts + moved.values, holdings = holdings.map { h -> moved[h.id]?.let { a -> h.copy(accountId = a.id, broker = null, address = null) } ?: h }).linked()
}
fun baseQuantity(units: String, chain: Chain): String {
    require(units.length <= 80 && units.matches(Regex("[0-9]+"))) { tr("Invalid balance response") }
    return BigDecimal(BigInteger(units), chain.decimals).text()
}
