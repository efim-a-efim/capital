package dev.capital.brokers

import dev.capital.domain.tr
import kotlinx.serialization.json.JsonObject

/**
 * A broker integration. One object per broker, registered in [Brokers.all]; see BROKER-PLUGINS.md for the contract.
 * A plugin reads one account's total value with a long-lived credential the user creates at the broker; it never trades.
 */
interface BrokerPlugin {
    /** Unique, shown to the user and stored in snapshots; English, never translated. */
    val name: String
    /** Home page for the attribution list. */
    val site: String
    /** Credentials the user enters, the main token or key first. Keys are Secrets names and must be unique across plugins. */
    val credentials: List<Credential>
    /** Form of the account or query id after [normalizeId]. */
    val idForm: Regex
    /** Label of the id field. */
    fun idLabel(): String
    /** Message when the id does not match [idForm]. */
    fun idError(): String
    fun normalizeId(raw: String): String = raw.trim()
    /** True when [accounts] lists the user's accounts for a picker. */
    val listsAccounts: Boolean get() = false
    /** Accounts the broker lists: id to label. */
    suspend fun accounts(host: BrokerHost): List<Pair<String,String>> = emptyList()
    /** Message shown when [accounts] is empty. */
    fun noAccounts(): String = tr("No accounts found")
    /** Label of a button that opens the broker's authorization page in the browser, null when the plugin has none. */
    fun connectLabel(): String? = null
    /** URL for [connectLabel]; HTTPS only. */
    suspend fun connect(host: BrokerHost): String = throw UnsupportedOperationException(name)
    /** Total value of the account, read-only. Throw [dev.capital.data.ProviderFailure] with a translated message on any problem. */
    suspend fun read(host: BrokerHost, id: String): Reading
}
/** One stored credential: `key` names it in Secrets, `label` is shown in the Credentials section (call [tr] inside). */
data class Credential(val key: String, val label: () -> String)
/** What a plugin returns: ISO currency code, decimal total as text, and when the broker observed it (epoch ms). */
data class Reading(val currency: String, val total: String, val observedAt: Long)
data class Response(val status: Int, val body: String, val headers: Map<String,String>) {
    fun header(name: String): String? = headers[name.lowercase()]
}
/** Everything a plugin may touch: paced, retried HTTPS requests and the stored credentials. */
interface BrokerHost {
    /** Stored credential, blank when missing. */
    fun secret(key: String): String
    /** [secret] or a KeyArgument naming the broker, for the user to fix on the Brokers screen. */
    fun required(key: String, broker: String): String
    /** GET when `body` is null, POST with a JSON content type otherwise. Non-2xx statuses fail unless listed in `soft`. */
    suspend fun send(url: String, body: String? = null, headers: Map<String,String> = emptyMap(), soft: Set<Int> = emptySet()): Response
    suspend fun text(url: String, body: String? = null, headers: Map<String,String> = emptyMap()): String = send(url,body,headers).body
    suspend fun json(url: String, body: String? = null, headers: Map<String,String> = emptyMap()): JsonObject
    val now: Long get() = System.currentTimeMillis()
}
object Brokers {
    val all: List<BrokerPlugin> = listOf(InteractiveBrokers, Oanda, Trading212, SnapTrade)
    fun byName(name: String?): BrokerPlugin? = all.find { it.name == name }
    fun credential(key: String): Credential? = all.firstNotNullOfOrNull { p -> p.credentials.find { it.key == key } }
}
/** Text from a provider, safe to show: no control characters, bounded length. */
internal fun String.clean() = filterNot { it.isISOControl() }.trim().take(40)
