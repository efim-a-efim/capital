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
fun mergeObservations(current: Portfolio, requested: Portfolio, holdings: List<Holding>, quotes: List<Quote>, unlisted: Set<String> = emptySet()): Portfolio {
    val results=holdings.associateBy { it.id }
    val merged=current.copy(
        holdings=current.holdings.map { h ->
            val old=requested.holdings.find { it.id == h.id }; val result=results[h.id]
            if(old != null && result != null && old.address == h.address && old.asset == h.asset && current.settings.providers[h.asset] == requested.settings.providers[h.asset]) {
                val native=h.copy(quantity=result.quantity,observedAt=result.observedAt,fetchedAt=result.fetchedAt,source=result.source,error=result.error)
                val src="${h.asset} tokens"
                if(current.settings.providers[src] == requested.settings.providers[src]) native.copy(tokens=result.tokens,tokensError=result.tokensError) else native
            } else h
        },
        quotes=if(current.settings.providers == requested.settings.providers) (current.quotes.associateBy { it.asset } + quotes.associateBy { it.asset }).values.filter { it.asset !in unlisted } else current.quotes,
    )
    val used=merged.holdings.flatMap { h -> h.tokens.filter { it.contract !in h.excluded }.map { tokenAsset(h.asset,it.contract) } }.toSet()
    return merged.copy(quotes=merged.quotes.filter { ':' !in it.asset || it.asset in used })
}
data class Observations(val holdings: List<Holding>, val quotes: List<Quote>, val errors: List<String>, val unlisted: Set<String> = emptySet())
// Contract lookups per refresh. DefiLlama answers in batches. CoinPaprika free tier: 60 requests per hour, a listed contract costs two.
fun tokenLookups(provider: String) = when(provider) { "DefiLlama" -> 1000; "CoinPaprika" -> 10; else -> 30 }
const val MIN_CONFIDENCE = "0.9"
private fun String.clean() = filterNot { it.isISOControl() }.trim().take(40)
private fun JsonObject.opt(name: String) = get(name)?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
private fun JsonObject.sub(name: String) = get(name)?.takeIf { it !is JsonNull }?.jsonObject
private fun cleanTokens(chain: Chain, raw: List<Token>) = raw.mapNotNull { t ->
    val contract=runCatching { canonicalAddress(chain,t.contract) }.getOrNull() ?: return@mapNotNull null
    if(!t.units.matches(Regex("[0-9]{1,80}")) || t.units.all { it=='0' }) null
    else t.copy(contract=contract,symbol=t.symbol.clean(),name=t.name.clean(),decimals=t.decimals?.takeIf { it in 0..36 })
}.distinctBy { it.contract }.take(100) // ponytail: first 100 in source order; wallets with more token types need paging plus a checked-contract memory.
class Providers(private val key: (String)->String, private val client: OkHttpClient = OkHttpClient.Builder().callTimeout(15,TimeUnit.SECONDS).followRedirects(false).build(), private val pace: Long = 1100) {
    private suspend fun call(request: Request): Pair<Int,Pair<String,Headers>> = suspendCancellableCoroutine { continuation ->
        val call=client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object: Callback {
            override fun onFailure(call: Call,e: IOException) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure("Network unavailable or request timed out")) }
            override fun onResponse(call: Call,response: Response) {
                response.use {
                    try {
                        val bytes=it.body?.byteStream()?.readLimited() ?: throw ProviderFailure("Empty response")
                        if(continuation.isActive) continuation.resume(it.code to (bytes.toString(Charsets.UTF_8) to it.headers))
                    } catch(_: Exception) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure("Invalid provider response")) }
                }
            }
        })
    }
    private suspend fun request(url: String, body: String? = null, headers: Map<String,String> = emptyMap()): String = send(url,body,headers).second.first
    // Statuses in `soft` are returned to the caller instead of failing.
    private suspend fun send(url: String, body: String? = null, headers: Map<String,String> = emptyMap(), soft: Set<Int> = emptySet()): Pair<Int,Pair<String,Headers>> {
        require(url.startsWith("https://")) { "HTTPS is required" }
        val builder=Request.Builder().url(url).header("User-Agent","Capital/1.0")
        headers.forEach { (k,v) -> builder.header(k,v) }
        body?.let { builder.post(it.toRequestBody(media)) }
        val req=builder.build()
        repeat(3) { attempt ->
            delay(pace) // ponytail: one foreground queue; per-provider queues if large portfolios need throughput.
            val result=try { call(req) } catch(e: ProviderFailure) {
                if(attempt == 2) throw e
                delay((1000L shl attempt)+(0..250).random()); return@repeat
            }
            val (status,payload)=result
            if(status in 200..299 || status in soft) return result
            if(status==401 || status==403) throw ProviderFailure("Access denied; check provider key or quota")
            if(status==402) throw ProviderFailure("Provider quota reached; retry later")
            if(status!=429 && status !in 500..599) throw ProviderFailure("Provider returned HTTP $status")
            if(attempt==2) throw ProviderFailure("Provider busy or rate limited; retry later")
            val retry=payload.second["Retry-After"]?.let { value -> value.toLongOrNull()?.times(1000) ?: runCatching { ZonedDateTime.parse(value,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-System.currentTimeMillis() }.getOrNull() }
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
    suspend fun tokens(h: Holding,provider: String): List<Token> {
        val chain=Chain.valueOf(h.asset); val address=canonicalAddress(chain,requireNotNull(h.address))
        fun token(contract: String?,symbol: String?,name: String?,units: String?,decimals: String?) = contract?.let { c -> units?.let { Token(c,symbol ?: "",name ?: "",it,decimals?.toIntOrNull()) } }
        val raw=when(chain) {
            Chain.BTC -> emptyList()
            Chain.ETH -> if(provider=="Blockscout") json.parseToJsonElement(request("https://eth.blockscout.com/api/v2/addresses/$address/token-balances")).jsonArray.mapNotNull { r ->
                val o=r.jsonObject; val t=o.sub("token")
                if(t?.opt("type")!="ERC-20") null else token(t.opt("address_hash"),t.opt("symbol"),t.opt("name"),o.opt("value"),t.opt("decimals"))
            } else (obj("https://api.ethplorer.io/getAddressInfo/$address?apiKey=freekey")["tokens"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { r ->
                // ponytail: Ethplorer may give rawBalance in exponent form; those rows are dropped as invalid.
                val o=r.jsonObject; val t=o.sub("tokenInfo")
                token(t?.opt("address"),t?.opt("symbol"),t?.opt("name"),o.opt("rawBalance"),t?.opt("decimals"))
            }
            Chain.TON -> if(provider=="TonAPI") (obj("https://tonapi.io/v2/accounts/$address/jettons")["balances"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { r ->
                val o=r.jsonObject; val j=o.sub("jetton")
                token(j?.opt("address"),j?.opt("symbol"),j?.opt("name"),o.opt("balance"),j?.opt("decimals"))
            } else {
                val headers=key(provider).takeIf { it.isNotBlank() }?.let { mapOf("X-API-Key" to it) } ?: emptyMap()
                val response=obj("https://toncenter.com/api/v3/jetton/wallets?owner_address=$address&exclude_zero_balance=true&limit=100",headers=headers)
                (response["jetton_wallets"]?.jsonArray ?: JsonArray(emptyList())).mapNotNull { r ->
                    val o=r.jsonObject; val jetton=o.opt("jetton")
                    val info=jetton?.let { response.sub("metadata")?.sub(it)?.get("token_info")?.jsonArray?.firstOrNull()?.jsonObject }
                    token(jetton,info?.opt("symbol"),info?.opt("name"),o.opt("balance"),info?.sub("extra")?.opt("decimals"))
                }
            }
            Chain.TRX -> {
                // TronGrid serves this keyless at a low rate; the key is sent when the user has one.
                val response=obj("https://api.trongrid.io/v1/accounts/${providerAddress(chain,address)}",headers=key(provider).takeIf { it.isNotBlank() }?.let { mapOf("TRON-PRO-API-KEY" to it) } ?: emptyMap())
                val account=response["data"]?.jsonArray?.firstOrNull()?.jsonObject
                (account?.get("trc20")?.jsonArray ?: JsonArray(emptyList())).flatMap { r -> r.jsonObject.mapNotNull { (c,u) -> token(c,null,null,u.jsonPrimitive.content,null) } }
            }
        }
        return cleanTokens(chain,raw)
    }
    /** TRC-20 decimals and symbol through a constant contract call. */
    suspend fun trxTokenInfo(contract: String,provider: String): Pair<Int,String> {
        val headers=key(provider).takeIf { it.isNotBlank() }?.let { mapOf("TRON-PRO-API-KEY" to it) } ?: emptyMap(); val visible=providerAddress(Chain.TRX,contract)
        suspend fun constant(selector: String) = obj("https://api.trongrid.io/walletsolidity/triggerconstantcontract","""{"owner_address":"T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb","contract_address":"$visible","function_selector":"$selector","visible":true}""",headers)["constant_result"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: throw ProviderFailure("Missing token $selector")
        val decimals=BigInteger(constant("decimals()"),16); require(decimals.bitLength()<8 && decimals.toInt() in 0..36) { "Invalid token decimals" }
        // ponytail: old tokens answering symbol() with a bare bytes32 get an empty symbol.
        val symbol=runCatching { val b=constant("symbol()").unhex(); val off=BigInteger(1,b.copyOfRange(0,32)).toInt(); val len=BigInteger(1,b.copyOfRange(off,off+32)).toInt(); String(b,off+32,len,Charsets.UTF_8).clean() }.getOrDefault("")
        return decimals.toInt() to symbol
    }
    /** Null when the provider does not list the contract; throws on failures. */
    private suspend fun tokenQuote(chain: Chain,contract: String,provider: String): Quote? {
        val now=System.currentTimeMillis(); val address=providerAddress(chain,contract)
        val price: java.math.BigDecimal; val stamp: Long
        if(provider=="CoinGecko") {
            val platform=mapOf("ETH" to "ethereum","TRX" to "tron","TON" to "the-open-network").getValue(chain.name)
            val result=obj("https://api.coingecko.com/api/v3/simple/token_price/$platform?contract_addresses=$address&vs_currencies=usd&include_last_updated_at=true",headers=mapOf("x-cg-demo-api-key" to requiredKey(provider)))
            if(result.isEmpty()) return null
            require(result.size==1 && result.keys.single().equals(address,ignoreCase=true)) { "Wrong quoted token" }
            val q=result.values.single().jsonObject
            price=q.opt("usd")?.toBigDecimal() ?: return null
            stamp=q.string("last_updated_at").toLong()*1000
        } else {
            val platform=mapOf("ETH" to "eth-ethereum","TRX" to "trx-tron","TON" to "toncoin-the-open-network").getValue(chain.name)
            val (status,reply)=send("https://api.coinpaprika.com/v1/contracts/$platform/$address",soft=setOf(301,302,404))
            if(status==404) return null
            val ticker=if(status==200) json.parseToJsonElement(reply.first).jsonObject else {
                val id=Regex("/v1/tickers/([a-z0-9-]{1,100})").find(reply.second["Location"] ?: "")?.groupValues?.get(1) ?: throw ProviderFailure("Invalid provider redirect")
                obj("https://api.coinpaprika.com/v1/tickers/$id")
            }
            price=ticker.sub("quotes")?.sub("USD")?.opt("price")?.toBigDecimal() ?: return null
            stamp=Instant.parse(ticker.string("last_updated")).toEpochMilli()
        }
        val exact=price.setScale(18,java.math.RoundingMode.DOWN)
        if(exact.signum()<=0) return null
        require(stamp>0 && stamp<=now+300_000 && exact.text().decimal()>ZERO) { "Invalid quote" }
        return Quote(tokenAsset(chain.name,contract),exact.text(),provider,stamp,now)
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
    /** DefiLlama batch prices by requested key (price, observed at). Entries without a positive price or below MIN_CONFIDENCE are left out. */
    private suspend fun llama(keys: List<String>): Map<String,Pair<String,Long>> {
        val now=System.currentTimeMillis()
        return keys.distinct().chunked(50).flatMap { chunk ->
            val coins=obj("https://coins.llama.fi/prices/current/${chunk.joinToString(",")}").sub("coins") ?: throw ProviderFailure("Missing coins in provider response")
            chunk.mapNotNull { k ->
                // TON and TRON addresses are case-sensitive; hex and ids are not.
                val c=coins.entries.firstOrNull { it.key.equals(k,ignoreCase=k.startsWith("ethereum:") || k.startsWith("coingecko:")) }?.value as? JsonObject ?: return@mapNotNull null
                val price=c.opt("price")?.toBigDecimalOrNull()?.setScale(18,java.math.RoundingMode.DOWN) ?: return@mapNotNull null
                val confidence=c.opt("confidence")?.toBigDecimalOrNull() ?: return@mapNotNull null
                val stamp=(c.opt("timestamp")?.toBigDecimalOrNull()?.toLong() ?: return@mapNotNull null)*1000
                if(price.signum()<=0 || price.precision()-price.scale()>40 || confidence<MIN_CONFIDENCE.toBigDecimal() || stamp<=0 || stamp>now+300_000) null else k to (price.text() to stamp)
            }
        }.toMap()
    }
    private suspend fun crypto(assets: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Quote> {
        val now=System.currentTimeMillis()
        if(assets.isEmpty()) return emptyList()
        val ids=mapOf("BTC" to "bitcoin","ETH" to "ethereum","TON" to "the-open-network","TRX" to "tron")
        if(provider=="DefiLlama") {
            val coins=llama(assets.map { "coingecko:${ids.getValue(it)}" })
            return quoteEach(assets,onError) { asset -> coins["coingecko:${ids.getValue(asset)}"]?.let { (price,stamp) -> Quote(asset,price,provider,stamp,now) } ?: throw ProviderFailure("No price for $asset") }
        }
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
            var r=try { balance(h,data.settings.providers.getValue(h.asset)) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += "${h.label}: $message"; h.copy(error=message) }
            val source=data.settings.providers["${h.asset} tokens"]
            if(h.asset != Chain.BTC.name && source != null) r=if(source=="Off") r.copy(tokens=emptyList(),tokensError=null) else try {
                val fresh=tokens(h,source).map { t -> h.tokens.find { it.contract==t.contract }?.let { o -> t.copy(decimals=t.decimals ?: o.decimals,symbol=t.symbol.ifBlank { o.symbol },checkedAt=o.checkedAt) } ?: t }
                r.copy(tokens=fresh,tokensError=null)
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += "${h.label} tokens: $message"; r.copy(tokens=h.tokens,tokensError=message) }
            updated += r
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
        // Contract lookups: held tokens with a quote first, then oldest check first (never checked = 0).
        val provider=data.settings.providers.getValue("Crypto")
        val seen=data.quotes.map { it.asset }.toSet()
        // Position within the wallet breaks ties, so wallets share the budget instead of the first one using it up.
        val stamps=updated.flatMap { h -> h.tokens.filter { it.contract !in h.excluded }.mapIndexed { i,t -> tokenAsset(h.asset,t.contract) to ((t.checkedAt ?: 0L) to i) } }.groupBy({ it.first },{ it.second }).mapValues { (_,v) -> v.minOf { it.first } to v.minOf { it.second } }
        // Two thirds of the budget keeps known prices fresh; the rest classifies unchecked tokens.
        val budget=tokenLookups(provider)
        val (quoted,fresh)=stamps.keys.sortedWith(compareBy<String>({ stamps.getValue(it).first },{ stamps.getValue(it).second },{ it })).partition { it in seen }
        val head=quoted.take(budget*2/3)
        val lookups=(head+fresh.take(budget-head.size)+quoted.drop(head.size)).take(budget)
        val unlisted=mutableSetOf<String>(); val looked=mutableSetOf<String>(); val now=System.currentTimeMillis()
        fun keepOld(asset: String,message: String): Boolean = data.quotes.find { it.asset==asset }?.let { quotes += it.copy(error=message); true } ?: false
        val missingKey=try { if(lookups.isNotEmpty() && provider=="CoinGecko") requiredKey(provider); null } catch(e: Exception) { e.safeMessage() }
        if(missingKey != null) { errors += "Tokens: $missingKey"; lookups.forEach { keepOld(it,missingKey) } }
        else if(provider=="DefiLlama") {
            val platform=mapOf("ETH" to "ethereum","TRX" to "tron","TON" to "ton")
            val keys=lookups.associateWith { a -> val (c,contract)=a.split(":",limit=2); "${platform.getValue(c)}:${providerAddress(Chain.valueOf(c),contract)}" }
            try {
                val found=if(keys.isEmpty()) emptyMap() else llama(keys.values.toList())
                lookups.forEach { a -> looked += a; found[keys.getValue(a)]?.let { (price,stamp) -> quotes += Quote(a,price,provider,stamp,now) } ?: run { unlisted += a } }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += "Tokens: $message"; lookups.forEach { keepOld(it,message) } }
        }
        else for((index,asset) in lookups.withIndex()) {
            val (c,contract)=asset.split(":",limit=2)
            try { val q=tokenQuote(Chain.valueOf(c),contract,provider); looked += asset; if(q==null) unlisted += asset else quotes += q }
            catch(e: CancellationException) { throw e }
            catch(e: ProviderFailure) {
                // Provider down or out of quota: stop asking, keep cached prices.
                val message=e.safeMessage(); errors += "Tokens: $message"; lookups.drop(index).forEach { keepOld(it,message) }; break
            }
            catch(e: Exception) { val message=e.safeMessage(); if(keepOld(asset,message)) errors += "Token ${contract.take(10)}…: $message" }
        }
        val listed=(quotes.map { it.asset }+seen)-unlisted
        val info=mutableMapOf<String,Pair<Int,String>>()
        for(asset in updated.flatMap { h -> if(h.asset=="TRX") h.tokens.filter { it.decimals==null && it.contract !in h.excluded }.map { tokenAsset(h.asset,it.contract) } else emptyList() }.distinct().filter { it in listed }) {
            try { info[asset]=trxTokenInfo(asset.substringAfter(":"),data.settings.providers.getValue("TRX tokens")) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { errors += "TRX token ${asset.substringAfter(":").take(10)}…: ${e.safeMessage()}" }
        }
        val final=updated.map { h -> h.copy(tokens=h.tokens.map { t ->
            val asset=tokenAsset(h.asset,t.contract); val i=info[asset]
            t.copy(decimals=t.decimals ?: i?.first,symbol=t.symbol.ifBlank { i?.second ?: "" },checkedAt=if(asset in looked) now else t.checkedAt)
        }) }
        return Observations(final,quotes,errors,unlisted)
    }
}
fun Exception.safeMessage(): String = when(this) {
    is ProviderFailure -> message ?: "Provider unavailable"
    is IllegalArgumentException -> if(message?.contains("API key")==true) message!! else "Provider returned invalid or unsupported data"
    is IllegalStateException -> if(message?.contains("key")==true) message!! else "Could not read provider data; retry or change provider"
    else -> "Could not read provider data; retry or change provider"
}
