package dev.capital.data

import dev.capital.domain.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.xml.parsers.DocumentBuilderFactory

private val media="application/json".toMediaType()
class ProviderFailure(message: String): IOException(message)
fun JsonObject.string(name: String) = get(name)?.jsonPrimitive?.contentOrNull ?: throw ProviderFailure("Missing $name in provider response")
fun parseBtc(body: JsonObject): String {
    val stats=body["chain_stats"]?.jsonObject ?: throw ProviderFailure("Missing confirmed balance")
    val funded=stats.string("funded_txo_sum").toBigInteger()
    val spent=stats.string("spent_txo_sum").toBigInteger()
    require(funded >= BigInteger.ZERO && spent >= BigInteger.ZERO && funded >= spent) { "Invalid confirmed balance" }
    return baseQuantity((funded-spent).toString(),Chain.BTC)
}
fun parseRpc(body: JsonObject): String {
    if(body["error"] != null) throw ProviderFailure("RPC rejected the request")
    return body.string("result").also { require(it.matches(Regex("0x[0-9a-fA-F]+")) && it.length <= 82) { "Invalid RPC response" } }
}
fun mergeObservations(current: Portfolio, requested: Portfolio, holdings: List<Holding>, quotes: List<Quote>): Portfolio {
    val results=holdings.associateBy { it.id }
    return current.copy(
        holdings=current.holdings.map { h ->
            val old=requested.holdings.find { it.id == h.id }; val result=results[h.id]
            if(old != null && result != null && old.address == h.address && old.asset == h.asset && current.settings.providers[h.asset] == requested.settings.providers[h.asset])
                h.copy(quantity=result.quantity,observedAt=result.observedAt,fetchedAt=result.fetchedAt,source=result.source,error=result.error) else h
        },
        quotes=if(current.settings.providers == requested.settings.providers) (current.quotes.associateBy { it.asset } + quotes.associateBy { it.asset }).values.toList() else current.quotes,
    )
}
data class Observations(val holdings: List<Holding>, val quotes: List<Quote>, val errors: List<String>)
class Providers(private val key: (String)->String, private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(15,TimeUnit.SECONDS).followRedirects(false).build()) {
    private suspend fun call(request: Request): Pair<Int,Pair<String,String?>> = suspendCancellableCoroutine { continuation ->
        val call=client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object: Callback {
            override fun onFailure(call: Call,e: IOException) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure("Network unavailable or request timed out")) }
            override fun onResponse(call: Call,response: Response) {
                response.use {
                    try {
                        val bytes=it.body?.byteStream()?.readLimited() ?: throw ProviderFailure("Empty response")
                        require(bytes.size <= MAX_FILE_BYTES) { "Provider response too large" }
                        if(continuation.isActive) continuation.resume(it.code to (bytes.toString(Charsets.UTF_8) to it.header("Retry-After")))
                    } catch(_: Exception) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure("Invalid provider response")) }
                }
            }
        })
    }
    private suspend fun request(url: String, body: String? = null, headers: Map<String,String> = emptyMap()): String {
        require(url.startsWith("https://")) { "HTTPS is required" }
        val builder=Request.Builder().url(url).header("User-Agent","Capital/0.1")
        headers.forEach { (k,v) -> builder.header(k,v) }
        body?.let { builder.post(it.toRequestBody(media)) }
        val req=builder.build()
        repeat(3) { attempt ->
            delay(1100) // ponytail: one foreground queue; per-provider queues if large portfolios need throughput.
            val result=try { call(req) } catch(e: ProviderFailure) {
                if(attempt == 2) throw e
                delay((1000L shl attempt)+(0..250).random()); return@repeat
            }
            val (status,payload)=result
            if(status in 200..299) return payload.first
            if(status==401 || status==403) throw ProviderFailure("Access denied; check provider key or quota")
            if(status!=429 && status !in 500..599) throw ProviderFailure("Provider returned HTTP $status")
            if(attempt==2) throw ProviderFailure("Provider busy or rate limited; retry later")
            val retry=payload.second?.let { value -> value.toLongOrNull()?.times(1000) ?: runCatching { ZonedDateTime.parse(value,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-System.currentTimeMillis() }.getOrNull() }
            if(retry != null && retry > 15_000) throw ProviderFailure("Provider quota reached; retry later")
            delay((retry ?: (1000L shl attempt)).coerceAtLeast(0)+(0..250).random())
        }
        throw ProviderFailure("Provider unavailable")
    }
    private suspend fun obj(url: String,body: String?=null,headers: Map<String,String> = emptyMap()): JsonObject = json.parseToJsonElement(request(url,body,headers)).jsonObject
    private fun requiredKey(provider: String)=key(provider).also { require(it.isNotBlank()) { "$provider needs your free API key in Settings" } }
    suspend fun balance(h: Holding,provider: String): Holding {
        val chain=Chain.valueOf(h.asset); val address=canonicalAddress(chain,requireNotNull(h.address))
        val quantity=when(chain) {
            Chain.BTC -> parseBtc(obj("https://${if(provider=="Blockstream") "blockstream.info" else "mempool.space"}/api/address/$address"))
            Chain.ETH -> {
                val url=if(provider=="Alchemy") "https://eth-mainnet.g.alchemy.com/v2/${requiredKey(provider)}" else "https://ethereum-rpc.publicnode.com"
                val chainId=parseRpc(obj(url,"""{"jsonrpc":"2.0","id":1,"method":"eth_chainId","params":[]}"""))
                require(chainId.substring(2).toBigInteger(16)==BigInteger.ONE) { "Wrong Ethereum network" }
                val units=parseRpc(obj(url,"""{"jsonrpc":"2.0","id":2,"method":"eth_getBalance","params":["$address","latest"]}"""))
                baseQuantity(units.substring(2).toBigInteger(16).toString(),chain)
            }
            Chain.TON -> {
                if(provider=="TON Center") {
                    val headers=key(provider).takeIf { it.isNotBlank() }?.let { mapOf("X-API-Key" to it) } ?: emptyMap()
                    val response=obj("https://toncenter.com/api/v2/getAddressBalance?address=$address",headers=headers)
                    require(response["ok"]?.jsonPrimitive?.booleanOrNull==true) { "TON provider rejected the address request" }
                    baseQuantity(response.string("result"),chain)
                } else baseQuantity(obj("https://tonapi.io/v2/accounts/$address").string("balance"),chain)
            }
            Chain.TRX -> {
                val url=if(provider=="TronGrid") "https://api.trongrid.io" else "https://tron.publicnode.com"
                val headers=if(provider=="TronGrid") mapOf("TRON-PRO-API-KEY" to requiredKey(provider)) else emptyMap()
                val response=obj("$url/walletsolidity/getaccount","""{"address":"$address","visible":false}""",headers)
                require(response["Error"]==null && response["error"]==null) { "TRON provider rejected the request" }
                // A valid getaccount query returns {} for an unactivated account.
                if(response.isEmpty()) "0" else {
                    require(response["address"]?.jsonPrimitive?.content?.lowercase()==address) { "TRON account mismatch" }
                    baseQuantity(response["balance"]?.jsonPrimitive?.content ?: "0",chain)
                }
            }
        }
        val now=System.currentTimeMillis()
        return h.copy(quantity=quantity,observedAt=now,fetchedAt=now,source=provider,error=null)
    }
    private suspend fun quoteEach(assets: Set<String>, onError: (String,Exception)->Unit, block: suspend (String)->Quote): List<Quote> {
        val results=mutableListOf<Quote>()
        for(asset in assets.sorted()) {
            try {
                val quote=block(asset)
                require(quote.usd.decimal()>ZERO && quote.observedAt>0 && quote.observedAt<=quote.fetchedAt+300_000) { "Invalid quote" }
                results += quote
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { onError(asset,e) }
        }
        return results
    }
    private suspend fun crypto(assets: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Quote> {
        val now=System.currentTimeMillis()
        if(assets.isEmpty()) return emptyList()
        val ids=mapOf("BTC" to "bitcoin","ETH" to "ethereum","TON" to "the-open-network","TRX" to "tron")
        if(provider=="CoinGecko") {
            val headers=mapOf("x-cg-demo-api-key" to requiredKey(provider))
            val result=obj("https://api.coingecko.com/api/v3/simple/price?ids=${assets.map { ids.getValue(it) }.joinToString(",")}&vs_currencies=usd&include_last_updated_at=true",headers=headers)
            return quoteEach(assets,onError) { asset ->
                val q=result[ids.getValue(asset)]?.jsonObject ?: throw ProviderFailure("No price for $asset")
                val price=q.string("usd").toBigDecimal().text(); require(price.decimal()>ZERO)
                val stamp=q.string("last_updated_at").toLong()*1000
                require(stamp>0 && stamp <= now+300_000) { "Invalid quote time" }
                Quote(asset,price,provider,stamp,now)
            }
        }
        val paprika=mapOf("BTC" to "btc-bitcoin","ETH" to "eth-ethereum","TON" to "toncoin-the-open-network","TRX" to "trx-tron")
        return quoteEach(assets,onError) { asset ->
            val q=obj("https://api.coinpaprika.com/v1/tickers/${paprika.getValue(asset)}")
            require(q.string("id")==paprika.getValue(asset)) { "Wrong quoted asset" }
            val price=q.getValue("quotes").jsonObject.getValue("USD").jsonObject.string("price").toBigDecimal().text()
            require(price.decimal()>ZERO)
            Quote(asset,price,provider,Instant.parse(q.string("last_updated")).toEpochMilli(),now)
        }
    }
    private suspend fun fiat(assets: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Quote> {
        if(assets.isEmpty()) return emptyList()
        val now=System.currentTimeMillis()
        if(provider=="Frankfurter") {
            val rows=json.parseToJsonElement(request("https://api.frankfurter.dev/v2/rates?base=USD&quotes=${assets.sorted().joinToString(",")}")).jsonArray
            return quoteEach(assets,onError) { asset ->
                val row=rows.map { it.jsonObject }.firstOrNull { it.string("quote")==asset } ?: throw ProviderFailure("No exchange rate for $asset")
                require(row.string("base")=="USD") { "Invalid exchange rate base" }
                val rate=row.string("rate").toBigDecimal(); require(rate>ZERO)
                Quote(asset,java.math.BigDecimal.ONE.divideMoney(rate).text(),provider,LocalDate.parse(row.string("date")).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),now)
            }
        }
        val xml=request("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
        require(!xml.contains("<!DOCTYPE",ignoreCase=true) && !xml.contains("<!ENTITY",ignoreCase=true)) { "ECB XML must not declare entities" }
        val factory=DocumentBuilderFactory.newInstance()
        val doc=factory.newDocumentBuilder().parse(xml.byteInputStream())
        val nodes=doc.getElementsByTagName("Cube"); val rates=mutableMapOf("EUR" to java.math.BigDecimal.ONE); var date: String?=null
        for(i in 0 until nodes.length) {
            val node=nodes.item(i) as org.w3c.dom.Element
            if(node.hasAttribute("time")) date=node.getAttribute("time")
            if(node.hasAttribute("currency")) rates[node.getAttribute("currency")]=node.getAttribute("rate").toBigDecimal().also { require(it>ZERO) }
        }
        val usd=rates["USD"] ?: throw ProviderFailure("ECB USD rate missing")
        val stamp=LocalDate.parse(requireNotNull(date)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return quoteEach(assets,onError) { asset -> Quote(asset,usd.divideMoney(rates[asset] ?: throw ProviderFailure("ECB does not cover $asset")).text(),provider,stamp,now) }
    }
    suspend fun refresh(data: Portfolio,bucketId: String?): Observations {
        val updated=mutableListOf<Holding>(); val quotes=mutableListOf<Quote>(); val errors=mutableListOf<String>()
        for(h in data.holdings.filter { it.address != null && (bucketId==null || it.bucketId==bucketId) }) {
            try { updated += balance(h,data.settings.providers.getValue(h.asset)) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += "${h.label}: $message"; updated += h.copy(error=message) }
        }
        val assets=(data.holdings.filter { bucketId==null || it.bucketId==bucketId }.map { it.asset }+data.buckets.map { it.currency }+data.goals.map { it.currency }+data.settings.currency).toSet()-"USD"
        val native=Chain.entries.map { it.name }.toSet()
        fun failedQuote(asset: String,e: Exception) {
            val message=e.safeMessage(); errors += "$asset: $message"
            data.quotes.find { it.asset==asset }?.let { quotes += it.copy(error=message) }
        }
        for((group,requested) in listOf("Crypto" to assets.intersect(native),"Fiat" to (assets-native))) {
            try { quotes += if(group=="Crypto") crypto(requested,data.settings.providers.getValue(group),::failedQuote) else fiat(requested,data.settings.providers.getValue(group),::failedQuote) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) {
                requested.forEach { failedQuote(it,e) }
            }
        }
        return Observations(updated,quotes,errors)
    }
}
fun Exception.safeMessage(): String = when(this) {
    is ProviderFailure -> message ?: "Provider unavailable"
    is IllegalArgumentException -> if(message?.contains("API key")==true) message!! else "Provider returned invalid or unsupported data"
    else -> "Could not read provider data; retry or change provider"
}
