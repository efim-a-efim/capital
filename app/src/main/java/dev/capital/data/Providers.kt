package dev.capital.data

import dev.capital.brokers.BrokerHost
import dev.capital.brokers.Brokers
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
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
// --- annual inflation and market growth series for FIRE goals ---
/** Currency to World Bank area. The one place for the map. */
private val rateAreas = mapOf("USD" to "USA","EUR" to "EMU","GBP" to "GBR","JPY" to "JPN","INR" to "IND","BRL" to "BRA","RUB" to "RUS","CNY" to "CHN")
fun rateArea(currency: String) = rateAreas[currency] ?: "WLD"
/** The World Bank has no equity index for the euro area or the world; the US index stands in. */
private fun growthArea(currency: String) = rateArea(currency).let { if(it=="EMU" || it=="WLD") "USA" else it }
private val oecdAreas = mapOf("USD" to "USA","EUR" to "EA20","GBP" to "GBR","JPY" to "JPN","INR" to "IND","BRL" to "BRA","CNY" to "CHN")
// RUB has no index with a long history on Yahoo; the US index stands in, like for any currency without its own.
private val yahooSymbols = mapOf("USD" to "^GSPC","EUR" to "^GDAXI","GBP" to "^FTSE","JPY" to "^N225","INR" to "^BSESN","BRL" to "^BVSP","CNY" to "000001.SS")
/** Currencies that get a rate series: ISO fiat codes of goals with a FIRE plan. */
fun rateCurrencies(data: Portfolio): Set<String> = data.goals.filter { it.fire != null }.map { it.currency }.filter { c -> c.matches(Regex("[A-Z]{3}")) && c !in Chain.entries.map { it.name } && runCatching { java.util.Currency.getInstance(c) }.isSuccess }.toSet()
private fun pct(d: java.math.BigDecimal) = d.setScale(2,java.math.RoundingMode.HALF_UP).text()
private fun JsonElement?.num() = (this as? JsonPrimitive)?.content?.toBigDecimalOrNull()
private fun badResponse() = ProviderFailure(tr("Invalid provider response"))
/** Area to year to percent. Null values are skipped. */
fun parseWorldBank(body: String): Map<String,Map<String,String>> {
    val rows=(json.parseToJsonElement(body) as? JsonArray)?.getOrNull(1) as? JsonArray ?: throw badResponse()
    val out=mutableMapOf<String,MutableMap<String,String>>()
    for(r in rows) {
        val o=r.jsonObject; val v=o["value"].num() ?: continue; val year=o.string("date")
        require(year.matches(Regex("[0-9]{4}"))) { tr("Invalid provider response") }
        out.getOrPut(o.string("countryiso3code")) { mutableMapOf() }[year]=pct(v)
    }
    return out
}
private fun csvRow(line: String): List<String> {
    val cells=mutableListOf<String>(); val cell=StringBuilder(); var quoted=false; var i=0
    while(i<line.length) {
        val c=line[i]
        if(quoted) { if(c=='"') { if(line.getOrNull(i+1)=='"') { cell.append('"'); i++ } else quoted=false } else cell.append(c) }
        else if(c=='"') quoted=true else if(c==',') { cells += cell.toString(); cell.clear() } else cell.append(c)
        i++
    }
    return cells+cell.toString()
}
/** Monthly annual rates to one mean per year; years without all twelve months are left out. */
fun parseEcbInflation(csv: String): Map<String,String> {
    val lines=csv.lines().filter { it.isNotBlank() }
    val head=csvRow(lines.firstOrNull() ?: throw badResponse())
    val period=head.indexOf("TIME_PERIOD").takeIf { it>=0 } ?: throw ProviderFailure(tr("Missing {0} in provider response","TIME_PERIOD"))
    val value=head.indexOf("OBS_VALUE").takeIf { it>=0 } ?: throw ProviderFailure(tr("Missing {0} in provider response","OBS_VALUE"))
    val months=mutableMapOf<String,MutableMap<String,java.math.BigDecimal>>()
    for(line in lines.drop(1)) {
        val cells=csvRow(line); val t=cells.getOrNull(period) ?: throw badResponse()
        require(t.matches(Regex("[0-9]{4}-[0-9]{2}"))) { tr("Invalid provider response") }
        months.getOrPut(t.take(4)) { mutableMapOf() }[t]=cells.getOrNull(value)?.toBigDecimalOrNull() ?: continue
    }
    return months.filterValues { it.size==12 }.mapValues { (_,m) -> pct(m.values.reduce { a,b -> a+b }.divide(java.math.BigDecimal(12),10,java.math.RoundingMode.HALF_UP)) }
}
/** OECD SDMX-JSON, one area, observations keyed by dimension index (dimensionAtObservation=AllDimensions). */
fun parseOecd(body: String): Map<String,String> {
    val data=json.parseToJsonElement(body).jsonObject["data"]?.jsonObject ?: throw badResponse()
    val dims=data["structure"]?.jsonObject?.get("dimensions")?.jsonObject?.get("observation")?.jsonArray ?: throw badResponse()
    val position=dims.indexOfFirst { it.jsonObject["id"]?.jsonPrimitive?.content=="TIME_PERIOD" }.takeIf { it>=0 } ?: throw badResponse()
    val years=dims[position].jsonObject["values"]?.jsonArray?.map { it.jsonObject.string("id") } ?: throw badResponse()
    val observations=data["dataSets"]?.jsonArray?.firstOrNull()?.jsonObject?.get("observations")?.jsonObject ?: throw badResponse()
    val out=mutableMapOf<String,String>()
    for((key,row) in observations) {
        val year=years.getOrNull(key.split(":").getOrNull(position)?.toIntOrNull() ?: throw badResponse()) ?: throw badResponse()
        require(year.matches(Regex("[0-9]{4}"))) { tr("Invalid provider response") }
        out[year]=pct((row as? JsonArray)?.firstOrNull().num() ?: continue)
    }
    return out
}
/** Yahoo monthly closes to the yearly change from the December close to the next; the current year is left out. */
fun parseYahoo(body: String,thisYear: Int): Map<String,String> {
    val result=(json.parseToJsonElement(body).jsonObject["chart"]?.jsonObject?.get("result") as? JsonArray)?.firstOrNull()?.jsonObject ?: throw badResponse()
    val offset=result["meta"]?.jsonObject?.get("gmtoffset").num()?.toLong() ?: 0L
    val times=result["timestamp"]?.jsonArray ?: throw badResponse()
    val closes=result["indicators"]?.jsonObject?.get("quote")?.jsonArray?.firstOrNull()?.jsonObject?.get("close")?.jsonArray ?: throw badResponse()
    require(times.size==closes.size) { tr("Invalid provider response") }
    val december=sortedMapOf<Int,java.math.BigDecimal>()
    for(i in times.indices) {
        val close=closes[i].num() ?: continue
        val date=Instant.ofEpochSecond(times[i].jsonPrimitive.long+offset).atZone(ZoneOffset.UTC).toLocalDate()
        if(date.monthValue==12) december[date.year]=close
    }
    val out=mutableMapOf<String,String>()
    for((year,close) in december) {
        val before=december[year-1] ?: continue
        if(year<thisYear && before.signum()>0) out[year.toString()]=pct(close.divide(before,java.math.MathContext.DECIMAL64).subtract(java.math.BigDecimal.ONE).multiply(java.math.BigDecimal(100)))
    }
    return out
}
fun mergeObservations(current: Portfolio, requested: Portfolio, holdings: List<Holding>, quotes: List<Quote>, unlisted: Set<String> = emptySet(), accounts: List<Account> = emptyList(), rates: List<Rate> = emptyList(), history: List<Series> = emptyList(), flows: List<Flow> = emptyList()): Portfolio {
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
        rates=current.rates.filterNot { old -> rates.any { it.kind==old.kind && it.currency==old.currency } }+rates,
        quotes=if(current.settings.providers == requested.settings.providers) (current.quotes.associateBy { it.asset } + quotes.associateBy { it.asset }).values.filter { it.asset !in unlisted } else current.quotes,
    )
    val used=merged.holdings.flatMap { h -> h.tokens.filter { it.contract !in h.excluded }.map { tokenAsset(h.asset,it.contract) } }.toSet()
    // Fetched days replace stored ones; other days are kept. A series keeps its newest 4000 days.
    val union=history.fold(merged.history.associateBy { it.key }) { acc,s -> acc + (s.key to Series(s.key,(acc[s.key]?.points.orEmpty()+s.points).toSortedMap().entries.toList().takeLast(4000).associate { it.key to it.value })) }
    // Fetched flows replace stored ones with the same holding, source and ref; the newest 20000 are kept.
    fun flowKey(f: Flow) = "${f.holdingId}/${f.source}/${f.ref}"
    val allFlows=(merged.flows.associateBy(::flowKey)+flows.associateBy(::flowKey)).values.sortedBy { it.date }.takeLast(20000)
    return merged.copy(quotes=merged.quotes.filter { ':' !in it.asset || it.asset in used },history=union.values.toList(),flows=allFlows).recorded().linked().recordedPnl().solvedFire()
}
/** Oldest first day of history the portfolio needs for an asset: the earliest day of its stored series, else the oldest observation time of any holding, account or quote, else one year back. Never more than 4000 days back. */
fun historyStart(data: Portfolio,asset: String,today: LocalDate): LocalDate {
    val stored=data.history.find { it.key=="rate:$asset" }?.points?.keys?.minOrNull()?.let { LocalDate.parse(it) }
    val observed=(data.holdings.mapNotNull { it.observedAt }+data.accounts.mapNotNull { it.observedAt }+data.quotes.map { it.observedAt }).filter { it>0 }.minOrNull()?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
    // A PnL bucket values its holdings from its first quantity point or flow on: its assets and currency need rates from that day.
    val pnl=data.buckets.filter { it.pnl }.mapNotNull { b ->
        val held=data.holdings.filter { it.bucketId==b.id }
        val days=held.flatMap { h -> data.history.find { it.key=="qty:${h.id}" }?.points?.keys.orEmpty()+data.flows.filter { it.holdingId==h.id }.map { it.date } }
        if(asset==b.currency || held.any { it.asset==asset } || data.flows.any { f -> f.currency==asset && held.any { it.id==f.holdingId } }) days.minOrNull()?.let { LocalDate.parse(it) } else null
    }.minOrNull()
    // The earliest of: the stored series, the oldest observation, the PnL start and one year back (a series that only holds today's point must still backfill).
    return listOfNotNull(stored,observed,pnl,today.minusDays(365)).min().let { if(it.isBefore(today.minusDays(3999))) today.minusDays(3999) else it }
}
/** The day to fetch history from, or null when the asset's series is complete: it has its first needed day and a point in the last 7 days. */
fun historyGap(data: Portfolio,asset: String,today: LocalDate): LocalDate? {
    val start=historyStart(data,asset,today); val points=data.history.find { it.key=="rate:$asset" }?.points?.keys.orEmpty()
    if(start.toString() !in points) return start
    return points.max().takeIf { it<today.minusDays(7).toString() }?.let { LocalDate.parse(it) }
}
private val coingeckoIds=mapOf("BTC" to "bitcoin","ETH" to "ethereum","TON" to "the-open-network","TRX" to "tron")
private val llamaPlatforms=mapOf("ETH" to "ethereum","TRX" to "tron","TON" to "ton")
/** DefiLlama coin key of a chain native or token asset. */
private fun llamaKey(asset: String): String {
    if(':' !in asset) return "coingecko:${coingeckoIds.getValue(asset)}"
    val (c,contract)=asset.split(":",limit=2)
    return "${llamaPlatforms.getValue(c)}:${providerAddress(Chain.valueOf(c),contract)}"
}
private fun llamaCase(key: String) = key.startsWith("ethereum:") || key.startsWith("coingecko:")
/** Dates since..until cut into inclusive ranges of at most `size` days. */
private fun dayRanges(since: LocalDate,until: LocalDate,size: Long) = generateSequence(since) { it.plusDays(size) }.takeWhile { !it.isAfter(until) }.map { it to minOf(it.plusDays(size-1),until) }.toList()
data class Observations(val holdings: List<Holding>, val quotes: List<Quote>, val errors: List<String>, val unlisted: Set<String> = emptySet(), val accounts: List<Account> = emptyList(), val rates: List<Rate> = emptyList(), val history: List<Series> = emptyList(), val flows: List<Flow> = emptyList())
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
                val c=coins.entries.firstOrNull { it.key.equals(k,ignoreCase=llamaCase(k)) }?.value as? JsonObject ?: return@mapNotNull null
                val price=c.opt("price")?.toBigDecimalOrNull()?.setScale(18,java.math.RoundingMode.DOWN) ?: return@mapNotNull null
                val confidence=c.opt("confidence")?.toBigDecimalOrNull() ?: return@mapNotNull null
                val stamp=(c.opt("timestamp")?.toBigDecimalOrNull()?.toLong() ?: return@mapNotNull null)*1000
                if(price.signum()<=0 || price.precision()-price.scale()>40 || confidence<MIN_CONFIDENCE.toBigDecimal() || stamp<=0 || stamp>now+300_000) null else k to (price.text() to stamp)
            }
        }.toMap()
    }
    suspend fun crypto(assets: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Quote> {
        val now=System.currentTimeMillis()
        if(assets.isEmpty()) return emptyList()
        val ids=coingeckoIds
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
    suspend fun fiat(assets: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Quote> {
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
    /** Annual percent series of one kind ("inflation" or "growth") per currency. Failed currencies go to onError; the caller keeps their old series. */
    suspend fun rates(kind: String,currencies: Set<String>,provider: String,onError: (String,Exception)->Unit): List<Rate> {
        val now=System.currentTimeMillis(); val year=LocalDate.now(ZoneOffset.UTC).year
        fun rate(c: String,values: Map<String,String>?): Rate {
            val kept=values.orEmpty().toSortedMap().entries.toList().takeLast(200).associate { it.key to it.value }
            if(kept.isEmpty()) throw ProviderFailure(tr("No rate data for {0}",c))
            return Rate(kind,c,kept,provider,now,now)
        }
        suspend fun each(block: suspend (String)->Rate) = currencies.sorted().mapNotNull { c ->
            try { block(c) } catch(e: CancellationException) { throw e } catch(e: Exception) { onError(c,e); null }
        }
        if(provider=="World Bank") {
            val area: (String)->String = if(kind=="inflation") ::rateArea else ::growthArea
            val indicator=if(kind=="inflation") "FP.CPI.TOTL.ZG" else "CM.MKT.INDX.ZG"
            val areas=currencies.map(area).distinct().sorted().joinToString(";")
            val byArea=try { parseWorldBank(request("https://api.worldbank.org/v2/country/$areas/indicator/$indicator?format=json&per_page=2000&date=1990:$year")) }
            catch(e: CancellationException) { throw e } catch(e: Exception) { currencies.sorted().forEach { onError(it,e) }; return emptyList() }
            return each { c -> rate(c,byArea[area(c)]) }
        }
        return each { c ->
            rate(c,when {
                kind=="inflation" && provider=="ECB" -> {
                    if(c!="EUR") throw ProviderFailure(tr("ECB does not cover {0}",c))
                    parseEcbInflation(request("https://data-api.ecb.europa.eu/service/data/ICP/M.U2.N.000000.4.ANR?format=csvdata&startPeriod=1997-01"))
                }
                kind=="inflation" && provider=="OECD" -> {
                    val area=oecdAreas[c] ?: throw ProviderFailure(tr("OECD does not cover {0}",c))
                    parseOecd(request("https://sdmx.oecd.org/public/rest/data/OECD.SDD.TPS,DSD_PRICES@DF_PRICES_ALL,1.0/$area.A.N.CPI.PA._T.N.GY?startPeriod=1990&dimensionAtObservation=AllDimensions&format=jsondata",null,mapOf("Accept" to "application/vnd.sdmx.data+json")))
                }
                kind=="growth" && provider=="Yahoo Finance" -> {
                    val symbol=(yahooSymbols[c] ?: "^GSPC").replace("^","%5E")
                    parseYahoo(request("https://query1.finance.yahoo.com/v8/finance/chart/$symbol?range=30y&interval=1mo"),year)
                }
                else -> throw ProviderFailure(tr("Unsupported provider"))
            })
        }
    }
    /** Daily USD prices of assets from `since` to `until` as `rate:<asset>` series. Failed assets go to onError and are left out; the caller keeps their old series. */
    suspend fun history(assets: Set<String>,since: LocalDate,until: LocalDate,provider: String,onError: (String,Exception)->Unit): List<Series> {
        if(provider!="Frankfurter + DefiLlama" || assets.isEmpty() || until.isBefore(since)) return emptyList()
        val points=mutableMapOf<String,MutableMap<String,String>>(); val failed=mutableSetOf<String>()
        fun fail(asset: String,e: Exception) { if(failed.add(asset)) onError(asset,e) }
        // One request for the assets that have not failed yet; a failure of the whole request fails each of them.
        suspend fun group(list: List<String>,block: suspend (List<String>)->Unit) {
            val active=list.filter { it !in failed }
            if(active.isEmpty()) return
            try { block(active) } catch(e: CancellationException) { throw e } catch(e: Exception) { active.forEach { fail(it,e) } }
        }
        val native=Chain.entries.map { it.name }.toSet()
        val fiat=assets.filter { ':' !in it && it !in native && it!="USD" }.sorted()
        // Frankfurter answers about one year per request and only for business days; the consumer carries the last point forward.
        for((from,to) in dayRanges(since,until,365)) group(fiat) { active ->
            val rows=obj("https://api.frankfurter.dev/v1/$from..$to?base=USD&symbols=${active.joinToString(",")}")["rates"]?.jsonObject ?: throw ProviderFailure(tr("Missing {0} in provider response","rates"))
            val found=mutableMapOf<String,MutableMap<String,String>>()
            for((day,row) in rows) {
                LocalDate.parse(day)
                for(a in active) {
                    val rate=row.jsonObject.opt(a)?.toBigDecimalOrNull() ?: continue
                    if(rate.signum()<=0) throw badResponse()
                    found.getOrPut(a) { mutableMapOf() }[day]=java.math.BigDecimal.ONE.divideMoney(rate).text()
                }
            }
            found.forEach { (a,p) -> points.getOrPut(a) { mutableMapOf() } += p }
        }
        for(list in assets.filter { ':' in it || it in native }.sorted().chunked(20)) for((from,to) in dayRanges(since,until,1000)) group(list) { active ->
            val keys=active.mapNotNull { a -> try { a to llamaKey(a) } catch(e: Exception) { fail(a,e); null } }.toMap()
            if(keys.isEmpty()) return@group
            val start=from.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
            val coins=obj("https://coins.llama.fi/chart/${keys.values.joinToString(",")}?start=$start&span=${ChronoUnit.DAYS.between(from,to)+1}&period=1d").sub("coins") ?: throw ProviderFailure(tr("Missing coins in provider response"))
            for((a,k) in keys) try {
                val prices=(coins.entries.firstOrNull { it.key.equals(k,ignoreCase=llamaCase(k)) }?.value as? JsonObject)?.get("prices")?.jsonArray ?: throw ProviderFailure(tr("No price for {0}",a))
                val found=mutableMapOf<String,String>()
                for(row in prices) {
                    val o=row.jsonObject; val day=Instant.ofEpochSecond(o.string("timestamp").toBigDecimal().toLong()).atZone(ZoneOffset.UTC).toLocalDate()
                    val price=o.string("price").toBigDecimal().setScale(18,java.math.RoundingMode.DOWN)
                    if(price.signum()<0 || price.precision()-price.scale()>40) throw badResponse()
                    if(!day.isBefore(from) && !day.isAfter(to)) found[day.toString()]=price.text()
                }
                points.getOrPut(a) { mutableMapOf() } += found
            } catch(e: CancellationException) { throw e } catch(e: Exception) { fail(a,e) }
        }
        return assets.sorted().filter { it !in failed }.mapNotNull { a ->
            val p=points[a].orEmpty()
            if(p.isEmpty()) { onError(a,ProviderFailure(tr("No rate data for {0}",a))); null }
            else Series("rate:$a",p.toSortedMap().entries.toList().takeLast(4000).associate { it.key to it.value })
        }
    }
    /** What broker plugins see: the paced HTTPS client and the stored credentials. */
    private val host=object: BrokerHost {
        override fun secret(key: String)=key(key)
        override fun required(key: String,broker: String)=key(key).also { if(it.isBlank()) throw KeyArgument(tr("{0} needs its credentials on the Brokers screen",broker)) }
        override suspend fun send(url: String,body: String?,headers: Map<String,String>,soft: Set<Int>): dev.capital.brokers.Response {
            val (status,payload)=this@Providers.send(url,body,headers,soft)
            return dev.capital.brokers.Response(status,payload.first,payload.second.names().associate { it.lowercase() to payload.second[it].orEmpty() })
        }
        override suspend fun json(url: String,body: String?,headers: Map<String,String>)=obj(url,body,headers)
    }
    private fun plugin(name: String)=Brokers.byName(name) ?: throw IllegalArgumentException(tr("Choose a supported broker"))
    /** Accounts the broker lists: id to label. */
    suspend fun accounts(broker: String): List<Pair<String,String>> = plugin(broker).accounts(host)
    /** URL of the broker's authorization page for the user's browser. */
    suspend fun connect(broker: String): String = plugin(broker).connect(host)
    /** Total account value in the account's base currency, read-only; the plugin reads, the host checks the result. */
    /** Flows of a linked account as [Flow]s of `holding`, checked: currency, decimal, date not in the future, unique refs. */
    suspend fun flows(a: Account,holding: Holding,since: LocalDate): List<Flow> {
        val plugin=plugin(a.broker); if(!plugin.listsFlows) return emptyList()
        val today=LocalDate.now().plusDays(1)
        return plugin.flows(host,accountId(a.broker,a.address),since).map { f ->
            require(f.currency.matches(Regex("[A-Z]{3}")) && validAsset(f.currency)) { tr("Unsupported account currency {0}",f.currency.clean()) }
            val amount=f.amount.trim().toBigDecimalOrNull() ?: throw ProviderFailure(tr("Invalid account value"))
            require(!f.date.isAfter(today) && f.ref.isNotBlank() && f.ref.length<=200) { tr("Invalid provider response") }
            Flow(holding.id,f.date.toString(),amount.setScale(18,java.math.RoundingMode.DOWN).text(),f.currency,a.broker,f.ref.filterNot { it.isISOControl() }.trim().take(200))
        }.also { list -> require(list.map { it.ref }.distinct().size==list.size) { tr("Invalid provider response") } }
    }
    /** Daily totals of a linked account as `qty:` points (value in the account currency), checked like [account]. */
    suspend fun accountHistory(a: Account,since: LocalDate): Map<String,String> {
        val plugin=plugin(a.broker); if(!plugin.listsHistory) return emptyMap()
        val today=LocalDate.now().plusDays(1)
        return plugin.history(host,accountId(a.broker,a.address),since).associate { (day,r) ->
            require(r.currency.matches(Regex("[A-Z]{3}")) && validAsset(r.currency) && !day.isAfter(today)) { tr("Invalid provider response") }
            val value=r.total.trim().toBigDecimalOrNull()?.takeIf { it.signum()>=0 } ?: throw ProviderFailure(tr("Invalid account value"))
            day.toString() to value.setScale(18,java.math.RoundingMode.DOWN).text()
        }
    }
    suspend fun account(a: Account): Account {
        val now=System.currentTimeMillis()
        val r=plugin(a.broker).read(host,accountId(a.broker,a.address))
        require(r.currency.matches(Regex("[A-Z]{3}")) && validAsset(r.currency)) { tr("Unsupported account currency {0}",r.currency.clean()) }
        val value=r.total.trim().toBigDecimalOrNull() ?: throw ProviderFailure(tr("Invalid account value"))
        if(value.signum()<0) throw ProviderFailure(tr("Negative account value {0} is not supported",value.toPlainString()))
        require(r.observedAt in 1..now+86_400_000) { tr("Invalid quote time") }
        return a.copy(asset=r.currency,quantity=value.setScale(18,java.math.RoundingMode.DOWN).text().also { it.decimal() },observedAt=r.observedAt,fetchedAt=now,error=null)
    }
    suspend fun refresh(data: Portfolio,bucketId: String?): Observations {
        val updated=mutableListOf<Holding>(); val quotes=mutableListOf<Quote>(); val errors=mutableListOf<String>(); val read=mutableListOf<Account>()
        // A bucket refresh covers the accounts linked into that bucket; a full refresh covers every account.
        for(a in data.accounts.filter { a -> bucketId==null || data.holdings.any { it.bucketId==bucketId && it.accountId==a.id } }) {
            read += try { account(a) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0} · {1}",a.broker,tr("{0}: {1}",a.name,message)); a.copy(error=message) }
        }
        // Cash flows of the accounts linked into PnL buckets, from 90 days before the last stored flow (brokers post late) or one year back.
        val fetchedFlows=mutableListOf<Flow>(); val pnlBuckets=data.buckets.filter { it.pnl }.map { it.id }.toSet()
        for(a in read.filter { it.error==null }) {
            val h=data.holdings.find { it.accountId==a.id && it.bucketId in pnlBuckets } ?: continue
            val since=data.flows.filter { it.holdingId==h.id && it.source==a.broker }.maxOfOrNull { it.date }?.let { LocalDate.parse(it).minusDays(90) } ?: LocalDate.now().minusDays(365)
            try { fetchedFlows += flows(a,h,since) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { errors += tr("{0} · {1}",a.broker,tr("{0}: {1}",a.name,e.safeMessage())) }
        }
        for(h in data.holdings.filter { it.address != null && (bucketId==null || it.bucketId==bucketId) }) {
            var r=try { balance(h,data.settings.providers.getValue(h.asset)) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0} · {1}",data.settings.providers.getValue(h.asset),tr("{0}: {1}",h.label,message)); h.copy(error=message) }
            val source=data.settings.providers["${h.asset} tokens"]
            if(h.asset != Chain.BTC.name && source != null) r=if(source=="Off") r.copy(tokens=emptyList(),tokensError=null) else try {
                val fresh=tokens(h,source).map { t -> h.tokens.find { it.contract==t.contract }?.let { o -> t.copy(decimals=t.decimals ?: o.decimals,symbol=t.symbol.ifBlank { o.symbol },checkedAt=o.checkedAt) } ?: t }
                r.copy(tokens=fresh,tokensError=null)
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0} · {1}",source,tr("{0} tokens: {1}",h.label,message)); r.copy(tokens=h.tokens,tokensError=message) }
            updated += r
        }
        // Account holdings may come back in another currency than stored; its rate is fetched in the same refresh.
        val assets=(data.holdings.filter { bucketId==null || it.bucketId==bucketId }.map { it.asset }+read.mapNotNull { it.asset }+data.buckets.map { it.currency }+data.goals.map { it.currency }+data.settings.currency).toSet()-"USD"
        val native=Chain.entries.map { it.name }.toSet()
        fun failedQuote(asset: String,e: Exception) {
            val message=e.safeMessage(); errors += tr("{0} · {1}",data.settings.providers.getValue(if(asset in native) "Crypto" else "Fiat"),tr("{0}: {1}",asset,message))
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
        if(missingKey != null) { errors += tr("{0} · {1}",provider,tr("Tokens: {0}",missingKey)); lookups.forEach { keepOld(it,missingKey) } }
        else if(provider=="DefiLlama") {
            val keys=lookups.associateWith(::llamaKey)
            try {
                val found=if(keys.isEmpty()) emptyMap() else llama(keys.values.toList())
                lookups.forEach { a -> looked += a; found[keys.getValue(a)]?.let { (price,stamp) -> quotes += Quote(a,price,provider,stamp,now) } ?: run { unlisted += a } }
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { val message=e.safeMessage(); errors += tr("{0} · {1}",provider,tr("Tokens: {0}",message)); lookups.forEach { keepOld(it,message) } }
        }
        else for((index,asset) in lookups.withIndex()) {
            val (c,contract)=asset.split(":",limit=2)
            try { val q=tokenQuote(Chain.valueOf(c),contract,provider); looked += asset; if(q==null) unlisted += asset else quotes += q }
            catch(e: CancellationException) { throw e }
            catch(e: ProviderFailure) {
                // Provider down or out of quota: stop asking, keep cached prices.
                val message=e.safeMessage(); errors += tr("{0} · {1}",provider,tr("Tokens: {0}",message)); lookups.drop(index).forEach { keepOld(it,message) }; break
            }
            catch(e: Exception) { val message=e.safeMessage(); if(keepOld(asset,message)) errors += tr("{0} · {1}",provider,tr("Token {0}…: {1}",contract.take(10),message)) }
        }
        val listed=(quotes.map { it.asset }+seen)-unlisted
        val info=mutableMapOf<String,Pair<Int,String>>()
        for(asset in updated.flatMap { h -> if(h.asset=="TRX") h.tokens.filter { it.decimals==null && it.contract !in h.excluded }.map { tokenAsset(h.asset,it.contract) } else emptyList() }.distinct().filter { it in listed }) {
            try { info[asset]=trxTokenInfo(asset.substringAfter(":"),data.settings.providers.getValue("TRX tokens")) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { errors += tr("{0} · {1}",data.settings.providers.getValue("TRX tokens"),tr("TRX token {0}…: {1}",asset.substringAfter(":").take(10),e.safeMessage())) }
        }
        val final=updated.map { h -> h.copy(tokens=h.tokens.map { t ->
            val asset=tokenAsset(h.asset,t.contract); val i=info[asset]
            t.copy(decimals=t.decimals ?: i?.first,symbol=t.symbol.ifBlank { i?.second ?: "" },checkedAt=if(asset in looked) now else t.checkedAt)
        }) }
        val fetchedRates=mutableListOf<Rate>(); val rateNow=System.currentTimeMillis()
        if(bucketId==null) for((kind,group) in listOf("inflation" to "Inflation","growth" to "Market growth")) {
            val source=data.settings.providers.getValue(group); val currencies=rateCurrencies(data)
            if(source=="Off" || currencies.isEmpty()) continue
            fun failedRate(c: String,e: Exception) {
                val message=e.safeMessage(); errors += tr("{0} · {1}",source,tr("{0}: {1}","$c $group",message))
                data.rates.find { it.kind==kind && it.currency==c }?.let { fetchedRates += it.copy(error=message,fetchedAt=rateNow) }
            }
            try { fetchedRates += rates(kind,currencies,source,::failedRate) }
            catch(e: CancellationException) { throw e }
            catch(e: Exception) { currencies.forEach { failedRate(it,e) } }
        }
        val fetchedHistory=mutableListOf<Series>()
        val historySource=data.settings.providers.getValue("Rate history")
        if(bucketId==null && historySource!="Off") {
            val today=LocalDate.now(ZoneOffset.UTC)
            // Assets that miss days, grouped by the day to start from so that one request covers many.
            val needs=assets.sorted().mapNotNull { a -> historyGap(data,a,today)?.let { a to it } }.groupBy({ it.second },{ it.first })
            for((since,group) in needs) {
                try { fetchedHistory += history(group.toSet(),since,today,historySource) { a,e -> errors += tr("{0} · {1}",historySource,tr("{0}: {1}",a,e.safeMessage())) } }
                catch(e: CancellationException) { throw e }
                catch(e: Exception) { group.forEach { a -> errors += tr("{0} · {1}",historySource,tr("{0}: {1}",a,e.safeMessage())) } }
            }
        }
        // Stored days win over fetched ones; the rest is the union.
        val mergedHistory=fetchedHistory.groupBy { it.key }.map { (k,list) -> Series(k,list.flatMap { it.points.entries }.associate { it.key to it.value }+data.history.find { it.key==k }?.points.orEmpty()) }
        return Observations(final,quotes,errors,unlisted,read,fetchedRates,mergedHistory,fetchedFlows)
    }
}
fun Exception.safeMessage(): String = when(this) {
    is ProviderFailure -> message ?: tr("Provider unavailable")
    is KeyArgument, is KeyState -> message.orEmpty()
    is IllegalArgumentException -> tr("Provider returned invalid or unsupported data")
    else -> tr("Could not read provider data; retry or change provider")
}
