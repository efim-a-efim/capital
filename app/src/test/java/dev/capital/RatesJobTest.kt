package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class RatesJobTest {
    private val day = 86_400_000L
    private val t0 = 1_700_000_000_000L // 2023-11-14T22:13:20Z
    private fun quote(asset: String, usd: String, at: Long) = Quote(asset, usd, "Frankfurter", at, at)
    private fun cached(asset: String, usd: String, at: Long) = CachedRate(asset, usd, at, at, "DefiLlama")

    @Test fun newerSamplesReplaceQuotesAndFillHistory() {
        val data = Portfolio(quotes = listOf(quote("EUR", "1.1", t0), quote("BTC", "40000", t0)), history = listOf(Series("rate:EUR", mapOf("2023-11-01" to "1.0"))))
        val merged = data.withCachedRates(listOf(
            cached("EUR", "1.2", t0 - day),      // older: quote kept, point added
            cached("BTC", "41000", t0 + 2 * day), // newer: quote replaced
            cached("BTC", "42000", t0 + 3 * day), // newest wins
            cached("CHF", "1.3", t0 + day),      // no stored quote: added
        ))
        assertEquals("1.1", merged.quotes.first { it.asset == "EUR" }.usd)
        assertEquals("Frankfurter", merged.quotes.first { it.asset == "EUR" }.source)
        assertEquals("42000", merged.quotes.first { it.asset == "BTC" }.usd)
        assertEquals("DefiLlama", merged.quotes.first { it.asset == "BTC" }.source)
        assertEquals("1.3", merged.quotes.first { it.asset == "CHF" }.usd)
        val history = merged.history.associate { it.key to it.points }
        assertEquals(mapOf("2023-11-01" to "1.0", "2023-11-13" to "1.2"), history["rate:EUR"])
        assertEquals(mapOf("2023-11-16" to "41000", "2023-11-17" to "42000"), history["rate:BTC"])
        assertEquals(mapOf("2023-11-15" to "1.3"), history["rate:CHF"])
        assertEquals(data, data.withCachedRates(emptyList()))
        merged.validate()
    }

    @Test fun pointsLandOnUtcDates() {
        val merged = Portfolio().withCachedRates(listOf(cached("EUR", "1", 1_700_006_399_000L), cached("EUR", "2", 1_700_006_400_000L))) // 23:59:59Z and 00:00:00Z
        assertEquals(mapOf("2023-11-14" to "1", "2023-11-15" to "2"), merged.history.single().points)
    }

    @Test fun snapshotRoundTrips() {
        val snap = RatesSnapshot(listOf("BTC", "EUR"), mapOf("Crypto" to "DefiLlama", "Fiat" to "Frankfurter"))
        assertEquals(snap, json.decodeFromString<RatesSnapshot>(json.encodeToString(snap)))
    }

    @Test fun cacheKeepsNewestEntries() {
        val dir = Files.createTempDirectory("rates").toFile()
        val file = File(dir, "rates-cache.json")
        assertEquals(emptyList<CachedRate>(), readCache(file))
        appendCache(file, (1..1500).map { quote("EUR", "$it", t0 + it) })
        appendCache(file, (1501..2100).map { quote("EUR", "$it", t0 + it) } + Quote("BTC", "1", "x", t0, t0, error = "down"))
        val kept = readCache(file)
        assertEquals(RATES_CACHE_MAX, kept.size)
        assertEquals("101", kept.first().usd)
        assertEquals("2100", kept.last().usd)
        assertTrue(kept.none { it.asset == "BTC" })
        dir.deleteRecursively()
    }
}
