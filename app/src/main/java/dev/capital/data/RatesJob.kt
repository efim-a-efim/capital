package dev.capital.data

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import dev.capital.domain.Chain
import dev.capital.domain.Portfolio
import dev.capital.domain.Quote
import dev.capital.domain.Series
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import java.time.Instant
import java.time.ZoneOffset

/** What the background job may know: asset codes and provider names, nothing else. */
@Serializable data class RatesSnapshot(val assets: List<String>, val providers: Map<String, String>)
@Serializable data class CachedRate(val asset: String, val usd: String, val observedAt: Long, val fetchedAt: Long, val source: String)

const val RATES_JOB_ID = 1001
const val RATES_CACHE_MAX = 2000
private const val TAG = "RatesJob"

/** Cached samples merged in: a newer sample replaces the stored quote; every sample becomes a `rate:<asset>` point on its UTC day. */
fun Portfolio.withCachedRates(cached: List<CachedRate>): Portfolio {
    if (cached.isEmpty()) return this
    val ordered = cached.filter { it.asset != "USD" }.sortedBy { it.observedAt }
    val stored = quotes.associateBy { it.asset }
    val newest = ordered.associateBy { it.asset }
    val fresh = newest.filter { (asset, c) -> (stored[asset]?.observedAt ?: 0L) < c.observedAt }.mapValues { (_, c) -> Quote(c.asset, c.usd, c.source, c.observedAt, c.fetchedAt) }
    val points = ordered.groupBy({ "rate:${it.asset}" }, { Instant.ofEpochMilli(it.observedAt).atOffset(ZoneOffset.UTC).toLocalDate().toString() to it.usd })
    val kept = history.map { s -> points[s.key]?.let { s.copy(points = (s.points + it).let { p -> if (p.size > 4000) p.toSortedMap().entries.drop(p.size - 4000).associate { e -> e.key to e.value } else p }) } ?: s }
    val added = (points.keys - history.map { it.key }.toSet()).map { Series(it, points.getValue(it).toMap()) }
    return copy(quotes = (stored + fresh).values.toList(), history = kept + added)
}

/** Appends successful quotes to the cache file; keeps the newest [RATES_CACHE_MAX] entries. */
fun appendCache(file: File, quotes: List<Quote>) {
    val old = readCache(file)
    file.writeText(json.encodeToString((old + quotes.filter { it.error == null }.map { CachedRate(it.asset, it.usd, it.observedAt, it.fetchedAt, it.source) }).takeLast(RATES_CACHE_MAX)))
}
fun readCache(file: File): List<CachedRate> = if (file.exists()) runCatching { json.decodeFromString<List<CachedRate>>(file.readText()) }.getOrElse { Log.w(TAG, "unreadable cache", it); emptyList() } else emptyList()

/** Asset list and provider choices for the next job run: the same set a refresh values. */
fun snapshot(context: Context, data: Portfolio) {
    val assets = (data.holdings.map { it.asset } + data.accounts.mapNotNull { it.asset } + data.buckets.map { it.currency } + data.goals.map { it.currency } + data.settings.currency).toSet() - "USD"
    runCatching { File(context.filesDir, "rates-job.json").writeText(json.encodeToString(RatesSnapshot(assets.sorted(), data.settings.providers.filterKeys { it == "Crypto" || it == "Fiat" }))) }
        .onFailure { Log.w(TAG, "snapshot failed", it) }
}
fun cacheFile(context: Context) = File(context.filesDir, "rates-cache.json")

object RatesSchedule {
    val options = listOf("Off", "24", "12", "6", "3", "1")
    private fun prefs(context: Context) = context.getSharedPreferences("capital-ui", Context.MODE_PRIVATE)
    fun choice(context: Context): String = prefs(context).getString("rates-hours", null)?.takeIf { it in options } ?: "Off"
    fun choose(context: Context, choice: String) { prefs(context).edit().putString("rates-hours", choice).apply(); apply(context, choice.toIntOrNull()) }
    fun last(context: Context): Long? = prefs(context).getLong("rates-last", 0).takeIf { it > 0 }
    fun touch(context: Context) { prefs(context).edit().putLong("rates-last", System.currentTimeMillis()).apply() }
    fun apply(context: Context, hours: Int?) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        if (hours == null) return scheduler.cancel(RATES_JOB_ID)
        val period = hours * 3_600_000L
        // Re-scheduling the same job resets its period; an unchanged pending job is left alone.
        if (scheduler.getPendingJob(RATES_JOB_ID)?.intervalMillis == period) return
        scheduler.schedule(JobInfo.Builder(RATES_JOB_ID, ComponentName(context, RatesJob::class.java)).setPeriodic(period).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).build())
    }
}

/** Fetches the current quotes of the snapshot's assets into the cache; never touches the data folder. */
class RatesJob : JobService() {
    private var job: Job? = null
    override fun onStartJob(params: JobParameters): Boolean {
        job = CoroutineScope(Dispatchers.IO).launch {
            try { run() } catch (e: CancellationException) { throw e } catch (e: Exception) { Log.w(TAG, "rates update failed", e) }
            jobFinished(params, false)
        }
        return true
    }
    override fun onStopJob(params: JobParameters): Boolean { job?.cancel(); return true }
    private suspend fun run() {
        val file = File(filesDir, "rates-job.json")
        if (!file.exists()) { Log.i(TAG, "no snapshot"); return }
        val snap = json.decodeFromString<RatesSnapshot>(file.readText())
        val native = Chain.entries.map { it.name }.toSet()
        val providers = Providers(Secrets(applicationContext)::get)
        val onError: (String, Exception) -> Unit = { asset, e -> Log.w(TAG, "$asset: ${e.message}") }
        val quotes = providers.crypto(snap.assets.filter { it in native }.toSet(), snap.providers.getValue("Crypto"), onError) + providers.fiat(snap.assets.filterNot { it in native }.toSet(), snap.providers.getValue("Fiat"), onError)
        appendCache(cacheFile(applicationContext), quotes)
        Log.i(TAG, "cached ${quotes.size} of ${snap.assets.size} quotes")
    }
}
