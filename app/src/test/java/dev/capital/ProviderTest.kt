package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

class ProviderTest {
    private fun client(retryAfter: String="0",reply: (Request)->Pair<Int,String>)=OkHttpClient.Builder().addInterceptor { chain ->
        val (status,body)=reply(chain.request())
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test").header("Retry-After",retryAfter).body(body.toResponseBody()).build()
    }.build()
    @Test fun partialQuoteFailurePreservesSuccessfulObservations()=runBlocking {
        val p=Portfolio(buckets=listOf(Bucket("b","Cash","USD")),holdings=listOf(Holding("eur","b","Euro","EUR","10"),Holding("rsd","b","Dinar","RSD","10")),quotes=listOf(Quote("RSD","0.01","old",1000,1000)),settings=Settings("USD"))
        val provider=Providers({ "" },client { 200 to """[{"base":"USD","quote":"EUR","date":"2026-09-28","rate":0.8}]""" })
        val result=provider.refresh(p,null)
        assertEquals(2,result.quotes.size)
        assertEquals("1.25",result.quotes.first { it.asset=="EUR" }.usd)
        assertNotNull(result.quotes.first { it.asset=="RSD" }.error)
        assertEquals("0.01",result.quotes.first { it.asset=="RSD" }.usd)
        assertEquals(1,result.errors.size)
    }
    @Test fun transientFailuresRetryButAuthenticationDoesNot()=runBlocking {
        val h=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client {
            if(attempts.incrementAndGet()<3) 429 to "{}" else 200 to """{"chain_stats":{"funded_txo_sum":100,"spent_txo_sum":0}}"""
        })
        assertEquals("0.000001",provider.balance(h,"Blockstream").quantity)
        assertEquals(3,attempts.get())
        attempts.set(0)
        val denied=Providers({ "" },client { attempts.incrementAndGet(); 401 to "secret provider detail" })
        try { denied.balance(h,"Blockstream"); fail("Expected access error") } catch(e: ProviderFailure) { assertFalse(e.message!!.contains("secret")) }
        assertEquals(1,attempts.get())
    }
    @Test fun cancellationStopsNetworkBeforeDispatch()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client { attempts.incrementAndGet(); 200 to "{}" })
        val h=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        try { withTimeout(30) { provider.balance(h,"Blockstream") }; fail("Expected cancellation") } catch(_: TimeoutCancellationException) { }
        assertEquals(0,attempts.get())
    }
    private val btc=Holding("h","b","BTC","BTC",null,"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
    @Test fun malformedResponseIsRejectedWithoutRetry()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client { attempts.incrementAndGet(); 200 to "not json" })
        try { provider.balance(btc,"Blockstream"); fail("Expected failure") } catch(_: kotlinx.serialization.SerializationException) { }
        assertEquals(1,attempts.get())
    }
    @Test fun timeoutRetriesAreBoundedToThreeAttempts()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },OkHttpClient.Builder().addInterceptor { attempts.incrementAndGet(); throw java.net.SocketTimeoutException("timeout") }.build())
        try { provider.balance(btc,"Blockstream"); fail("Expected failure") } catch(e: ProviderFailure) { assertEquals("Network unavailable or request timed out",e.message) }
        assertEquals(3,attempts.get())
    }
    @Test fun retryAfterBeyondBudgetStopsEarly()=runBlocking {
        val attempts=AtomicInteger()
        val provider=Providers({ "" },client("60") { attempts.incrementAndGet(); 429 to "{}" })
        try { provider.balance(btc,"Blockstream"); fail("Expected quota failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("quota")) }
        assertEquals(1,attempts.get())
    }
    @Test fun refreshRetainsPriorHoldingOnFailure()=runBlocking {
        val p=Portfolio(buckets=listOf(Bucket("b","Cash","USD")),holdings=listOf(btc.copy(quantity="0.5")),settings=Settings("USD"))
        val result=Providers({ "" },client { 500 to "{}" }).refresh(p,null)
        assertEquals("0.5",result.holdings.single().quantity)
        assertNotNull(result.holdings.single().error)
        assertEquals(2,result.errors.size) // holding failure + BTC quote (CoinGecko needs API key)
    }
    // --- wallet tokens ---
    private val real="0xdac17f958d2ee523a2206206994597c13d831ec7"
    private val fakeUsdt="0x"+"1".repeat(40)
    private val wallet="0x52908400098527886e0f7030069857d2e4169ee7"
    private fun route(pace: Long=0,reply: (Request)->Triple<Int,String,String?>?): Providers = Providers({ "k" },OkHttpClient.Builder().addInterceptor { chain ->
        val (status,body,location)=reply(chain.request()) ?: Triple(404,"{}",null)
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test").apply { location?.let { header("Location",it) } }.body(body.toResponseBody()).build()
    }.build(),pace)
    private fun ok(body: String) = Triple(200,body,null as String?)
    private fun ticker(id: String,price: String) = ok("""{"id":"$id","quotes":{"USD":{"price":$price}},"last_updated":"2026-09-29T00:00:00Z"}""")
    private fun market(req: Request): Triple<Int,String,String?>? {
        val path=req.url.encodedPath
        return when {
            path.startsWith("/v1/contracts/") -> if(path.endsWith(real)) Triple(301,"","http://api.coinpaprika.com/v1/tickers/usdt-tether?contract=$real") else Triple(404,"{}",null)
            path=="/v1/tickers/usdt-tether" -> ticker("usdt-tether","1.0")
            path=="/v1/tickers/eth-ethereum" -> ticker("eth-ethereum","2000")
            req.url.host=="ethereum-rpc.publicnode.com" -> ok("""{"result":"${if(okio.Buffer().also { req.body?.writeTo(it) }.readUtf8().contains("eth_chainId")) "0x1" else "0x2a"}"}""")
            else -> null
        }
    }
    private fun blockscout(vararg rows: Triple<String,String,String>) = ok(rows.joinToString(",","[","]") { (a,v,type) -> """{"token":{"address_hash":"$a","decimals":"6","name":"n","symbol":"USDT","type":"$type"},"value":"$v"}""" })
    private val ethWallet=Holding("w","b","Wallet","ETH",null,wallet)
    private fun ethPortfolio(w: Holding=ethWallet,quotes: List<Quote> = emptyList(),ethTokens: String="Blockscout")=Portfolio(buckets=listOf(Bucket("b","B","USD")),holdings=listOf(w),quotes=quotes,
        settings=Settings("USD",providers=providerChoices.mapValues { it.value.first() }+("Crypto" to "CoinPaprika")+("ETH tokens" to ethTokens)))
    @Test fun blockscoutKeepsOnlyValidErc20()=runBlocking {
        val provider=route { blockscout(Triple(real,"250000000","ERC-20"),Triple("0x"+"4".repeat(40),"1","ERC-721"),Triple("0x"+"5".repeat(40),"0","ERC-20"),Triple("0x123","7","ERC-20")).let { ok(it.second.replace("USDT","US\\u0000D\\nT")) } }
        val t=provider.tokens(ethWallet,"Blockscout").single()
        assertEquals(real,t.contract); assertEquals("250000000",t.units); assertEquals(6,t.decimals); assertEquals("USDT",t.symbol)
    }
    @Test fun tonCenterReadsUppercaseJettonAndMetadata()=runBlocking {
        val jetton="0:B113A994B5024A16719F69139328EB759596C38A25F59028B146FECDC3621DFE"
        val urls=mutableListOf<String>()
        val provider=route { urls += it.url.toString(); ok("""{"jetton_wallets":[{"balance":"5000000","jetton":"$jetton"}],"metadata":{"$jetton":{"token_info":[{"name":"N","symbol":"S","extra":{"decimals":"6"}}]}}}""") }
        val t=provider.tokens(Holding("w","b","W","TON",null,"EQCxE6mUtQJKFnGfaROTKOt1lZbDiiX1kCixRv7Nw2Id_sDs"),"TON Center").single()
        assertEquals(jetton.lowercase(),t.contract); assertEquals(6,t.decimals); assertEquals("S",t.symbol)
        assertTrue(urls.single().contains("owner_address=0:b113"))
    }
    @Test fun tronGridReadsTrc20AndEmptyAccount()=runBlocking {
        val trx=Holding("w","b","W","TRX",null,"T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb")
        val usdt="TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        val t=route { ok("""{"data":[{"trc20":[{"$usdt":"1000"},{"$usdt":"5"}]}]}""") }.tokens(trx,"TronGrid").single()
        assertEquals(canonicalAddress(Chain.TRX,usdt),t.contract); assertNull(t.decimals); assertEquals("",t.symbol)
        assertTrue(route { ok("""{"data":[]}""") }.tokens(trx,"TronGrid").isEmpty())
    }
    @Test fun fakeUsdtIsUnknownAndNotCounted()=runBlocking {
        val p=ethPortfolio()
        val provider=route { if(it.url.host=="eth.blockscout.com") blockscout(Triple(real,"250000000","ERC-20"),Triple(fakeUsdt,"999000000","ERC-20")) else market(it) }
        val result=provider.refresh(p,null)
        val merged=mergeObservations(p,p,result.holdings,result.quotes,result.unlisted)
        val h=merged.holdings.single()
        assertEquals(2,h.tokens.size)
        assertTrue(merged.known(h,h.tokens.first { it.contract==real })); assertFalse(merged.known(h,h.tokens.first { it.contract==fakeUsdt }))
        assertEquals(0,BigDecimal("250.000000000000084").compareTo(merged.bucketValue("b","USD")))
        assertFalse(merged.incomplete("USD")); assertTrue(tokenAsset("ETH",fakeUsdt) in result.unlisted)
        assertTrue(h.tokens.all { it.checkedAt != null })
    }
    @Test fun previouslyListedTokenBecomesUnlisted() {
        val asset=tokenAsset("ETH",real)
        val w=ethWallet.copy(tokens=listOf(Token(real,units="5",decimals=6)))
        val p=ethPortfolio(w,listOf(Quote(asset,"1","x",1000,1000)))
        assertTrue(mergeObservations(p,p,listOf(w),emptyList(),setOf(asset)).quotes.isEmpty())
        assertEquals(1,mergeObservations(p,p,listOf(w),emptyList()).quotes.size)
    }
    @Test fun tokenSourceFailureKeepsOldTokensWhileNativeUpdates()=runBlocking {
        val old=Token(real,"USDT","",units="5",decimals=6)
        val p=ethPortfolio(ethWallet.copy(tokens=listOf(old)))
        val result=route { if(it.url.host=="eth.blockscout.com") Triple(401,"{}",null) else market(it) }.refresh(p,null)
        val h=result.holdings.single()
        assertEquals("0.000000000000000042",h.quantity); assertEquals(listOf(old.copy(checkedAt=h.tokens.single().checkedAt)),h.tokens)
        assertNotNull(h.tokensError); assertTrue(result.errors.any { it.contains("tokens") })
    }
    @Test fun tokenSourceOffSendsNoRequestAndClearsTokens()=runBlocking {
        val hosts=mutableListOf<String>()
        val p=ethPortfolio(ethWallet.copy(tokens=listOf(Token(real,units="5",decimals=6)),tokensError="old"),ethTokens="Off")
        val h=route { hosts += it.url.host; market(it) }.refresh(p,null).holdings.single()
        assertTrue(h.tokens.isEmpty()); assertNull(h.tokensError); assertTrue(hosts.none { it.contains("blockscout") || it.contains("ethplorer") })
    }
    @Test fun lookupBudgetRefreshesQuotedFirst()=runBlocking {
        fun c(i: Int)="0x"+"%040x".format(i+1)
        val quoted=(35..39).map { c(it) }
        val looked=mutableSetOf<String>()
        val p=ethPortfolio(quotes=quoted.map { Quote(tokenAsset("ETH",it),"1","x",1000,1000) })
        val result=route {
            when {
                it.url.host=="eth.blockscout.com" -> ok((0 until 40).joinToString(",","[","]") { i -> """{"token":{"address_hash":"${c(i)}","decimals":"6","name":"n","symbol":"T","type":"ERC-20"},"value":"1"}""" })
                it.url.encodedPath.startsWith("/v1/contracts/") -> { looked += it.url.encodedPath.substringAfterLast('/'); Triple(404,"{}",null) }
                else -> market(it)
            }
        }.refresh(p,null)
        assertEquals(10,looked.size); assertTrue(looked.containsAll(quoted))
        assertEquals(10,result.unlisted.size); assertEquals(10,result.holdings.single().tokens.count { it.checkedAt != null })
    }
    @Test fun lookupBudgetIsSharedBetweenWallets()=runBlocking {
        fun c(i: Int)="0x"+"%040x".format(i+1)
        val second="0x"+"2".repeat(40)
        val base=ethPortfolio()
        val p=base.copy(holdings=base.holdings+base.holdings.single().copy(id="w2",address=second))
        val looked=mutableSetOf<String>()
        route {
            when {
                it.url.host=="eth.blockscout.com" -> { val off=if(it.url.encodedPath.contains(second)) 100 else 0; ok((0 until 40).joinToString(",","[","]") { i -> """{"token":{"address_hash":"${c(i+off)}","decimals":"6","name":"n","symbol":"T","type":"ERC-20"},"value":"1"}""" }) }
                it.url.encodedPath.startsWith("/v1/contracts/") -> { looked += it.url.encodedPath.substringAfterLast('/'); Triple(404,"{}",null) }
                else -> market(it)
            }
        }.refresh(p,null)
        assertEquals(10,looked.size)
        assertEquals(5,looked.count { it in (0 until 40).map { i -> c(i) } })
    }
    @Test fun quotaFailureStopsTokenLookups()=runBlocking {
        fun c(i: Int)="0x"+"%040x".format(i+1)
        val attempts=AtomicInteger()
        val p=ethPortfolio(quotes=listOf(Quote(tokenAsset("ETH",c(0)),"1","x",1000,1000)))
        val result=route {
            when {
                it.url.host=="eth.blockscout.com" -> ok((0 until 5).joinToString(",","[","]") { i -> """{"token":{"address_hash":"${c(i)}","decimals":"6","name":"n","symbol":"T","type":"ERC-20"},"value":"1"}""" })
                it.url.encodedPath.startsWith("/v1/contracts/") -> { attempts.incrementAndGet(); Triple(402,"{}",null) }
                else -> market(it)
            }
        }.refresh(p,null)
        assertEquals(1,attempts.get())
        assertEquals(1,result.errors.count { it.startsWith("Tokens:") })
        assertNotNull(result.quotes.first { it.asset==tokenAsset("ETH",c(0)) }.error)
        assertTrue(result.unlisted.isEmpty())
    }
    @Test fun excludedTokenIsNeverPriced()=runBlocking {
        val looked=mutableListOf<String>()
        val result=route {
            when {
                it.url.host=="eth.blockscout.com" -> blockscout(Triple(real,"250000000","ERC-20"),Triple(fakeUsdt,"999000000","ERC-20"))
                it.url.encodedPath.startsWith("/v1/contracts/") -> { looked += it.url.encodedPath.substringAfterLast('/'); market(it) }
                else -> market(it)
            }
        }.refresh(ethPortfolio(ethWallet.copy(excluded=listOf(real))),null)
        assertEquals(listOf(fakeUsdt),looked)
        val h=result.holdings.single()
        assertEquals(2,h.tokens.size); assertEquals(listOf(real),h.excluded); assertTrue(result.quotes.none { it.asset==tokenAsset("ETH",real) })
    }
    @Test fun mergeDropsExcludedQuoteAndKeepsExclusion() {
        val asset=tokenAsset("ETH",real)
        val w=ethWallet.copy(tokens=listOf(Token(real,units="5",decimals=6)),excluded=listOf(real))
        val p=ethPortfolio(w,listOf(Quote(asset,"1","x",1000,1000)))
        val merged=mergeObservations(p,p,listOf(w.copy(excluded=emptyList(),quantity="42")),emptyList())
        assertTrue(merged.quotes.isEmpty()); assertEquals(listOf(real),merged.holdings.single().excluded); assertEquals("42",merged.holdings.single().quantity)
    }
    @Test fun defiLlamaPricesByContractInBatches()=runBlocking {
        val weak="0x"+"3".repeat(40); val calls=AtomicInteger()
        val p=ethPortfolio().let { it.copy(settings=it.settings.copy(providers=it.settings.providers+("Crypto" to "DefiLlama"))) }
        val result=route {
            when {
                it.url.host=="eth.blockscout.com" -> blockscout(Triple(real,"250000000","ERC-20"),Triple(fakeUsdt,"999000000","ERC-20"),Triple(weak,"5000000","ERC-20"))
                it.url.host=="coins.llama.fi" -> { calls.incrementAndGet(); ok("""{"coins":{"coingecko:ethereum":{"price":2000,"symbol":"ETH","timestamp":1790000000,"confidence":0.99},"ethereum:${real.uppercase().replace("0X","0x")}":{"decimals":6,"symbol":"USDT","price":1.0,"timestamp":1790000000,"confidence":0.99},"ethereum:$weak":{"decimals":6,"symbol":"USDT","price":1.0,"timestamp":1790000000,"confidence":0.5}}}""") }
                else -> market(it)
            }
        }.refresh(p,null)
        assertEquals(2,calls.get())
        assertEquals("2000",result.quotes.first { it.asset=="ETH" }.usd)
        assertEquals("DefiLlama",result.quotes.first { it.asset==tokenAsset("ETH",real) }.source)
        assertEquals(setOf(tokenAsset("ETH",fakeUsdt),tokenAsset("ETH",weak)),result.unlisted)
        val merged=mergeObservations(p,p,result.holdings,result.quotes,result.unlisted)
        assertEquals(0,BigDecimal("250.000000000000084").compareTo(merged.bucketValue("b","USD")))
        assertTrue(result.errors.isEmpty())
    }
    @Test fun defiLlamaFailureKeepsCachedTokenPrices()=runBlocking {
        val asset=tokenAsset("ETH",real)
        val base=ethPortfolio(quotes=listOf(Quote(asset,"1","DefiLlama",1000,1000)))
        val p=base.copy(settings=base.settings.copy(providers=base.settings.providers+("Crypto" to "DefiLlama")))
        val result=route { when { it.url.host=="eth.blockscout.com" -> blockscout(Triple(real,"250000000","ERC-20")); it.url.host=="coins.llama.fi" -> Triple(402,"{}",null); else -> market(it) } }.refresh(p,null)
        assertNotNull(result.quotes.first { it.asset==asset }.error); assertTrue(result.unlisted.isEmpty())
    }
    // --- broker accounts ---
    private val ibHolding=Account("ib","Broker","Interactive Brokers","123456","USD","10")
    private val oandaHolding=Account("oa","Forex","OANDA","001-001-1234567-001")
    private fun statement(statements: String)=ok("""<FlexQueryResponse queryName="Capital" type="AF"><FlexStatements count="1">$statements</FlexStatements></FlexQueryResponse>""")
    private fun one(total: String="12345.67",currency: String="USD",extra: String="")=statement("""<FlexStatement accountId="U1234567" fromDate="20261001" toDate="20261002" period="LastBusinessDay" whenGenerated="20261003;091500"><AccountInformation accountId="U1234567" currency="$currency"/><EquitySummaryInBase><EquitySummaryByReportDateInBase accountId="U1234567" reportDate="20261001" total="1.00"/><EquitySummaryByReportDateInBase accountId="U1234567" reportDate="20261002" total="$total"/>$extra</EquitySummaryInBase></FlexStatement>""")
    private fun flexError(code: String,message: String)=ok("""<FlexStatementResponse timestamp="03 October, 2026 09:15 AM EDT"><Status>Warn</Status><ErrorCode>$code</ErrorCode><ErrorMessage>$message</ErrorMessage></FlexStatementResponse>""")
    private val sent=ok("""<FlexStatementResponse timestamp="03 October, 2026 09:15 AM EDT"><Status>Success</Status><ReferenceCode>9876543210</ReferenceCode><Url>https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService/GetStatement</Url></FlexStatementResponse>""")
    @Test fun flexWebServicePollsUntilTheStatementIsReady()=runBlocking {
        val polls=AtomicInteger(); val urls=mutableListOf<String>()
        val provider=route { req ->
            urls += req.url.toString()
            assertEquals("Java",req.header("User-Agent"))
            when(req.url.encodedPath) {
                "/AccountManagement/FlexWebService/SendRequest" -> { assertEquals("3",req.url.queryParameter("v")); assertEquals("123456",req.url.queryParameter("q")); assertEquals("k",req.url.queryParameter("t")); sent }
                "/AccountManagement/FlexWebService/GetStatement" -> { assertEquals("9876543210",req.url.queryParameter("q")); if(polls.incrementAndGet()<2) flexError("1019","Statement generation in progress. Please try again shortly.") else one("12345.67","EUR") }
                else -> null
            }
        }
        val h=provider.account(ibHolding)
        assertEquals("EUR",h.asset); assertEquals("12345.67",h.quantity); assertEquals("Interactive Brokers",h.broker); assertNull(h.error)
        assertEquals(java.time.LocalDate.parse("2026-10-02").atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),h.observedAt)
        assertEquals(2,polls.get()); assertTrue(urls.all { it.startsWith("https://ndcdyn.interactivebrokers.com/") })
    }
    @Test fun flexErrorsAreReportedWithoutTheToken()=runBlocking {
        val expired=route { req -> if(req.url.encodedPath.endsWith("SendRequest")) flexError("1012","Token has expired.") else null }
        try { expired.account(ibHolding); fail("Expected failure") } catch(e: ProviderFailure) { assertEquals("Token has expired; generate a new one in Client Portal",e.message) }
        val many=route { req -> if(req.url.encodedPath.endsWith("SendRequest")) sent else statement("""<FlexStatement accountId="U1"><EquitySummaryInBase><EquitySummaryByReportDateInBase reportDate="20261002" total="1" currency="USD"/></EquitySummaryInBase></FlexStatement><FlexStatement accountId="U2"/>""") }
        try { many.account(ibHolding); fail("Expected failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("2 accounts")) }
        val noNav=route { req -> if(req.url.encodedPath.endsWith("SendRequest")) sent else statement("""<FlexStatement accountId="U1"><AccountInformation currency="USD"/></FlexStatement>""") }
        try { noNav.account(ibHolding); fail("Expected failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("Net Asset Value")) }
        val negative=route { req -> if(req.url.encodedPath.endsWith("SendRequest")) sent else one("-5.00") }
        try { negative.account(ibHolding); fail("Expected failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("Negative")) }
        val entity=route { req -> if(req.url.encodedPath.endsWith("SendRequest")) ok("""<!DOCTYPE x [<!ENTITY e "x">]><FlexStatementResponse><Status>Success</Status></FlexStatementResponse>""") else null }
        try { entity.account(ibHolding); fail("Expected failure") } catch(_: IllegalArgumentException) { }
        val noKey=Providers({ "" },client { 200 to "" })
        try { noKey.account(ibHolding); fail("Expected failure") } catch(e: KeyArgument) { assertTrue(e.message!!.contains("Interactive Brokers")) }
    }
    @Test fun oandaSummaryGivesNavInAccountCurrency()=runBlocking {
        val provider=route { req ->
            assertEquals("Bearer k",req.header("Authorization"))
            if(req.url.toString()=="https://api-fxtrade.oanda.com/v3/accounts/001-001-1234567-001/summary") ok("""{"account":{"id":"001-001-1234567-001","currency":"CHF","balance":"43650.78835","NAV":"43651.12345","unrealizedPL":"0.3451"},"lastTransactionID":"6356"}""") else null
        }
        val h=provider.account(oandaHolding)
        assertEquals("CHF",h.asset); assertEquals("43651.12345",h.quantity); assertEquals("OANDA",h.broker)
        val other=route { ok("""{"account":{"id":"001-001-7654321-001","currency":"CHF","NAV":"1"}}""") }
        try { other.account(oandaHolding); fail("Expected failure") } catch(_: IllegalArgumentException) { }
    }
    @Test fun refreshUpdatesAccountsAndFollowsTheirCurrency()=runBlocking {
        val p=Portfolio(buckets=listOf(Bucket("b","B","USD")),accounts=listOf(oandaHolding,ibHolding),holdings=listOf(Holding("oa","b","Forex","USD",null,accountId="oa"),Holding("ib","b","Broker","USD","10",accountId="ib")),settings=Settings("USD",providers=providerChoices.mapValues { it.value.first() }+("Fiat" to "Frankfurter"))).linked()
        val fiat=mutableListOf<String>()
        val provider=route { req -> when {
            req.url.host=="api-fxtrade.oanda.com" -> ok("""{"account":{"id":"001-001-1234567-001","currency":"CHF","NAV":"100.5"}}""")
            req.url.encodedPath.endsWith("SendRequest") -> flexError("1015","Token is invalid.")
            req.url.host=="api.frankfurter.dev" -> { fiat += req.url.queryParameter("quotes").orEmpty(); ok("""[{"base":"USD","quote":"CHF","date":"2026-10-02","rate":0.8}]""") }
            else -> null
        } }
        val result=provider.refresh(p,null)
        val oa=result.accounts.first { it.id=="oa" }; val ib=result.accounts.first { it.id=="ib" }
        assertEquals("CHF",oa.asset); assertEquals("100.5",oa.quantity); assertNull(oa.error)
        assertEquals("10",ib.quantity); assertEquals("Token is invalid",ib.error)
        assertEquals(listOf("CHF"),fiat); assertTrue(result.holdings.isEmpty())
        val merged=mergeObservations(p,p,result.holdings,result.quotes,accounts=result.accounts).validate()
        assertEquals("CHF",merged.holdings.first { it.id=="oa" }.asset); assertEquals("100.5",merged.holdings.first { it.id=="oa" }.quantity); assertEquals("OANDA",merged.holdings.first { it.id=="oa" }.source)
        assertEquals("Token is invalid",merged.holdings.first { it.id=="ib" }.error)
        assertEquals(0,BigDecimal("135.625").compareTo(merged.bucketValueOrNull("b","USD")!!))
        assertEquals(1,result.errors.size)
        // A bucket refresh reads only the accounts linked into that bucket; an unlinked account is read by a full refresh only.
        val other=p.copy(accounts=p.accounts+Account("x","Spare","OANDA","001-001-1234567-002"))
        assertEquals(listOf("oa","ib"),provider.refresh(other,"b").accounts.map { it.id })
        assertEquals(listOf("oa","ib","x"),provider.refresh(other,null).accounts.map { it.id })
    }
    private fun secrets(vararg pairs: Pair<String,String>): (String)->String = { name -> mapOf(*pairs)[name] ?: "" }
    private fun routeWith(keys: (String)->String,reply: (Request)->Triple<Int,String,String?>?): Providers = Providers(keys,OkHttpClient.Builder().addInterceptor { chain ->
        val (status,body,location)=reply(chain.request()) ?: Triple(404,"{}",null)
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(status).message("test").apply { location?.let { header("Location",it) } }.body(body.toResponseBody()).build()
    }.build(),0)
    @Test fun trading212SummaryUsesBasicAuthAndTotalValue()=runBlocking {
        val h=Account("t","T212","Trading 212","12345678")
        val provider=routeWith(secrets("Trading 212" to "KEY","Trading 212 secret" to "SECRET")) { req ->
            assertEquals("Basic "+java.util.Base64.getEncoder().encodeToString("KEY:SECRET".toByteArray()),req.header("Authorization"))
            if(req.url.toString()=="https://live.trading212.com/api/v0/equity/account/summary") ok("""{"cash":{"availableToTrade":10.5,"inPies":0,"reservedForOrders":0},"currency":"GBP","id":12345678,"investments":{"currentValue":990.25},"totalValue":1000.75}""") else null
        }
        val r=provider.account(h)
        assertEquals("GBP",r.asset); assertEquals("1000.75",r.quantity); assertEquals("Trading 212",r.broker)
        try { routeWith(secrets("Trading 212" to "KEY")) { null }.account(h); fail("Expected failure") } catch(e: KeyArgument) { assertTrue(e.message!!.contains("Trading 212")) }
        try { routeWith(secrets("Trading 212" to "KEY","Trading 212 secret" to "S")) { ok("""{"currency":"GBP","id":99,"totalValue":1}""") }.account(h); fail("Expected failure") } catch(_: IllegalArgumentException) { }
    }
    private fun snapSignature(canonical: String,secret: String): String {
        val mac=javax.crypto.Mac.getInstance("HmacSHA256").apply { init(javax.crypto.spec.SecretKeySpec(secret.toByteArray(),"HmacSHA256")) }
        return java.util.Base64.getEncoder().encodeToString(mac.doFinal(canonical.toByteArray()))
    }
    @Test fun snapTradeRequestsAreSignedAndReadTheAccountTotal()=runBlocking {
        val id="8b5f262d-4bb9-365d-888a-202bd3b15fa1"
        val h=Account("s","Snap","SnapTrade",id)
        val paths=mutableListOf<String>()
        val provider=routeWith(secrets("SnapTrade" to "CLIENT","SnapTrade consumer key" to "CONSUMER")) { req ->
            paths += req.url.encodedPath+(if(req.method=="POST") " POST" else "")
            val query=req.url.encodedQuery.orEmpty(); assertTrue(query.matches(Regex("clientId=CLIENT&timestamp=[0-9]{10}")))
            assertEquals(snapSignature("""{"content":null,"path":"${req.url.encodedPath}","query":"$query"}""","CONSUMER"),req.header("Signature"))
            when(req.url.encodedPath) {
                "/api/v1/accounts/$id" -> ok("""{"id":"${id.uppercase()}","name":"Robinhood Individual","balance":{"total":{"amount":15363.23,"currency":"USD"}}}""")
                "/api/v1/accounts" -> ok("""[{"id":"$id","institution_name":"Robinhood","name":"Individual","number":"Q6542138443"},{"id":"zzz","institution_name":"X","name":"","number":""}]""")
                "/api/v1/snapTrade/login" -> ok("""{"redirectURI":"https://app.snaptrade.com/snapTrade/redeemToken?token=abc","sessionId":"cf371bb4"}""")
                else -> null
            }
        }
        val r=provider.account(h)
        assertEquals("USD",r.asset); assertEquals("15363.23",r.quantity)
        assertEquals(listOf(id to "Robinhood · Individual · Q6542138443","zzz" to "X"),provider.accounts("SnapTrade"))
        assertEquals("https://app.snaptrade.com/snapTrade/redeemToken?token=abc",provider.snapTradeLogin())
        assertTrue(paths.last().endsWith("POST"))
        val unsynced=routeWith(secrets("SnapTrade" to "CLIENT","SnapTrade consumer key" to "CONSUMER")) { ok("""{"id":"$id","balance":{"total":null}}""") }
        try { unsynced.account(h); fail("Expected failure") } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("sync")) }
    }
}
