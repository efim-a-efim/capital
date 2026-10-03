package dev.capital

import dev.capital.data.*
import dev.capital.domain.*
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test

/** Broker plugins against the documented request and response shapes (responses copied from the brokers' API references). */
class PluginTest {
    private class Reply(val status: Int, val body: String, val headers: Map<String,String> = emptyMap())
    private fun ok(body: String, headers: Map<String,String> = emptyMap())=Reply(200,body,headers)
    private fun body(r: Request)=r.body?.let { Buffer().also { b -> it.writeTo(b) }.readUtf8() }
    private fun providers(vararg keys: Pair<String,String>,reply: (Request)->Reply?)=Providers({ name -> mapOf(*keys)[name] ?: "" },OkHttpClient.Builder().addInterceptor { chain ->
        val r=reply(chain.request()) ?: Reply(404,"{}")
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(r.status).message("test").apply { r.headers.forEach { (k,v) -> header(k,v) } }.body(r.body.toResponseBody()).build()
    }.build(),0)
    private fun account(broker: String,id: String)=Account("a","A",broker,id)

    @Test fun alpacaReadsEquityWithKeyHeaders()=runBlocking {
        val p=providers("Alpaca" to "KID","Alpaca secret" to "SEC") { r ->
            assertEquals("KID",r.header("APCA-API-KEY-ID")); assertEquals("SEC",r.header("APCA-API-SECRET-KEY"))
            if(r.url.toString()=="https://api.alpaca.markets/v2/account") ok("""{"account_number":"PALPACA_123","currency":"USD","cash":"122086.5","equity":"123346.11","id":"1d9eed04-be39-4e01-9b84-a48ac5bbafcf","portfolio_value":"123346.11","status":"ACTIVE"}""".replace("PALPACA_123","PA123")) else null
        }
        assertEquals(listOf("PA123" to "PA123 · USD"),p.accounts("Alpaca"))
        val a=p.account(account("Alpaca","PA123")); assertEquals("USD",a.asset); assertEquals("123346.11",a.quantity)
        try { p.account(account("Alpaca","PA999")); fail() } catch(_: IllegalArgumentException) { }
    }
    @Test fun tradierListsProfileAccountsAndReadsTotalEquity()=runBlocking {
        val p=providers("Tradier" to "T") { r ->
            assertEquals("Bearer T",r.header("Authorization")); assertEquals("application/json",r.header("Accept"))
            when(r.url.encodedPath) {
                "/v1/user/profile" -> ok("""{"profile":{"id":"id-123456","name":"John Doe","account":{"account_number":"VA000001","classification":"individual","status":"active","type":"margin"}}}""")
                "/v1/accounts/VA000001/balances" -> ok("""{"balance":{"balances":{"total_equity":17798.36,"account_number":"VA000001","account_type":"margin","total_cash":6363.86}}}""")
                else -> null
            }
        }
        assertEquals(listOf("VA000001" to "VA000001 · individual · margin"),p.accounts("Tradier"))
        val a=p.account(account("Tradier","va000001")); assertEquals("USD",a.asset); assertEquals("17798.36",a.quantity)
    }
    @Test fun tastytradeTradesTheRefreshTokenAndReadsNetLiquidatingValue()=runBlocking {
        val p=providers("tastytrade" to "RT","tastytrade client secret" to "CS") { r ->
            assertTrue(r.header("User-Agent")!!.startsWith("capital/"))
            when(r.url.encodedPath) {
                "/oauth/token" -> { assertEquals("POST",r.method); val b=body(r)!!; assertTrue(b.contains("\"grant_type\":\"refresh_token\"") && b.contains("\"refresh_token\":\"RT\"") && b.contains("\"client_secret\":\"CS\"")); ok("""{"access_token":"AT","token_type":"Bearer","expires_in":900}""") }
                "/customers/me/accounts" -> { assertEquals("Bearer AT",r.header("Authorization")); ok("""{"data":{"items":[{"account":{"account-number":"5WX01234","nickname":"Main","account-type-name":"Individual","is-closed":false},"authority-level":"owner"},{"account":{"account-number":"5WX09999","is-closed":true}}]},"context":"/customers/me/accounts"}""") }
                "/accounts/5WX01234/balances" -> ok("""{"data":{"account-number":"5WX01234","currency":"USD","cash-balance":"7218.997","net-liquidating-value":"32520.734"},"context":"/accounts/5WX01234/balances"}""")
                else -> null
            }
        }
        assertEquals(listOf("5WX01234" to "Main · Individual · 5WX01234"),p.accounts("tastytrade"))
        assertEquals("32520.734",p.account(account("tastytrade","5WX01234")).quantity)
        val bad=providers("tastytrade" to "RT","tastytrade client secret" to "CS") { Reply(400,"""{"error_code":"invalid_grant","error_description":"Invalid JWT"}""") }
        try { bad.account(account("tastytrade","5WX01234")); fail() } catch(e: KeyArgument) { assertTrue(e.message!!.contains("tastytrade")) }
    }
    @Test fun publicTradesTheSecretAndReadsTotalAccountValue()=runBlocking {
        val p=providers("Public.com" to "SECRET") { r ->
            when(r.url.encodedPath) {
                "/userapiauthservice/personal/access-tokens" -> { assertTrue(body(r)!!.contains("\"secret\":\"SECRET\"")); ok("""{"accessToken":"JWT"}""") }
                "/userapigateway/trading/account" -> { assertEquals("Bearer JWT",r.header("Authorization")); ok("""{"accounts":[{"accountId":"ab12","accountType":"BROKERAGE","optionsLevel":"NONE","brokerageAccountType":"CASH","tradePermissions":"BUY_AND_SELL"}]}""") }
                "/userapigateway/trading/ab12/portfolio/v2" -> ok("""{"accountId":"ab12","accountType":"BROKERAGE","buyingPower":{"cashOnlyBuyingPower":"10"},"equity":[{"type":"CASH","value":"100.50","percentageOfPortfolio":"10"},{"type":"STOCK","value":"900","percentageOfPortfolio":"90"}],"cash":"100.50","totalAccountValue":null}""")
                else -> null
            }
        }
        assertEquals(listOf("ab12" to "BROKERAGE · CASH · ab12"),p.accounts("Public.com"))
        // totalAccountValue is nullable; the equity breakdown is summed instead.
        val a=p.account(account("Public.com","ab12")); assertEquals("USD",a.asset); assertEquals("1000.5",a.quantity)
    }
    @Test fun eToroReadsTheChosenAccountBalance()=runBlocking {
        val p=providers("eToro" to "PUB","eToro user key" to "USER") { r ->
            assertEquals("PUB",r.header("x-api-key")); assertEquals("USER",r.header("x-user-key")); assertTrue(r.header("x-request-id")!!.matches(Regex("[0-9a-f-]{36}")))
            if(r.url.encodedPath=="/api/v1/balances") ok("""{"gcid":1,"totalBalance":2185.0,"displayCurrency":"USD","balances":[{"accountId":"T1","accountType":"Trading","subType":null,"balance":2185.0,"currency":"USD","displayBalance":2185.0,"displayCurrency":"USD","exchangeRate":1},{"accountId":"C2","accountType":"Cash","balance":50.5,"currency":"EUR"}]}""") else null
        }
        assertEquals(listOf("T1" to "Trading · USD · T1","C2" to "Cash · EUR · C2"),p.accounts("eToro"))
        assertEquals("2185",p.account(account("eToro","T1")).quantity); assertEquals("EUR",p.account(account("eToro","C2")).asset)
        try { p.account(account("eToro","X9")); fail() } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("not found")) }
    }
    @Test fun indexaReadsPortfolioTotalAndAccountCurrency()=runBlocking {
        val p=providers("Indexa Capital" to "TOK") { r ->
            assertEquals("TOK",r.header("X-AUTH-TOKEN"))
            when(r.url.encodedPath) {
                "/users/me" -> ok("""{"username":"u","accounts":[{"account_number":"INDEXA01","status":"active","type":"mutual","@path":"/accounts/INDEXA01"},{"account_number":"OLD00001","status":"cancelled","type":"pension"}]}""")
                "/accounts/INDEXA01" -> ok("""{"account_number":"INDEXA01","currency":"EUR","status":"active"}""")
                "/accounts/INDEXA01/portfolio" -> ok("""{"portfolio":{"account_number":"INDEXA01","cash_amount":3898.76,"date":"2017-03-07","instruments_amount":60572.6859556,"instruments_cost":60672.6859556,"total_amount":67471.4459556},"instrument_accounts":[],"cash_accounts":[]}""")
                else -> null
            }
        }
        assertEquals(listOf("INDEXA01" to "INDEXA01 · mutual · active"),p.accounts("Indexa Capital"))
        val a=p.account(account("Indexa Capital","indexa01")); assertEquals("EUR",a.asset); assertEquals("67471.4459556",a.quantity)
        assertEquals(java.time.LocalDate.parse("2017-03-07").atStartOfDay().toInstant(java.time.ZoneOffset.UTC).toEpochMilli(),a.observedAt)
    }
    @Test fun tInvestReadsMoneyValueUnitsAndNano()=runBlocking {
        val p=providers("T-Invest" to "t.TOKEN") { r ->
            assertEquals("Bearer t.TOKEN",r.header("Authorization")); assertEquals("POST",r.method)
            when(r.url.encodedPath) {
                "/rest/tinkoff.public.invest.api.contract.v1.UsersService/GetAccounts" -> ok("""{"accounts":[{"id":"2000123456","type":"ACCOUNT_TYPE_TINKOFF","name":"Брокерский счёт","status":"ACCOUNT_STATUS_OPEN","accessLevel":"ACCOUNT_ACCESS_LEVEL_READ_ONLY"}]}""")
                "/rest/tinkoff.public.invest.api.contract.v1.OperationsService/GetPortfolio" -> { assertTrue(body(r)!!.contains("\"accountId\":\"2000123456\"")); ok("""{"totalAmountShares":{"currency":"rub","units":"100","nano":0},"totalAmountPortfolio":{"currency":"rub","units":"152340","nano":550000000},"accountId":"2000123456"}""") }
                else -> null
            }
        }
        assertEquals(listOf("2000123456" to "Брокерский счёт · TINKOFF · 2000123456"),p.accounts("T-Invest"))
        val a=p.account(account("T-Invest","2000123456")); assertEquals("RUB",a.asset); assertEquals("152340.55",a.quantity)
    }
    @Test fun alorTradesTheRefreshTokenAndListsPortfoliosFromTheToken()=runBlocking {
        val claims=java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("""{"sub":"P1","portfolios":"D12345 G14975 7500GHC","exp":1}""".toByteArray())
        val p=providers("ALOR" to "RT") { r ->
            when(r.url.toString()) {
                "https://oauth.alor.ru/refresh" -> { assertEquals("""{"token":"RT"}""",body(r)); ok("""{"AccessToken":"h.$claims.s"}""") }
                "https://api.alor.ru/md/v2/Clients/MOEX/D12345/summary" -> { assertEquals("Bearer h.$claims.s",r.header("Authorization")); ok("""{"buyingPowerAtMorning":439844.15,"buyingPower":452404.0,"profit":12560.0,"profitRate":1.93,"portfolioEvaluation":651717.0,"portfolioLiquidationValue":651717.5,"initialMargin":199313.0}""") }
                else -> null
            }
        }
        assertEquals(listOf("D12345","G14975","7500GHC"),p.accounts("ALOR").map { it.first })
        val a=p.account(account("ALOR","d12345")); assertEquals("RUB",a.asset); assertEquals("651717.5",a.quantity)
    }
    @Test fun capitalComOpensASessionAndReadsTheBalance()=runBlocking {
        val p=providers("Capital.com" to "KEY","Capital.com login" to "me@example.com","Capital.com key password" to "PW") { r ->
            when(r.url.encodedPath) {
                "/api/v1/session" -> { assertEquals("KEY",r.header("X-CAP-API-KEY")); val b=body(r)!!; assertTrue(b.contains("\"identifier\":\"me@example.com\"") && b.contains("\"password\":\"PW\""))
                    ok("""{"accountType":"CFD","currencyIsoCode":"USD","currentAccountId":"12345678901234567"}""",mapOf("CST" to "C1","X-SECURITY-TOKEN" to "X1")) }
                "/api/v1/accounts" -> { assertEquals("C1",r.header("CST")); assertEquals("X1",r.header("X-SECURITY-TOKEN"))
                    ok("""{"accounts":[{"accountId":"12345678901234567","accountName":"USD","status":"ENABLED","accountType":"CFD","preferred":true,"balance":{"balance":92.89,"deposit":90.38,"profitLoss":2.51,"available":64.66},"currency":"USD","symbol":"$"},{"accountId":"12345678907654321","accountName":"EUR","accountType":"CFD","balance":{"balance":0,"deposit":0,"profitLoss":0,"available":0},"currency":"EUR"}]}""") }
                else -> null
            }
        }
        assertEquals("USD · CFD · 12345678901234567",p.accounts("Capital.com").first().second)
        val a=p.account(account("Capital.com","12345678901234567")); assertEquals("USD",a.asset); assertEquals("92.89",a.quantity)
        val noSession=providers("Capital.com" to "KEY","Capital.com login" to "me","Capital.com key password" to "PW") { ok("{}") }
        try { noSession.account(account("Capital.com","1")); fail() } catch(e: KeyArgument) { assertTrue(e.message!!.contains("Capital.com")) }
    }
    @Test fun akahuReadsTheConnectedAccountBalance()=runBlocking {
        val id="acc_c01234567890123456789012345"
        val p=providers("Akahu" to "USER","Akahu app token" to "APP") { r ->
            assertEquals("Bearer USER",r.header("Authorization")); assertEquals("APP",r.header("X-Akahu-Id"))
            if(r.url.toString()=="https://api.akahu.io/v1/accounts") ok("""{"success":true,"items":[{"_id":"$id","connection":{"_id":"conn_cjgaac5at000001qi2yw8ftil","name":"Sharesies","connection_type":"official"},"name":"Portfolio","status":"ACTIVE","type":"INVESTMENT","balance":{"currency":"NZD","current":1250.4},"refreshed":{"balance":"2026-10-03T12:00:00.000Z"}}]}""") else null
        }
        assertEquals(listOf(id to "Sharesies · Portfolio · INVESTMENT"),p.accounts("Akahu"))
        val a=p.account(account("Akahu",id)); assertEquals("NZD",a.asset); assertEquals("1250.4",a.quantity); assertEquals(java.time.Instant.parse("2026-10-03T12:00:00Z").toEpochMilli(),a.observedAt)
    }
    @Test fun hostRejectsBadPluginResults()=runBlocking {
        val p=providers("Akahu" to "U","Akahu app token" to "A") { ok("""{"items":[{"_id":"acc_c01234567890123456789012345","balance":{"currency":"NZD","current":-5}},{"_id":"acc_d01234567890123456789012345","balance":{"currency":"nz","current":1}}]}""") }
        try { p.account(account("Akahu","acc_c01234567890123456789012345")); fail() } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("Negative")) }
        try { p.account(account("Akahu","acc_d01234567890123456789012345")); fail() } catch(_: IllegalArgumentException) { }
        val missing=providers { null }
        try { missing.account(account("Tradier","VA1")); fail() } catch(e: KeyArgument) { assertTrue(e.message!!.contains("Tradier")) }
    }
}
