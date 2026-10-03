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
/** Key problems, told apart by type so that safeMessage does not depend on the message language. */
class KeyArgument(message: String): IllegalArgumentException(message)
class KeyState(message: String): IllegalStateException(message)
fun JsonObject.string(name: String) = get(name)?.jsonPrimitive?.contentOrNull ?: throw ProviderFailure(tr("Missing {0} in provider response",name))
fun parseBtc(body: JsonObject): String {
    val stats=body["chain_stats"]?.jsonObject ?: throw ProviderFailure(tr("Missing confirmed balance"))
    val funded=stats.string("funded_txo_sum").toBigInteger()
    val spent=stats.string("spent_txo_sum").toBigInteger()
    require(funded >= BigInteger.ZERO && spent >= BigInteger.ZERO && funded >= spent) { tr("Invalid confirmed balance") }
    return baseQuantity((funded-spent).toString(),Chain.BTC)
}
fun parseRpc(body: JsonObject): String {
    if(body["error"] != null) throw ProviderFailure(tr("RPC rejected the request"))
    return body.string("result").also { require(it.matches(Regex("0x[0-9a-fA-F]+")) && it.length <= 82) { tr("Invalid RPC response") } }
}
fun mergeObservations(current: Portfolio, requested: Portfolio, holdings: List<Holding>, quotes: List<Quote>, unlisted: Set<String> = emptySet(), accounts: List<Account> = emptyList()): Portfolio {
    val results=holdings.associateBy { it.id }; val read=accounts.associateBy { it.id }
    val merged=current.copy(
        accounts=current.accounts.map { a -> read[a.id]?.takeIf { r -> requested.accounts.find { it.id == a.id }?.let { it.broker == a.broker && it.address == a.address } == true }?.let { r -> a.copy(asset=r.asset,quantity=r.quantity,observedAt=r.observedAt,fetchedAt=r.fetchedAt,error=r.error) } ?: a },
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
    return merged.copy(quotes=merged.quotes.filter { ':' !in it.asset || it.asset in used }).linked()
}
data class Observations(val holdings: List<Holding>, val quotes: List<Quote>, val errors: List<String>, val unlisted: Set<String> = emptySet(), val accounts: List<Account> = emptyList())
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
            override fun onFailure(call: Call,e: IOException) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure(tr("Network unavailable or request timed out"))) }
            override fun onResponse(call: Call,response: Response) {
                response.use {
                    try {
                        val bytes=it.body?.byteStream()?.readLimited() ?: throw ProviderFailure(tr("Empty response"))
                        if(continuation.isActive) continuation.resume(it.code to (bytes.toString(Charsets.UTF_8) to it.headers))
                    } catch(_: Exception) { if(continuation.isActive) continuation.resumeWithException(ProviderFailure(tr("Invalid provider response"))) }
                }
            }
        })
    }
    private suspend fun request(url: String, body: String? = null, headers: Map<String,String> = emptyMap()): String = send(url,body,headers).second.first
    // Statuses in `soft` are returned to the caller instead of failing.
    private suspend fun send(url: String, body: String? = null, headers: Map<String,String> = emptyMap(), soft: Set<Int> = emptySet()): Pair<Int,Pair<String,Headers>> {
        require(url.startsWith("https://")) { tr("HTTPS is required") }
        val builder=Request.Builder().url(url).header("User-Agent","Capital/2.0")
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
            if(status==401 || status==403) throw ProviderFailure(tr("Access denied; check provider key or quota"))
            if(status==402) throw ProviderFailure(tr("Provider quota reached; retry later"))
            if(status!=429 && status !in 500..599) throw ProviderFailure(tr("Provider returned HTTP {0}",status))
            if(attempt==2) throw ProviderFailure(tr("Provider busy or rate limited; retry later"))
            val retry=payload.second["Retry-After"]?.let { value -> value.toLongOrNull()?.times(1000) ?: runCatching { ZonedDateTime.parse(value,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()-System.currentTimeMillis() }.getOrNull() }
            if(retry != null && retry > 15_000) throw ProviderFailure(tr("Provider quota reached; retry later"))
            delay((retry ?: (1000L shl attempt)).coerceAtLeast(0)+(0..250).random())
        }
        throw ProviderFailure(tr("Provider unavailable"))
    }
    private suspend fun obj(url: String,body: String?=null,headers: Map<String,String> = emptyMap()): JsonObject = json.parseToJsonElement(request(url,body,headers)).jsonObject
    private fun requiredKey(provider: String)=key(provider).also { if(it.isBlank()) throw KeyArgument(tr("{0} needs your free API key in Settings",provider)) }
    suspend fun balance(h: Holding,provider: String): Holding {
        val chain=Chain.valueOf(h.asset); val address=canonicalAddress(chain,requireNotNull(h.address))
        val quantity=when(chain) {
            Chain.BTC -> parseBtc(obj("https://${if(provider=="Blockstream") "blockstream.info" else "mempool.space"}/api/address/$address"))
            Chain.ETH -> {
                val url=if(provider=="Alchemy") "https://eth-mainnet.g.alchemy.com/v2/${requiredKey(provider)}" else "https://ethereum-rpc.publicnode.com"
                val chainId=parseRpc(obj(url,"""{"jsonrpc":"2.0","id":1,"method":"eth_chainId","params":[]}"""))
                require(chainId.substring(2).toBigInteger(16)==BigInteger.ONE) { tr("Wrong Ethereum network") }
                val units=parseRpc(obj(url,"""{"jsonrpc":"2.0","id":2,"method":"eth_getBalance","params":["$address","latest"]}"""))
                baseQuantity(units.substring(2).toBigInteger(16).toString(),chain)
            }
            Chain.TON -> {
                if(provider=="TON Center") {
                    val headers=key(provider).takeIf { it.isNotBlank() }?.let { mapOf("X-API-Key" to it) } ?: emptyMap()
                    val response=obj("https://toncenter.com/api/v2/getAddressBalance?address=$address",headers=headers)
                    require(response["ok"]?.jsonPrimitive?.booleanOrNull==true) { tr("TON provider rejected the address request") }
                    baseQuantity(response.string("result"),chain)
                } else baseQuantity(obj("https://tonapi.io/v2/accounts/$address").string("balance"),chain)
            }
            Chain.TRX -> {
                val url=if(provider=="TronGrid") "https://api.trongrid.io" else "https://tron.publicnode.com"
                val headers=if(provider=="TronGrid") mapOf("TRON-PRO-API-KEY" to requiredKey(provider)) else emptyMap()
                val response=obj("$url/walletsolidity/getaccount","""{"address":"$address","visible":false}""",headers)
                require(response["Error"]==null && response["error"]==null) { tr("TRON provider rejected the request") }
                // A valid getaccount query returns {} for an unactivated account.
                if(response.isEmpty()) "0" else {
                    require(response["address"]?.jsonPrimitive?.content?.lowercase()==address) { tr("TRON account mismatch") }
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
        suspend fun constant(selector: String) = obj("https://api.trongrid.io/walletsolidity/triggerconstantcontract","""{"owner_address":"T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb","contract_address":"$visible","function_selector":"$selector","visible":true}""",headers)["constant_result"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: throw ProviderFailure(tr("Missing token {0}",selector))
        val decimals=BigInteger(constant("decimals()"),16); require(decimals.bitLength()<8 && decimals.toInt() in 0..36) { tr("Invalid token decimals") }
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
            require(result.size==1 && result.keys.single().equals(address,ignoreCase=true)) { tr("Wrong quoted token") }
            val q=result.values.single().jsonObject
            price=q.opt("usd")?.toBigDecimal() ?: return null
            stamp=q.string("last_updated_at").toLong()*1000
        } else {
            val platform=mapOf("ETH" to "eth-ethereum","TRX" to "trx-tron","TON" to "toncoin-the-open-network").getValue(chain.name)
            val (status,reply)=send("https://api.coinpaprika.com/v1/contracts/$platform/$address",soft=setOf(301,302,404))
            if(status==404) return null
            val ticker=if(status==200) json.parseToJsonElement(reply.first).jsonObject else {
                val id=Regex("/v1/tickers/([a-z0-9-]{1,100})").find(reply.second["Location"] ?: "")?.groupValues?.get(1) ?: throw ProviderFailure(tr("Invalid provider redirect"))
                obj("https://api.coinpaprika.com/v1/tickers/$id")
            }
            price=ticker.sub("quotes")?.sub("USD")?.opt("price")?.toBigDecimal() ?: return null
            stamp=Instant.parse(ticker.string("last_updated")).toEpochMilli()
        }
        val exact=price.setScale(18,java.math.RoundingMode.DOWN)
        if(exact.signum()<=0) return null
        require(stamp>0 && stamp<=now+300_000 && exact.text().decimal()>ZERO) { tr("Invalid quote") }
        return Quote(tokenAsset(chain.name,contract),exact.text(),provider,stamp,now)
    }
    private suspend fun quoteEach(assets: Set<String>, onError: (String,Exception)->Unit, block: suspend (String)->Quote): List<Quote> {
        val results=mutableListOf<Quote>()
        for(asset in assets.sorted()) {
            try {
                val quote=block(asset)
                require(quote.usd.decimal()>ZERO && quote.observedAt>0 && quote.observedAt<=quote.fetchedAt+300_000) { tr("Invalid quote") }
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
            val coins=obj("https://coins.llama.fi/prices/current/${chunk.joinToString(",")}").sub("coins") ?: throw ProviderFailure(tr("Missing coins in provider response"))
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
            return quoteEach(assets,onError) { asset -> coins["coingecko:${ids.getValue(asset)}"]?.let { (price,stamp) -> Quote(asset,price,provider,stamp,now) } ?: throw ProviderFailure(tr("No price for {0}",asset)) }
        }
        if(provider=="CoinGecko") {
            val headers=mapOf("x-cg-demo-api-key" to requiredKey(provider))
            val result=obj("https://api.coingecko.com/api/v3/simple/price?ids=${assets.map { ids.getValue(it) }.joinToString(",")}&vs_currencies=usd&include_last_updated_at=true",headers=headers)
            return quoteEach(assets,onError) { asset ->
                val q=result[ids.getValue(asset)]?.jsonObject ?: throw ProviderFailure(tr("No price for {0}",asset))
                val price=q.string("usd").toBigDecimal().text(); require(price.decimal()>ZERO)
                val stamp=q.string("last_updated_at").toLong()*1000
                require(stamp>0 && stamp <= now+300_000) { tr("Invalid quote time") }
                Quote(asset,price,provider,stamp,now)
            }
        }
        val paprika=mapOf("BTC" to "btc-bitcoin","ETH" to "eth-ethereum","TON" to "toncoin-the-open-network","TRX" to "trx-tron")
        return quoteEach(assets,onError) { asset ->
            val q=obj("https://api.coinpaprika.com/v1/tickers/${paprika.getValue(asset)}")
            require(q.string("id")==paprika.getValue(asset)) { tr("Wrong quoted asset") }
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
                val row=rows.map { it.jsonObject }.firstOrNull { it.string("quote")==asset } ?: throw ProviderFailure(tr("No exchange rate for {0}",asset))
                require(row.string("base")=="USD") { tr("Invalid exchange rate base") }
                val rate=row.string("rate").toBigDecimal(); require(rate>ZERO)
                Quote(asset,java.math.BigDecimal.ONE.divideMoney(rate).text(),provider,LocalDate.parse(row.string("date")).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),now)
            }
        }
        val xml=request("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
        require(!xml.contains("<!DOCTYPE",ignoreCase=true) && !xml.contains("<!ENTITY",ignoreCase=true)) { tr("ECB XML must not declare entities") }
        val factory=DocumentBuilderFactory.newInstance()
        val doc=factory.newDocumentBuilder().parse(xml.byteInputStream())
        val nodes=doc.getElementsByTagName("Cube"); val rates=mutableMapOf("EUR" to java.math.BigDecimal.ONE); var date: String?=null
        for(i in 0 until nodes.length) {
            val node=nodes.item(i) as org.w3c.dom.Element
            if(node.hasAttribute("time")) date=node.getAttribute("time")
            if(node.hasAttribute("currency")) rates[node.getAttribute("currency")]=node.getAttribute("rate").toBigDecimal().also { require(it>ZERO) }
        }
        val usd=rates["USD"] ?: throw ProviderFailure(tr("ECB USD rate missing"))
        val stamp=LocalDate.parse(requireNotNull(date)).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return quoteEach(assets,onError) { asset -> Quote(asset,usd.divideMoney(rates[asset] ?: throw ProviderFailure(tr("ECB does not cover {0}",asset))).text(),provider,stamp,now) }
    }
    private fun xml(text: String): org.w3c.dom.Document {
        require(!text.contains("<!DOCTYPE",ignoreCase=true) && !text.contains("<!ENTITY",ignoreCase=true)) { tr("XML must not declare entities") }
        return try { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(text.byteInputStream()) } catch(e: org.xml.sax.SAXException) { throw ProviderFailure(tr("Invalid provider response")) }
    }
    private fun org.w3c.dom.Element.child(name: String): String? = getElementsByTagName(name).item(0)?.textContent?.trim()
    private fun flexError(code: String?,message: String?): String = when(code) {
        "1012" -> tr("Token has expired; generate a new one in Client Portal")
        "1015" -> tr("Token is invalid")
        "1013" -> tr("Token is restricted to another IP address")
        "1014" -> tr("Flex Query id is invalid")
        "1011" -> tr("Flex Web Service is inactive; enable it in Client Portal")
        "1016" -> tr("Account is invalid")
        "1003","1021" -> tr("Statement is not available; retry later")
        else -> tr("Interactive Brokers error {0}",(code ?: "?")+(message?.let { " "+it.clean() } ?: ""))
    }
    private fun brokerToken(broker: String,name: String=broker)=key(name).also { if(it.isBlank()) throw KeyArgument(tr("{0} needs its credentials on the Brokers screen",broker)) }
    /** SnapTrade signed request: HMAC-SHA256 of the canonical payload with the consumer key. GET when body is null. */
    private suspend fun snap(path: String,post: Boolean=false): String {
        val client=brokerToken("SnapTrade"); val secret=brokerToken("SnapTrade","SnapTrade consumer key")
        if(!client.matches(Regex("[A-Za-z0-9_-]{1,100}"))) throw KeyArgument(tr("Invalid SnapTrade client id"))
        val query="clientId=$client&timestamp=${System.currentTimeMillis()/1000}"
        // The only POST sends an empty body, which SnapTrade signs as null content.
        val canonical="""{"content":null,"path":"/api/v1$path","query":"$query"}"""
        val mac=javax.crypto.Mac.getInstance("HmacSHA256").apply { init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(),"HmacSHA256")) }
        val signature=java.util.Base64.getEncoder().encodeToString(mac.doFinal(canonical.toByteArray()))
        return request("https://api.snaptrade.com/api/v1$path?$query",if(post) "{}" else null,mapOf("Signature" to signature))
    }
    /** Connection Portal URL for the user's own SnapTrade account; valid 5 minutes. */
    suspend fun snapTradeLogin(): String = json.parseToJsonElement(snap("/snapTrade/login",post=true)).jsonObject.string("redirectURI").also { require(it.startsWith("https://")) { tr("Invalid provider response") } }
    /** Accounts the broker lists: id to label. SnapTrade only. */
    suspend fun accounts(broker: String): List<Pair<String,String>> {
        require(broker=="SnapTrade") { tr("Choose a supported broker") }
        return json.parseToJsonElement(snap("/accounts")).jsonArray.map { it.jsonObject }.mapNotNull { a ->
            val id=a.opt("id")?.lowercase() ?: return@mapNotNull null
            id to listOfNotNull(a.opt("institution_name"),a.opt("name"),a.opt("number")).map { it.clean() }.filter { it.isNotBlank() }.distinct().joinToString(" · ").ifBlank { id }
        }
    }
    /** Interactive Brokers Flex Web Service: NAV total, base currency and report date of the one statement the query returns. */
    private suspend fun flex(query: String): Triple<String,String,Long> {
        val token=brokerToken("Interactive Brokers"); if(!token.matches(Regex("[A-Za-z0-9]{1,200}"))) throw KeyArgument(tr("Invalid Interactive Brokers token"))
        val base="https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService"; val ua=mapOf("User-Agent" to "Java")
        val sent=xml(request("$base/SendRequest?t=$token&q=$query&v=3",headers=ua)).documentElement
        if(sent.tagName!="FlexStatementResponse") throw ProviderFailure(tr("Invalid provider response"))
        if(sent.child("Status")!="Success") throw ProviderFailure(flexError(sent.child("ErrorCode"),sent.child("ErrorMessage")))
        val reference=sent.child("ReferenceCode")?.takeIf { it.matches(Regex("[0-9]{1,30}")) } ?: throw ProviderFailure(tr("Missing {0} in provider response","ReferenceCode"))
        // ponytail: 6 polls, 5 s apart (10 s when throttled); a query with many sections can take longer and then fails as "not ready".
        repeat(6) { attempt ->
            val doc=xml(request("$base/GetStatement?t=$token&q=$reference&v=3",headers=ua)); val root=doc.documentElement
            if(root.tagName=="FlexQueryResponse") {
                val statements=doc.getElementsByTagName("FlexStatement")
                if(statements.length!=1) throw ProviderFailure(tr("The query returned {0} accounts; make one Flex Query per account",statements.length))
                val rows=doc.getElementsByTagName("EquitySummaryByReportDateInBase")
                val latest=(0 until rows.length).map { rows.item(it) as org.w3c.dom.Element }.filter { it.hasAttribute("total") }.maxByOrNull { it.getAttribute("reportDate").filter { c -> c.isDigit() } }
                    ?: throw ProviderFailure(tr("Add the section Net Asset Value (NAV) Summary in Base with Report Date and Total to the Flex Query"))
                val info=doc.getElementsByTagName("AccountInformation").item(0) as? org.w3c.dom.Element
                val currency=latest.getAttribute("currency").ifBlank { info?.getAttribute("currency").orEmpty() }.ifBlank { throw ProviderFailure(tr("Add the Currency field of Account Information to the Flex Query")) }
                val date=latest.getAttribute("reportDate").filter { it.isDigit() }.takeIf { it.length==8 }?.let { LocalDate.parse(it,DateTimeFormatter.BASIC_ISO_DATE) } ?: throw ProviderFailure(tr("Invalid quote time"))
                return Triple(currency,latest.getAttribute("total"),date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
            }
            if(root.tagName!="FlexStatementResponse") throw ProviderFailure(tr("Invalid provider response"))
            val code=root.child("ErrorCode")
            if(code !in setOf("1009","1019","1018","1004")) throw ProviderFailure(flexError(code,root.child("ErrorMessage")))
            if(attempt==5) throw ProviderFailure(tr("Statement is not ready yet; refresh again in a minute"))
            delay(if(code=="1018") 10_000 else 5_000)
        }
        throw ProviderFailure(tr("Provider unavailable"))
    }
    /** Total account value in the account's base currency, read-only. */
    suspend fun account(a: Account): Account {
        val broker=a.broker; val id=accountId(broker,a.address); val now=System.currentTimeMillis()
        val (currency,total,observed)=when(broker) {
            "OANDA" -> {
                val a=obj("https://api-fxtrade.oanda.com/v3/accounts/$id/summary",headers=mapOf("Authorization" to "Bearer ${brokerToken(broker)}")).sub("account") ?: throw ProviderFailure(tr("Missing {0} in provider response","account"))
                require(a.string("id")==id) { tr("Account mismatch") }
                Triple(a.string("currency"),a.string("NAV"),now)
            }
            "Trading 212" -> {
                val basic=java.util.Base64.getEncoder().encodeToString("${brokerToken(broker)}:${brokerToken(broker,"Trading 212 secret")}".toByteArray())
                val a=obj("https://live.trading212.com/api/v0/equity/account/summary",headers=mapOf("Authorization" to "Basic $basic"))
                require(a.string("id")==id) { tr("Account mismatch") }
                Triple(a.string("currency"),a.string("totalValue"),now)
            }
            "SnapTrade" -> {
                val a=json.parseToJsonElement(snap("/accounts/$id")).jsonObject
                require(a.string("id").lowercase()==id) { tr("Account mismatch") }
                val t=a.sub("balance")?.sub("total") ?: throw ProviderFailure(tr("SnapTrade has no total value for this account yet; sync the connection and retry"))
                Triple(t.string("currency"),t.string("amount"),now)
            }
            else -> flex(id)
        }
        require(currency.matches(Regex("[A-Z]{3}")) && validAsset(currency)) { tr("Unsupported account currency {0}",currency.clean()) }
        val value=total.trim().toBigDecimalOrNull() ?: throw ProviderFailure(tr("Invalid account value"))
        if(value.signum()<0) throw ProviderFailure(tr("Negative account value {0} is not supported",value.toPlainString()))
        return a.copy(asset=currency,quantity=value.setScale(18,java.math.RoundingMode.DOWN).text().also { it.decimal() },observedAt=observed,fetchedAt=now,error=null)
    }
    suspend fun refresh(data: Portfolio,bucketId: String?): Observations {
        val updated=mutableListOf<Holding>(); val quotes=mutableListOf<Quote>(); val errors=mutableListOf<String>(); val read=mutableListOf<Account>()
        // A bucket refresh covers the accounts linked into that bucket; a full refresh covers every account.
        for(a in data.accounts.filter { a -> bucketId==null || data.holdings.any { it.bucketId==bucketId && it.accountId==a.id } }) {
            read += try { account(a) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0}: {1}",a.name,message); a.copy(error=message) }
        }
        for(h in data.holdings.filter { it.address != null && (bucketId==null || it.bucketId==bucketId) }) {
            var r=try { balance(h,data.settings.providers.getValue(h.asset)) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0}: {1}",h.label,message); h.copy(error=message) }
            val source=data.settings.providers["${h.asset} tokens"]
            if(h.asset != Chain.BTC.name && source != null) r=if(source=="Off") r.copy(tokens=emptyList(),tokensError=null) else try {
                val fresh=tokens(h,source).map { t -> h.tokens.find { it.contract==t.contract }?.let { o -> t.copy(decimals=t.decimals ?: o.decimals,symbol=t.symbol.ifBlank { o.symbol },checkedAt=o.checkedAt) } ?: t }
                r.copy(tokens=fresh,tokensError=null)
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0} tokens: {1}",h.label,message); r.copy(tokens=h.tokens,tokensError=message) }
            updated += r
        }
        // Account holdings may come back in another currency than stored; its rate is fetched in the same refresh.
        val assets=(data.holdings.filter { bucketId==null || it.bucketId==bucketId }.map { it.asset }+read.mapNotNull { it.asset }+data.buckets.map { it.currency }+data.goals.map { it.currency }+data.settings.currency).toSet()-"USD"
        val native=Chain.entries.map { it.name }.toSet()
        fun failedQuote(asset: String,e: Exception) {
            val message=e.safeMessage(); errors += tr("{0}: {1}",asset,message)
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
        if(missingKey != null) { errors += tr("Tokens: {0}",missingKey); lookups.forEach { keepOld(it,missingKey) } }
        else if(provider=="DefiLlama") {
            val platform=mapOf("ETH" to "ethereum","TRX" to "tron","TON" to "ton")
            val keys=lookups.associateWith { a -> val (c,contract)=a.split(":",limit=2); "${platform.getValue(c)}:${providerAddress(Chain.valueOf(c),contract)}" }
            try {
                val found=if(keys.isEmpty()) emptyMap() else llama(keys.values.toList())
                lookups.forEach { a -> looked += a; found[keys.getValue(a)]?.let { (price,stamp) -> quotes += Quote(a,price,provider,stamp,now) } ?: run { unlisted += a } }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("Tokens: {0}",message); lookups.forEach { keepOld(it,message) } }
        }
        else for((index,asset) in lookups.withIndex()) {
            val (c,contract)=asset.split(":",limit=2)
            try { val q=tokenQuote(Chain.valueOf(c),contract,provider); looked += asset; if(q==null) unlisted += asset else quotes += q }
            catch(e: CancellationException) { throw e }
            catch(e: ProviderFailure) {
                // Provider down or out of quota: stop asking, keep cached prices.
                val message=e.safeMessage(); errors += tr("Tokens: {0}",message); lookups.drop(index).forEach { keepOld(it,message) }; break
            }
            catch(e: Exception) { val message=e.safeMessage(); if(keepOld(asset,message)) errors += tr("Token {0}…: {1}",contract.take(10),message) }
        }
        val listed=(quotes.map { it.asset }+seen)-unlisted
        val info=mutableMapOf<String,Pair<Int,String>>()
        for(asset in updated.flatMap { h -> if(h.asset=="TRX") h.tokens.filter { it.decimals==null && it.contract !in h.excluded }.map { tokenAsset(h.asset,it.contract) } else emptyList() }.distinct().filter { it in listed }) {
            try { info[asset]=trxTokenInfo(asset.substringAfter(":"),data.settings.providers.getValue("TRX tokens")) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { errors += tr("TRX token {0}…: {1}",asset.substringAfter(":").take(10),e.safeMessage()) }
        }
        val final=updated.map { h -> h.copy(tokens=h.tokens.map { t ->
            val asset=tokenAsset(h.asset,t.contract); val i=info[asset]
            t.copy(decimals=t.decimals ?: i?.first,symbol=t.symbol.ifBlank { i?.second ?: "" },checkedAt=if(asset in looked) now else t.checkedAt)
        }) }
        return Observations(final,quotes,errors,unlisted,read)
    }
}
fun Exception.safeMessage(): String = when(this) {
    is ProviderFailure -> message ?: tr("Provider unavailable")
    is KeyArgument, is KeyState -> message.orEmpty()
    is IllegalArgumentException -> tr("Provider returned invalid or unsupported data")
    else -> tr("Could not read provider data; retry or change provider")
}
