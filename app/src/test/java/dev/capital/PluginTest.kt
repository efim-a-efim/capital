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

    // Flows and value history. `since` is fixed; the fake host checks the request and replays the documented response shape.
    private val holding=Holding("h","b","Broker","USD","0")
    private val since=java.time.LocalDate.parse("2025-01-10")
    @Test fun interactiveBrokersFlowsAndHistoryComeFromTheFlexSections()=runBlocking {
        // Rows built from the documented CashTransaction / EquitySummaryByReportDateInBase attribute names (values invented).
        val statement="""<FlexQueryResponse queryName="q" type="AF"><FlexStatements count="1"><FlexStatement accountId="U1234567" fromDate="20250101" toDate="20250131">
            <AccountInformation accountId="U1234567" currency="USD"/>
            <EquitySummaryInBase>
              <EquitySummaryByReportDateInBase accountId="U1234567" currency="USD" reportDate="20250109" cash="1" stock="2" total="400000.00"/>
              <EquitySummaryByReportDateInBase accountId="U1234567" currency="USD" reportDate="20250115" cash="150000.50" stock="250000.00" total="425526.00"/>
              <EquitySummaryByReportDateInBase accountId="U1234567" currency="USD" reportDate="2025-01-16" total="426000.00"/>
            </EquitySummaryInBase>
            <CashTransactions>
              <CashTransaction accountId="U1234567" currency="USD" fxRateToBase="1" description="Withdrawal via ACH" dateTime="2025-01-15;100000" amount="-5000.00" type="Deposits &amp; Withdrawals" transactionID="666662" reportDate="2025-01-15"/>
              <CashTransaction accountId="U1234567" currency="EUR" fxRateToBase="1.03" description="Wire in" dateTime="20250112;093000" amount="10000.00" type="Deposits/Withdrawals" transactionID="666661" reportDate="20250112"/>
              <CashTransaction accountId="U1234567" currency="USD" dateTime="20250105;093000" amount="300.00" type="Deposits &amp; Withdrawals" transactionID="666600"/>
              <CashTransaction accountId="U1234567" currency="USD" dateTime="20250114;093000" amount="12.50" type="Dividends" transactionID="666663"/>
            </CashTransactions>
        </FlexStatement></FlexStatements></FlexQueryResponse>"""
        fun flex(body: String)=providers("Interactive Brokers" to "TOKEN") { r ->
            when(r.url.encodedPath) {
                "/AccountManagement/FlexWebService/SendRequest" -> { assertEquals("TOKEN",r.url.queryParameter("t")); assertEquals("99",r.url.queryParameter("q")); ok("""<FlexStatementResponse timestamp="x"><Status>Success</Status><ReferenceCode>1234567</ReferenceCode><Url>u</Url></FlexStatementResponse>""") }
                "/AccountManagement/FlexWebService/GetStatement" -> { assertEquals("1234567",r.url.queryParameter("q")); ok(body) }
                else -> null
            }
        }
        val p=flex(statement); val a=account("Interactive Brokers","99")
        // Dividends are not flows, the 5 January row is before `since`; both type spellings and both date formats are read, newest last.
        assertEquals(listOf(Triple("2025-01-12","10000","EUR"),Triple("2025-01-15","-5000","USD")),p.flows(a,holding,since).map { Triple(it.date,it.amount,it.currency) })
        assertEquals(listOf("666661","666662"),p.flows(a,holding,since).map { it.ref })
        assertEquals(mapOf("2025-01-15" to "425526","2025-01-16" to "426000"),p.accountHistory(a,since))
        // read() is unchanged: newest NAV row.
        assertEquals("426000",p.account(a).quantity)
        val noSections=flex("""<FlexQueryResponse><FlexStatements count="1"><FlexStatement accountId="U1"><AccountInformation currency="USD"/></FlexStatement></FlexStatements></FlexQueryResponse>""")
        try { noSections.flows(a,holding,since); fail() } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("lacks the Cash Transactions section")) }
        try { noSections.accountHistory(a,since); fail() } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("lacks the Net Asset Value (NAV) Summary in Base section")) }
    }
    @Test fun alpacaPagesActivitiesByLastIdAndReadsDailyEquity()=runBlocking {
        // Activities built from the documented non-trade activity fields; the portfolio history is the "example-query-1d-7d" shape of the reference.
        val pages=mutableListOf<String?>()
        val p=providers("Alpaca" to "KID","Alpaca secret" to "SEC") { r ->
            assertEquals("KID",r.header("APCA-API-KEY-ID"))
            when(r.url.encodedPath) {
                "/v2/account/activities" -> {
                    assertEquals("CSD,CSW",r.url.queryParameter("activity_types")); assertEquals("asc",r.url.queryParameter("direction")); assertEquals("2025-01-09",r.url.queryParameter("after"))
                    pages += r.url.queryParameter("page_token")
                    if(pages.size==1) ok("["+(1..100).joinToString(",") { i -> """{"activity_type":"CSD","id":"2025011009000000000$i::a","date":"2025-01-${10+i%3}","net_amount":"${i}"}""" }+"]")
                    else ok("""[{"activity_type":"CSW","id":"20250120090000000::b","date":"2025-01-20","net_amount":"-250.5"},{"activity_type":"CSD","id":"20250109::c","date":"2025-01-09","net_amount":"7"}]""")
                }
                "/v2/account/portfolio/history" -> { assertEquals("2025-01-10",r.url.queryParameter("start")); assertEquals("1D",r.url.queryParameter("timeframe"))
                    ok("""{"base_value":2784.79,"cashflow":{"CSD":[0,0,100,0,0]},"equity":[2784.79,2794.79,null,2774.16,2748.73],"profit_loss":[0,10,10.6,-31.3,-25.4],"profit_loss_pct":[0,0.0035,0.0074,-0.0038,-0.0129],"timeframe":"1D","timestamp":[1736467200,1736726400,1736812800,1736899200,1736985600]}""") }
                else -> null
            }
        }
        val a=account("Alpaca","PA123"); val flows=p.flows(a,holding,since)
        assertEquals(listOf(null,"2025011009000000000100::a"),pages)
        // 100 deposits of the first page plus the withdrawal; the 9 January row is before `since`.
        assertEquals(101,flows.size); assertEquals("-250.5",flows.last().amount); assertEquals("20250120090000000::b",flows.last().ref); assertTrue(flows.all { it.currency=="USD" })
        assertEquals(mapOf("2025-01-10" to "2784.79","2025-01-13" to "2794.79","2025-01-15" to "2774.16","2025-01-16" to "2748.73"),p.accountHistory(a,since))
    }
    @Test fun tradierFlowsAreExternalEventsAndHistoryIsThePeriodBalances()=runBlocking {
        // History events use the reference's field names (ach example constructed); historical balances are the reference example.
        val p=providers("Tradier" to "T") { r ->
            assertEquals("Bearer T",r.header("Authorization"))
            when(r.url.encodedPath) {
                "/v1/accounts/VA000001/history" -> when(r.url.queryParameter("type") to r.url.queryParameter("page")) {
                    "ach" to "1" -> ok("""{"history":{"event":[{"amount":1240,"date":"2025-01-12T00:00:00Z","type":"ach","ach":{"description":"ACH DEPOSIT","quantity":0}},{"amount":1240,"date":"2025-01-12T00:00:00Z","type":"ach","ach":{"description":"ACH DEPOSIT","quantity":0}}],"page":1,"total_pages":2,"total_events":3}}""")
                    "ach" to "2" -> ok("""{"history":{"event":{"amount":-500,"date":"2025-01-20T00:00:00Z","type":"ach","description":"ACH WITHDRAWAL"},"page":2,"total_pages":2,"total_events":3}}""")
                    "wire" to "1" -> ok("""{"history":{"event":{"amount":9000,"date":"2024-12-30T00:00:00Z","type":"wire"},"page":1,"total_pages":1,"total_events":1}}""")
                    "check" to "1" -> ok("""{"history":null}""")
                    else -> null
                }
                "/v1/accounts/VA000001/historical-balances" -> { assertEquals("YEAR_3",r.url.queryParameter("period")); ok("""{"balances":[{"date":"2025-01-09","value":113302.4},{"date":"2025-07-15","value":113302.4},{"date":"2025-07-16","value":114357.75}],"delta":2780.66,"deltaPercent":2.45}""") }
                else -> null
            }
        }
        val a=account("Tradier","VA000001"); val flows=p.flows(a,holding,since)
        // Two identical same-day deposits get distinct refs; the December wire is before `since`; a null history is an empty page.
        assertEquals(listOf("1240","1240","-500"),flows.map { it.amount }); assertEquals(listOf("ach 2025-01-12 1240","ach 2025-01-12 1240 #2","ach 2025-01-20 -500"),flows.map { it.ref })
        assertEquals(mapOf("2025-07-15" to "113302.4","2025-07-16" to "114357.75"),p.accountHistory(a,since))
    }
    @Test fun tastytradeFlowsUseTheValueEffectAndHistoryAcceptsBothShapes()=runBlocking {
        // Transactions built from the documented Transaction fields; net-liq history shape is unverified, so an array and an `items` object are both replayed.
        var shape="array"
        val p=providers("tastytrade" to "RT","tastytrade client secret" to "CS") { r ->
            when(r.url.encodedPath) {
                "/oauth/token" -> ok("""{"access_token":"AT","token_type":"Bearer","expires_in":900}""")
                "/accounts/5WX01234/transactions" -> { assertEquals("Bearer AT",r.header("Authorization")); assertEquals("2025-01-10",r.url.queryParameter("start-date")); assertEquals("Money Movement",r.url.queryParameter("type"))
                    when(r.url.queryParameter("page-offset")) {
                        "0" -> ok("""{"data":{"items":[{"id":252640963,"transaction-type":"Money Movement","transaction-sub-type":"Deposit","value":"1000.0","value-effect":"Credit","net-value":"1000.0","net-value-effect":"Credit","currency":"USD","transaction-date":"2025-01-12","executed-at":"2025-01-12T21:00:00.000+00:00"},{"id":252640970,"transaction-type":"Money Movement","transaction-sub-type":"Dividend","value":"3.2","value-effect":"Credit","net-value":"3.2","net-value-effect":"Credit","currency":"USD","transaction-date":"2025-01-13"}]},"pagination":{"per-page":250,"page-offset":0,"total-items":3,"total-pages":2}}""")
                        "1" -> ok("""{"data":{"items":[{"id":252641000,"transaction-type":"Money Movement","transaction-sub-type":"Withdrawal","value":"400.0","value-effect":"Debit","net-value":"400.0","net-value-effect":"Debit","currency":"USD","transaction-date":"2025-01-20"}]},"pagination":{"per-page":250,"page-offset":1,"total-items":3,"total-pages":2}}""")
                        else -> null
                    } }
                "/accounts/5WX01234/net-liq/history" -> { assertEquals("all",r.url.queryParameter("time-back"))
                    val rows="""{"open":32100.5,"high":32650.75,"low":31980.25,"close":32520.734,"time":"2025-01-12T21:00:00Z"},{"open":1,"high":1,"low":1,"close":32600.1,"time":"2025-01-13T21:00:00Z"},{"close":1.0,"time":"2025-01-09T21:00:00Z"}"""
                    ok(if(shape=="array") """{"data":[$rows],"context":"/accounts/5WX01234/net-liq/history"}""" else """{"data":{"items":[$rows]},"context":"/accounts/5WX01234/net-liq/history"}""") }
                else -> null
            }
        }
        val a=account("tastytrade","5WX01234")
        assertEquals(listOf("2025-01-12" to "1000","2025-01-20" to "-400"),p.flows(a,holding,since).map { it.date to it.amount }); assertEquals(listOf("252640963","252641000"),p.flows(a,holding,since).map { it.ref })
        val expected=mapOf("2025-01-12" to "32520.734","2025-01-13" to "32600.1")
        assertEquals(expected,p.accountHistory(a,since)); shape="items"; assertEquals(expected,p.accountHistory(a,since))
    }
    @Test fun eToroHistoryTakesTheAccountSnapshotsOfTheLastYear()=runBlocking {
        // Built from the documented GetHistoricalBalancesResponse schema (values invented).
        val today=java.time.LocalDate.now()
        val p=providers("eToro" to "PUB","eToro user key" to "USER") { r ->
            assertEquals("PUB",r.header("x-api-key"))
            if(r.url.encodedPath=="/api/v1/balances/history") { assertEquals(today.minusDays(364).toString(),r.url.queryParameter("fromDate")); assertEquals(today.toString(),r.url.queryParameter("toDate"))
                ok("""{"gcid":1,"displayCurrency":"USD","fromDate":"2026-01-01","toDate":"2026-01-02","snapshots":[{"date":"2026-01-01","totalCurrencyIso":"USD","totalBalance":10300.9,"accountSnapshots":[{"accountId":"T1","accountType":"Trading","currency":"USD","cash":250.4,"investedAmount":9800.0,"pnl":200.0,"total":10250.4},{"accountId":"C2","accountType":"Cash","currency":"EUR","total":50.5}]},{"date":"2026-01-02","accountSnapshots":[{"accountId":"T1","accountType":"Trading","currency":"USD","total":10301.0}]}]}""") } else null
        }
        assertEquals(mapOf("2026-01-01" to "10250.4","2026-01-02" to "10301"),p.accountHistory(account("eToro","T1"),today.minusYears(3)))
        assertEquals(mapOf("2026-01-01" to "50.5"),p.accountHistory(account("eToro","C2"),today.minusYears(3)))
        assertTrue(p.flows(account("eToro","T1"),holding,since).isEmpty())
    }
    // Cash flows (deposits and withdrawals) of the brokers that list them; responses follow the documented field names (values invented where the docs give no example).
    private val flowHolding=Holding("h","b","A","USD")
    @Test fun oandaReadsTransferFundsPerIdRangeInYearWindows()=runBlocking {
        val since=java.time.LocalDate.now().minusDays(400); val id="001-001-1234567-001"; val indexFrom=mutableListOf<String>()
        val p=providers("OANDA" to "T") { r ->
            assertEquals("Bearer T",r.header("Authorization"))
            when(r.url.encodedPath) {
                "/v3/accounts/$id/summary" -> ok("""{"account":{"id":"$id","currency":"EUR","NAV":"1000.0"},"lastTransactionID":"6412"}""")
                "/v3/accounts/$id/transactions" -> { assertEquals("TRANSFER_FUNDS",r.url.queryParameter("type")); indexFrom += r.url.queryParameter("from")!!
                    if(indexFrom.size==1) ok("""{"count":3,"pageSize":1000,"type":["TRANSFER_FUNDS"],"pages":["https://api-fxtrade.oanda.com/v3/accounts/$id/transactions/idrange?from=6409&to=6410","https://api-fxtrade.oanda.com/v3/accounts/$id/transactions/idrange?from=6411&to=6412"],"lastTransactionID":"6412"}""") else ok("""{"count":0,"pageSize":1000,"pages":[],"lastTransactionID":"6412"}""") }
                "/v3/accounts/$id/transactions/idrange" -> { assertEquals("TRANSFER_FUNDS",r.url.queryParameter("type")); when(r.url.queryParameter("from")) {
                    "6409" -> ok("""{"transactions":[{"id":"6410","type":"TRANSFER_FUNDS","time":"2025-03-04T10:00:00.000000000Z","accountID":"$id","batchID":"6410","amount":"1000.00000","fundingReason":"CLIENT_FUNDING","accountBalance":"1000.00000"}],"lastTransactionID":"6412"}""")
                    "6411" -> ok("""{"transactions":[{"id":"6411","type":"TRANSFER_FUNDS_REJECT","time":"2025-04-01T10:00:00.000000000Z","amount":"5.00000"},{"id":"6412","type":"TRANSFER_FUNDS","time":"2025-05-06T08:00:00.000000000Z","amount":"-250.50000","fundingReason":"CLIENT_FUNDING","accountBalance":"749.50000"}],"lastTransactionID":"6412"}""")
                    else -> null } }
                else -> null
            }
        }
        val f=p.flows(account("OANDA",id),flowHolding,since)
        assertEquals(listOf("${since}T00:00:00Z","${since.plusDays(365)}T00:00:00Z"),indexFrom)
        assertEquals(listOf("6410" to "1000","6412" to "-250.5"),f.map { it.ref to it.amount })
        assertEquals(listOf("2025-03-04","2025-05-06"),f.map { it.date }); assertTrue(f.all { it.currency=="EUR" })
    }
    @Test fun trading212PagesDepositsAndWithdrawalsThroughNextPagePath()=runBlocking {
        val since=java.time.LocalDate.parse("2026-01-01"); val paths=mutableListOf<String>()
        val p=providers("Trading 212" to "K","Trading 212 secret" to "S") { r ->
            assertEquals("Basic ${java.util.Base64.getEncoder().encodeToString("K:S".toByteArray())}",r.header("Authorization"))
            when(r.url.encodedPath) {
                "/api/v0/equity/account/summary" -> ok("""{"id":12345,"currency":"GBP","totalValue":700.0}""")
                "/api/v0/equity/history/transactions" -> { paths += r.url.encodedPath+"?"+r.url.encodedQuery; when(r.url.queryParameter("cursor")) {
                    null -> { assertEquals("2026-01-01T00:00:00Z",r.url.queryParameter("time")); assertEquals("50",r.url.queryParameter("limit"))
                        ok("""{"items":[{"reference":"0b1c-1","type":"DEPOSIT","amount":500,"currency":"GBP","dateTime":"2026-09-01T09:00:00Z"},{"reference":"0b1c-2","type":"FEE","amount":1.5,"currency":"GBP","dateTime":"2026-09-02T09:00:00Z"}],"nextPagePath":"/api/v0/equity/history/transactions?limit=50&cursor=abc"}""") }
                    "abc" -> ok("""{"items":[{"reference":"0b1c-3","type":"WITHDRAW","amount":200,"dateTime":"2026-03-10T12:00:00Z"},{"reference":"0b1c-4","type":"DEPOSIT","amount":10,"currency":"GBP","dateTime":"2025-12-31T12:00:00Z"}],"nextPagePath":null}""")
                    else -> null } }
                else -> null
            }
        }
        val f=p.flows(account("Trading 212","12345"),flowHolding,since)
        assertEquals("/api/v0/equity/history/transactions?limit=50&cursor=abc",paths[1])
        // Oldest first; the WITHDRAW amount is negated whatever its sign; a missing currency falls back to the account summary; items before `since` are dropped.
        assertEquals(listOf(Triple("2026-03-10","-200","GBP"),Triple("2026-09-01","500","GBP")),f.map { Triple(it.date,it.amount,it.currency) })
        assertEquals(listOf("0b1c-3","0b1c-1"),f.map { it.ref })
    }
    @Test fun snapTradeSignsTheActivitiesQueryAndPagesByOffset()=runBlocking {
        val id="2f7dc9b3-1111-2222-3333-444444444444"; val offsets=mutableListOf<String?>()
        val p=providers("SnapTrade" to "CID","SnapTrade consumer key" to "KEY") { r ->
            val expected=javax.crypto.Mac.getInstance("HmacSHA256").apply { init(javax.crypto.spec.SecretKeySpec("KEY".toByteArray(),"HmacSHA256")) }
                .doFinal("""{"content":null,"path":"${r.url.encodedPath}","query":"${r.url.encodedQuery}"}""".toByteArray())
            assertEquals(java.util.Base64.getEncoder().encodeToString(expected),r.header("Signature"))
            if(r.url.encodedPath!="/api/v1/accounts/$id/activities") return@providers null
            assertEquals("2025-01-01",r.url.queryParameter("startDate")); assertEquals("CONTRIBUTION,WITHDRAWAL",r.url.queryParameter("type")); assertEquals("1000",r.url.queryParameter("limit"))
            offsets += r.url.queryParameter("offset")
            when(r.url.queryParameter("offset")) {
                "0" -> ok("""{"data":[{"id":"act-2","type":"WITHDRAWAL","amount":-300.25,"currency":{"code":"USD"},"trade_date":"2025-06-01T15:00:00.000Z","settlement_date":"2025-06-03T00:00:00.000Z","description":"Wire out","symbol":null}],"pagination":{"offset":0,"limit":1000,"total":2}}""")
                "1" -> ok("""{"data":[{"id":"act-1","type":"CONTRIBUTION","amount":1000.0,"currency":{"code":"USD"},"trade_date":null,"settlement_date":"2025-03-24T00:00:00.000Z","description":"ACH deposit","symbol":null}],"pagination":{"offset":1,"limit":1000,"total":2}}""")
                else -> null
            }
        }
        val f=p.flows(account("SnapTrade",id),flowHolding,java.time.LocalDate.parse("2025-01-01"))
        assertEquals(listOf("0","1"),offsets)
        assertEquals(listOf(Triple("2025-03-24","1000","act-1"),Triple("2025-06-01","-300.25","act-2")),f.map { Triple(it.date,it.amount,it.ref) }); assertTrue(f.all { it.currency=="USD" })
    }
    @Test fun publicComPagesMoneyMovementsByNextToken()=runBlocking {
        val tokens=mutableListOf<String?>()
        val p=providers("Public.com" to "SECRET") { r ->
            when(r.url.encodedPath) {
                "/userapiauthservice/personal/access-tokens" -> ok("""{"accessToken":"JWT"}""")
                "/userapigateway/trading/ab12/history" -> { assertEquals("Bearer JWT",r.header("Authorization")); assertEquals("2025-01-01T00:00:00Z",r.url.queryParameter("start")); tokens += r.url.queryParameter("nextToken")
                    when(r.url.queryParameter("nextToken")) {
                        null -> ok("""{"transactions":[{"timestamp":"2025-03-04T14:00:00Z","id":"t1","type":"MONEY_MOVEMENT","subType":"DEPOSIT","accountNumber":"ab12","description":"ACH","netAmount":"1000.00","direction":"INCOMING","fees":"0"},{"timestamp":"2025-03-05T14:00:00Z","id":"t2","type":"TRADE","subType":"TRADE","symbol":"AAPL","netAmount":"-500.00","direction":"OUTGOING"}],"nextToken":"n/2","start":"2025-01-01T00:00:00Z","pageSize":100}""")
                        "n/2" -> ok("""{"transactions":[{"timestamp":"2025-04-01T09:00:00-05:00","id":"t3","type":"MONEY_MOVEMENT","subType":"WITHDRAWAL","netAmount":"250.00","direction":"OUTGOING"},{"timestamp":"2025-04-02T09:00:00Z","id":"t4","type":"MONEY_MOVEMENT","subType":"DIVIDEND","netAmount":"3.10","direction":"INCOMING"}],"nextToken":null}""")
                        else -> null } }
                else -> null
            }
        }
        val f=p.flows(account("Public.com","ab12"),flowHolding,java.time.LocalDate.parse("2025-01-01"))
        assertEquals(listOf(null,"n/2"),tokens)
        assertEquals(listOf(Triple("2025-03-04","1000","t1"),Triple("2025-04-01","-250","t3")),f.map { Triple(it.date,it.amount,it.ref) }); assertTrue(f.all { it.currency=="USD" })
    }
    @Test fun tInvestPagesCashOperationsByCursor()=runBlocking {
        val bodies=mutableListOf<String>()
        val p=providers("T-Invest" to "t.TOKEN") { r ->
            assertEquals("Bearer t.TOKEN",r.header("Authorization"))
            if(r.url.encodedPath!="/rest/tinkoff.public.invest.api.contract.v1.OperationsService/GetOperationsByCursor") return@providers null
            val b=body(r)!!; bodies += b
            assertTrue(b.contains("\"accountId\":\"2000123456\"") && b.contains("\"from\":\"2025-01-01T00:00:00Z\"") && b.contains("\"OPERATION_TYPE_INPUT\"") && b.contains("\"OPERATION_TYPE_OUT_MULTI\"") && b.contains("\"state\":\"OPERATION_STATE_EXECUTED\""))
            if(b.contains("\"cursor\":\"\"")) ok("""{"hasNext":true,"nextCursor":"c2","items":[{"cursor":"1","brokerAccountId":"2000123456","id":"op-1","type":"OPERATION_TYPE_INPUT","name":"Пополнение","state":"OPERATION_STATE_EXECUTED","date":"2025-03-04T10:00:00Z","payment":{"currency":"rub","units":"100000","nano":0}},{"cursor":"2","id":"op-2","type":"OPERATION_TYPE_OUTPUT_ACQUIRING","state":"OPERATION_STATE_EXECUTED","date":"2025-05-06T10:00:00Z","payment":{"currency":"rub","units":"2500","nano":500000000}}]}""")
            else ok("""{"hasNext":false,"nextCursor":"","items":[{"cursor":"3","id":"op-3","type":"OPERATION_TYPE_INPUT_SWIFT","state":"OPERATION_STATE_EXECUTED","date":"2025-02-01T10:00:00Z","payment":{"currency":"usd","units":"1000","nano":0}}]}""")
        }
        val f=p.flows(account("T-Invest","2000123456"),flowHolding,java.time.LocalDate.parse("2025-01-01"))
        assertEquals(2,bodies.size); assertTrue(bodies[1].contains("\"cursor\":\"c2\""))
        assertEquals(listOf(Triple("2025-02-01","1000","USD"),Triple("2025-03-04","100000","RUB"),Triple("2025-05-06","-2500.5","RUB")),f.map { Triple(it.date,it.amount,it.currency) })
        assertEquals(listOf("op-3","op-1","op-2"),f.map { it.ref })
    }
    @Test fun capitalComReadsProcessedDepositsAndWithdrawalsOfTheSessionAccount()=runBlocking {
        val id="12345678901234567"; val since=java.time.LocalDate.parse("2025-01-01")
        val p=providers("Capital.com" to "KEY","Capital.com login" to "me@example.com","Capital.com key password" to "PW") { r ->
            when(r.url.encodedPath) {
                "/api/v1/session" -> ok("""{"accountType":"CFD","currencyIsoCode":"USD","currentAccountId":"$id"}""",mapOf("CST" to "C1","X-SECURITY-TOKEN" to "X1"))
                "/api/v1/history/transactions" -> { assertEquals("C1",r.header("CST")); assertEquals("X1",r.header("X-SECURITY-TOKEN"))
                    assertEquals("2025-01-01T00:00:00",r.url.queryParameter("from")); assertTrue(r.url.queryParameter("to")!!.endsWith("T23:59:59"))
                    ok("""{"transactions":[{"date":"2022-04-04T16:54:27.568","dateUtc":"2022-04-04T16:54:27.568","transactionType":"DEPOSIT","note":"Deposit","reference":"12345678","size":"20.0","currency":"USD","status":"PROCESSED"},{"date":"2025-02-01T10:00:00.000","dateUtc":"2025-02-01T09:00:00.000","transactionType":"DEPOSIT","note":"Deposit","reference":"23456789","size":"100.0","currency":"USD","status":"PROCESSED"},{"date":"2025-03-01T10:00:00.000","dateUtc":"2025-03-01T09:00:00.000","transactionType":"WITHDRAWAL","note":"Withdrawal","reference":"34567890","size":"15.5","currency":"USD","status":"PROCESSED"},{"date":"2025-03-02T10:00:00.000","dateUtc":"2025-03-02T09:00:00.000","transactionType":"WITHDRAWAL","reference":"45678901","size":"7.0","currency":"USD","status":"REJECTED"},{"date":"2025-03-03T10:00:00.000","dateUtc":"2025-03-03T09:00:00.000","instrumentName":"Gold","transactionType":"TRADE","reference":"56789012","size":"-3.2","currency":"USD","status":"PROCESSED"}]}""") }
                else -> null
            }
        }
        val f=p.flows(account("Capital.com",id),flowHolding,since)
        // Rows before `since` (the 10-minute default window is overridden), rejected rows and non-cash rows are dropped.
        assertEquals(listOf(Triple("2025-02-01","100","23456789"),Triple("2025-03-01","-15.5","34567890")),f.map { Triple(it.date,it.amount,it.ref) }); assertTrue(f.all { it.currency=="USD" })
        try { p.flows(account("Capital.com","12345678907654321"),flowHolding,since); fail() } catch(e: ProviderFailure) { assertTrue(e.message!!.contains("preferred")) }
    }
}
