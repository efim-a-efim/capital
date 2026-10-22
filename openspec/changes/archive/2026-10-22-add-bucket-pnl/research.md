# PnL research: cash flows and value history per broker

Date of research: 2026-10-22. Repository was not modified. Sources are official docs and official OpenAPI files unless a line says "third-party". "Constructed sample" means: the docs give a schema but no example, so the sample is built from the documented field names (values are invented).

## Summary table

| # | Broker | Cash flows | Value history | Verdict |
|---|--------|-----------|---------------|---------|
| 1 | Interactive Brokers (Flex) | yes: CashTransactions + Transfers | yes: EquitySummaryByReportDateInBase (daily) | full support (365 days per request) |
| 2 | OANDA v20 | yes: TRANSFER_FUNDS | no (balance per transaction only) | flows only |
| 3 | Trading 212 | yes: /equity/history/transactions | no (no equity history endpoint exists) | flows only |
| 4 | SnapTrade | yes: /accounts/{id}/activities | beta, off by default: /accounts/{id}/balanceHistory | full support, with caveats |
| 5 | Alpaca | yes: /v2/account/activities (CSD/CSW) | yes: /v2/account/portfolio/history | full support |
| 6 | Tradier | yes: /history type ach/wire/check | yes: /historical-balances | full support |
| 7 | tastytrade | yes: /transactions Money Movement | yes: /net-liq/history | full support |
| 8 | Public.com | yes: /history MONEY_MOVEMENT | no | flows only |
| 9 | eToro | no for trading accounts (cash accounts only) | yes: /balances/history (12 months) | value history only |
| 10 | Indexa Capital | partial: /cash-transactions (deposit codes undocumented) | no amounts (monthly return index only) | flows only (weak) |
| 11 | T-Invest | yes: GetOperationsByCursor | no | flows only |
| 12 | ALOR | no | no | neither |
| 13 | Capital.com | yes: /history/transactions | no | flows only |
| 14 | Akahu | no (investment accounts have no transaction data) | no | neither |

Side finding for the existing plugin (not asked, not changed): the SnapTrade OpenAPI says the legacy `/api/v1` path prefix is deprecated (header `Deprecation: @1781222400` = 2026-06-12) and the canonical path is the root, for example `/accounts/{accountId}/activities`. `SnapTrade.kt` signs and calls `/api/v1$path`. The same spec marks `userId` and `userSecret` as required query parameters on `/accounts/{accountId}/...`; `SnapTrade.kt` sends only `clientId` and `timestamp`. Check this against a real Personal key before building on it.

---

## 1. Interactive Brokers (Flex Web Service)

Plugin today: `https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService/SendRequest?t=TOKEN&q=QUERYID&v=3`, then `GetStatement?t=TOKEN&q=REFERENCE&v=3`; XML; section `EquitySummaryByReportDateInBase` is already read.

### Cash flows
- Source: Flex section `CashTransactions` (docs: "This section displays cash deposits and withdrawals into and out of your account. The section also shows dividend payments."). Doc page: https://www.ibkrguides.com/reportingreference/reportguide/cash%20transactionsfq.htm
- Second source for non-cash (in kind) moves: section `Transfers` (ACATS, internal). Fields: `Direction` ("In or out"), `Date`, `Currency`, `Position Amount`, `Position Amount in Base`, `Type` ("ACATS, Internal, etc."), `Transaction ID`. https://www.ibkrguides.com/reportingreference/reportguide/transfersfq.htm
- Auth: the same Flex token. Same Flex Query (add the sections to the existing query, or make a second query).
- Parameters on `SendRequest` (official, https://www.interactivebrokers.com/docs/web-api/api-reference/send-request.md): `t` (token), `q` (query id), `v` (2 or 3, use 3), `fd` (from date, requires `td`), `td` (to date, requires `fd`), `p` (number of days back, "A maximum of 365 days may be requested"). `p` cannot be combined with `fd`/`td`. Date format in the doc example: `fromDate = "20260801"` (yyyyMMdd). Rate limit: 1 request per second, 10 per minute per token.
- Without `fd`/`td`/`p` the period saved in the Flex Query is used. Portal choices: Last Business Day, Last Business Week, Last Month, Last Quarter, Last 30 Calendar Days, Last 365 Calendar Days, Last N Calendar Days, Month to Date, Quarter to Date, Year to Date.
- Identify a deposit or withdrawal: attribute `type` of element `CashTransaction`. Official doc lists the field `Type` ("The type of the cash transaction") but does NOT list the values. Third-party fixtures (github.com/clifton/ib-flex, tests/fixtures/activity_cash.xml, synthetic data) show `type="Deposits &amp; Withdrawals"` (decoded: `Deposits & Withdrawals`). The Cash Report and Change in NAV field labels use `Deposits/Withdrawals`. Match both spellings. Sign: `amount` positive = deposit, negative = withdrawal (fixture: 10000.00 deposit, -5000.00 withdrawal). Unique id: `transactionID`.
- Not flows: `Dividends`, `Withholding Tax`, `Broker Interest Paid`, `Broker Interest Received`, `Bond Interest ...`, `Other Fees`, `Payment In Lieu Of Dividends`, `Commission Adjustments` etc.
- Currency: attribute `currency` on each row (currency of the row), plus `fxRateToBase`. Report date attributes: `dateTime` (`20250115;093000` or `2025-01-15;093000` depending on date format setting), `date`, `reportDate`.
- Depth: per request at most 365 days (`p` max 365 is official; a Reddit thread from IBKR's official subreddit says the largest Flex range available is 365 days; the "7 years" statement history is not reachable through Flex). To go further back the app would have to store its own history from first sync. Not documented: whether `fd`/`td` accept a window older than 365 days from today.
- Paging: none. One XML document per request.
- Alternative aggregate: section `ChangeInNAV` has attribute `depositsWithdrawals` ("Includes deposits, withdrawals and cash transfers") per statement period, together with `startingValue`, `endingValue`, `mtm`, `realized`, `changeInUnrealized`, `dividends`, `interest`, `assetTransfers` (in kind). With one-day periods or the default period this gives net flow, but without dates inside the period.

### Value history
- Source: Flex section `EquitySummaryByReportDateInBase` (daily rows; the plugin takes the newest). Official field table for the NAV sections (https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20(nav)%20summary%20in%20base.htm): `Report Date`, `Cash`, `Stock`, `Options`, `Bonds`, `Interest Accruals`, `Dividend Accruals`, `Total` ("The total NAV as of the report date"). I could not open an official page named for `EquitySummaryByReportDateInBase`; the attribute names `reportDate`, `total`, `currency` come from the plugin code and from a third-party fixture (tests/fixtures/activity_daily_portfolio.xml). In that fixture the `currency` attribute is the base currency.
- Parameters, depth, paging: same as above (365 days per request, rows are one per report date within the period).
- Also usable: `ChangeInNAV` with `fromDate`, `toDate`, `startingValue`, `endingValue` (one row per statement period).

### Verdict: full support (365 days per request, store locally for more).

### Samples
CashTransaction (third-party fixture, synthetic):
```
<CashTransaction accountId="U1234567" currency="USD" fxRateToBase="1" description="Withdrawal via ACH"
  dateTime="2025-01-15;100000" amount="-5000.00" type="Deposits &amp; Withdrawals"
  transactionID="666662" reportDate="2025-01-15" />
```
EquitySummaryByReportDateInBase and ChangeInNAV (third-party fixture, abbreviated):
```
<EquitySummaryByReportDateInBase accountId="U1234567" currency="USD" reportDate="20250115" cash="150000.50" stock="250000.00" total="425526.00" ... />
<ChangeInNAV accountId="U1234567" currency="USD" fromDate="20250115" toDate="20250115" startingValue="420000.00" depositsWithdrawals="0" ... />
```

---

## 2. OANDA v20

Plugin today: `https://api-fxtrade.oanda.com/v3/accounts/{id}/summary`, header `Authorization: Bearer TOKEN`.

### Cash flows
- Endpoint: `GET /v3/accounts/{accountID}/transactions` returns an index of pages, not transactions. Then `GET /v3/accounts/{accountID}/transactions/idrange?from=ID&to=ID` (both required) returns `transactions`. Also `GET .../transactions/sinceid?id=ID`. Source: https://developer.oanda.com/rest-live-v20/transaction-ep/
- Auth: same Bearer token. Rate limit documented: 120 req/s (30 req/s for idrange).
- Parameters of `/transactions`: `from` (DateTime, inclusive, default account creation time), `to` (DateTime, inclusive, default request time), `pageSize` (default 100, maximum 1000), `type` (csv of TransactionFilter). "The maximum time range that can be requested for is 365 days between the starting time and the ending time."
- Response of `/transactions`: `from`, `to`, `pageSize`, `type`, `count`, `pages` (array of ready-made idrange URLs), `lastTransactionID`.
- Identify flows: transaction `type` = `TRANSFER_FUNDS` (filter `type=FUNDING` in the index call; the filter list contains `FUNDING`). Fields of `TransferFundsTransaction` (https://developer.oanda.com/rest-live-v20/transaction-df/): `id`, `time`, `accountID`, `batchID`, `type`, `amount` ("The amount to deposit/withdraw from the Account in the Account's home currency. A positive value indicates a deposit, a negative value indicates a withdrawal."), `fundingReason`, `comment`, `accountBalance` ("The Account's balance after funds are transferred.").
- `fundingReason` values: `CLIENT_FUNDING` (client funds transfer, external), `ACCOUNT_TRANSFER` (between two OANDA accounts; external for a per-account PnL, internal for a combined PnL), `DIVISION_MIGRATION`, `SITE_MIGRATION`, `ADJUSTMENT`. Rejected transfers appear as separate type `TRANSFER_FUNDS_REJECT`; ignore them.
- Not flows: `ORDER_FILL`, `DAILY_FINANCING`, `DIVIDEND_ADJUSTMENT`, `RESET_RESETTABLE_PL`, margin call types.
- Currency: no currency field on the transaction; it is the account home currency (`currency` from `/summary`, already read by the plugin).
- Depth: from account creation; walk in windows of at most 365 days. Paging: use the `pages` URLs.

### Value history: not available.
- No endpoint returns NAV over time. `GET /v3/accounts/{id}/changes` returns state changes since a transaction id, not a series. Every transaction carries `accountBalance` (balance after the transaction, without open-position PnL, so it is not NAV). Daily NAV cannot be rebuilt from the API.

### Verdict: flows only.

### Sample (constructed sample from the documented TransferFundsTransaction; index response is from the docs)
```
GET /v3/accounts/001-001-1234567-001/transactions?from=2025-01-01T00:00:00Z&to=2025-12-31T00:00:00Z&type=FUNDING
{ "count": 2, "from": "...", "to": "...", "pageSize": 100, "lastTransactionID": "6412",
  "pages": ["<URL>/v3/accounts/<ACCOUNT>/transactions/idrange?from=6409&to=6412"] }
GET .../transactions/idrange?from=6409&to=6412
{ "transactions": [ { "id": "6410", "type": "TRANSFER_FUNDS", "time": "2025-03-04T10:00:00.000000000Z",
  "amount": "1000.00000", "fundingReason": "CLIENT_FUNDING", "accountBalance": "1000.00000" } ],
  "lastTransactionID": "6412" }
```

---

## 3. Trading 212

Plugin today: `https://live.trading212.com/api/v0/equity/account/summary`, `Authorization: Basic base64(key:secret)`.

### Cash flows
- Endpoint: `GET https://live.trading212.com/api/v0/equity/history/transactions` (note the `/equity/` segment; the path `/api/v0/history/transactions` from the task does not exist). Source: official OpenAPI file https://docs.trading212.com/_bundle/api.json (downloaded and parsed).
- Auth: same Basic header. Rate limit: 20 requests per minute. Note from the docs: API is beta, works for Invest and Stocks ISA accounts only.
- Parameters: `cursor` (string), `time` (date-time, "Retrieve transactions starting from the specified time"), `limit` (default 20, max 50). There is no end-date parameter.
- Response: `{ "items": [HistoryTransactionItem], "nextPagePath": string }`. `nextPagePath` is null or absent on the last page; call it as given.
- `HistoryTransactionItem` fields: `reference` ("ID"), `type` (enum `WITHDRAW`, `DEPOSIT`, `FEE`, `TRANSFER`, `INTEREST_ON_FREE_CASH`, `LENDING_INTEREST`), `amount` ("Amount in the currency of the transaction"), `currency`, `dateTime`.
- Flows: `type` = `DEPOSIT` or `WITHDRAW`. `TRANSFER` is not defined in the docs (probably between own accounts or pies; treat as unknown). Sign of `amount` for `WITHDRAW` is not documented; use `abs()` and the type.
- Currency: field `currency` on each item. Docs say multi-currency accounts are not supported, values are in the primary account currency.
- Depth: not documented. Order of items not documented (pagination example for orders suggests newest first).
- Alternative: `POST /api/v0/equity/history/exports` + `GET /api/v0/equity/history/exports` build an asynchronous CSV report (body: `dataIncluded`, `timeFrom`, `timeTo`; status `Queued/Processing/Running/Canceled/Failed/Finished`, then `downloadLink`). Limits 1 request per 30 s and 1 per minute. Useful for deep history; CSV columns are not described in the OpenAPI file.

### Value history: not available.
- The OpenAPI file has these paths only: `/equity/account/summary`, `/equity/history/dividends`, `/equity/history/exports`, `/equity/history/orders`, `/equity/history/transactions`, `/equity/metadata/*`, `/equity/orders*`, `/equity/pies*`, `/equity/positions`. There is no `/equity/history` endpoint.

### Verdict: flows only.

### Sample (constructed from the schema; field names exact)
```
GET /api/v0/equity/history/transactions?limit=50
{ "items": [ { "reference": "0b1c...", "type": "DEPOSIT", "amount": 500, "currency": "GBP",
               "dateTime": "2026-09-01T09:00:00Z" } ],
  "nextPagePath": "/api/v0/equity/history/transactions?limit=50&cursor=..." }
```

---

## 4. SnapTrade

Plugin today: `https://api.snaptrade.com/api/v1/accounts/{id}` signed with HMAC (clientId, consumer key). OpenAPI: https://docs.snaptrade.com/openapi.yaml (downloaded and parsed).

### Cash flows
- Endpoint: `GET /accounts/{accountId}/activities` ("Returns all historical transactions for the specified account"). Plugin-style path would be `/api/v1/accounts/{accountId}/activities`.
- Auth: same signed request. Spec marks `userId` and `userSecret` as required query parameters (see side finding above).
- Parameters: `startDate` (inclusive; default = first transaction known to SnapTrade, by `trade_date`), `endDate` (inclusive), `offset` (default 0), `limit` (default 1000, max 1000 per request), `type` (csv filter).
- Response: `{ "data": [AccountUniversalActivity], "pagination": { "offset", "limit", "total" } }`. Sorted newest first by `trade_date`. "This endpoint returns Daily data ... cached and refreshed once a day."
- Identify flows: `type` = `CONTRIBUTION` ("Cash contribution") and `WITHDRAWAL` ("Cash withdrawal"). Sign: `amount` "can be positive or negative ... transactions that positively affect the account balance (like sell, deposits, dividends) will have a positive amount, ... negative (like buy, withdrawals, fees)". `TRANSFER` ("Transfer of assets from one account to another") is in-kind. Other types: BUY, SELL, DIVIDEND, SUBSTITUTE_DIVIDEND, REI, STOCK_DIVIDEND, INTEREST, FEE, TAX, OPTIONEXPIRATION, OPTIONASSIGNMENT, OPTIONEXERCISE, SPLIT. Types are "best effort" normalised per brokerage, so some brokers may emit other strings.
- Currency: `currency` object (code in `currency.code` per the `Currency` schema) on each activity. `currency` can be null when `currency_universal_symbol` is used (crypto). Date: `trade_date` (date-time, granularity depends on the brokerage), `settlement_date`.
- Depth: "first transaction known to SnapTrade"; real depth depends on each brokerage connection (SnapTrade holds a brokerage table, not fetched here).

### Value history: beta, off by default.
- Endpoint: `GET /accounts/{accountId}/balanceHistory` ("List historical account total value", `x-beta: true`). "An experimental endpoint that returns estimated historical total account value ... disabled by default, and has a maximum lookback of 1 year ... may contain gaps ... best used for charting ... should not be relied on as a complete or exact record." Needs the free add-on enabled in the SnapTrade Customer Dashboard (Add-ons page); otherwise 403 `403FeatureNotEnabledResponse`.
- Parameters: only `userId`, `userSecret` (no date range). Response `AccountValueHistoryResponse`: `history[]` of `{ "date": "2026-01-01", "total_value": "15363.23" }` and `currency` (ISO 4217). Depth 1 year. Dynamically generated: replace the dataset on every request.
- Current balances endpoint `/accounts/{id}/balances` is a snapshot, not history.

### Verdict: full support, but value history is an estimate, beta, opt-in, 1 year. Flows depth depends on the brokerage. Account-level rate limit for Personal users: 10 requests per minute per account.

### Samples (field names from the schema; values invented)
```
GET /accounts/{id}/activities?startDate=2025-01-01&type=CONTRIBUTION,WITHDRAWAL&limit=1000&offset=0
{ "data": [ { "id": "2f7dc9b3-...", "type": "CONTRIBUTION", "amount": 1000.0, "currency": {"code": "USD"},
              "trade_date": "2025-03-22T16:27:55.000Z", "settlement_date": "2025-03-24T00:00:00.000Z",
              "description": "ACH deposit", "symbol": null } ],
  "pagination": { "offset": 0, "limit": 1000, "total": 1 } }
GET /accounts/{id}/balanceHistory
{ "history": [ { "date": "2026-01-01", "total_value": "15363.23" } ], "currency": "USD" }
```

---

## 5. Alpaca

Plugin today: `https://api.alpaca.markets/v2/account`, headers `APCA-API-KEY-ID`, `APCA-API-SECRET-KEY`.

### Cash flows
- Endpoint: `GET https://api.alpaca.markets/v2/account/activities` (also `/v2/account/activities/{activity_type}`). Docs: https://docs.alpaca.markets/us/reference/getaccountactivities-2.md and https://docs.alpaca.markets/us/docs/account-activities.md
- Auth: same two headers.
- Parameters: `activity_types` (csv), `category`, `order_id`, `date` (one day, by creation date), `until`, `after` (ISO date or datetime; filter by creation date `created_at`, not settlement date; "For non-trade activities such as fees, the creation date is typically the day after the trade date"), `direction` (asc/desc), `page_size`, `page_token` (id of the last activity of the previous page). "If `date` is not specified, the default and maximum value is 100" for `page_size`.
- Identify flows: `activity_type` = `CSD` ("Cash deposit(+)") and `CSW` ("Cash withdrawal(-)"); `TRANS` is the group "Cash transactions (both CSD and CSW)". Also `ACATC` (ACATS cash in/out), `JNLC` (journal, cash; moves between Alpaca accounts), `FOPT` (free of payment transfer, securities). Not flows: `FILL`, `DIV`, `DIVFEE`, `DIVNRA`, `FEE`, `CFEE`, `INT`, `INTNRA`, `INTTW`, `PTC`, `PTR`, `MISC`, corporate action types.
- Non-trade activity (NTA) fields: `activity_type`, `id` (format `20190801011955195::5f59...`), `date`, `net_amount` (string, "positive or negative"), `symbol`, `qty`, `per_share_amount`. No currency field: account currency (`currency` in `/v2/account`, USD for US accounts).
- Depth: not documented. Paging: `page_token` with `direction=asc`, up to 100 per page.
- Second route: `/v2/account/portfolio/history` accepts `cashflow_types` ("The cashflow activities to include in the report. One of 'ALL', 'NONE', or a comma-separated list of activity types") and returns `cashflow` ("Accumulated value in dollar amount as of the end of each time window", an object keyed by activity type with one array entry per timestamp). The semantics are only described in two sentences; test before using.

### Value history
- Endpoint: `GET https://api.alpaca.markets/v2/account/portfolio/history` (https://docs.alpaca.markets/reference/getaccountportfoliohistory-1.md).
- Parameters: `period` (number + unit D/W/M/A, for example `1A`; not allowed with both start and end; default `1M`), `timeframe` (`1Min`,`5Min`,`15Min`,`1H`,`1D`; "For queries with longer than 30 days of `period`, the system only accepts 1D"), `start`, `end` (ISO timestamps), `intraday_reporting`, `pnl_reset`, `cashflow_types`, `extended_hours` (deprecated).
- Response `PortfolioHistory`: `timestamp[]` (UNIX epoch seconds, left-labelled), `equity[]`, `profit_loss[]`, `profit_loss_pct[]`, `base_value`, `base_value_asof`, `timeframe`, `cashflow`. For `1D` "the response will only contain entries for days when the market is open". Equity can be null in entries.
- Depth: not documented (`period=1A`, `start` any date); account creation is the natural lower bound.
- Currency: USD.

### Verdict: full support.

### Samples
Activities (constructed from the documented NTA schema):
```
GET /v2/account/activities?activity_types=CSD,CSW&direction=asc&page_size=100
[ { "activity_type": "CSD", "id": "20240102090000000::8efc7b9a-...", "date": "2024-01-02", "net_amount": "1000" } ]
```
Portfolio history (schema, `GET /v2/account/portfolio/history?period=1A&timeframe=1D`):
```
{ "timestamp": [1700000000, 1700086400], "equity": [10000.0, 10120.5], "profit_loss": [0.0, 120.5],
  "profit_loss_pct": [0.0, 0.012], "base_value": 10000.0, "base_value_asof": "2023-11-14", "timeframe": "1D" }
```

---

## 6. Tradier

Plugin today: `https://api.tradier.com/v1/accounts/{id}/balances`, `Authorization: Bearer TOKEN`, `Accept: application/json`.

### Cash flows
- Endpoint: `GET /v1/accounts/{account_id}/history` (https://docs.tradier.com/reference/brokerage-api-accounts-get-account-history.md). Data from the clearing firm: "Updated nightly (not intraday)", no time of day, live accounts only (no sandbox history).
- Auth: same Bearer token and `Accept: application/json` (required).
- Parameters: `page` (default 1), `limit` (default 25; no maximum documented), `type` (one of `trade`, `option`, `ach`, `wire`, `dividend`, `fee`, `tax`, `journal`, `check`, `transfer`, `adjustment`). There is NO date-range parameter; page through everything and filter locally.
- Response: `history.event[]` (one event comes as an object, not an array, in Tradier JSON; the plugin already handles that for accounts), plus `history.page`, `history.total_pages`, `history.total_events`.
- Event fields: `date` (date-time, midnight UTC in the guide), `type`, `amount` (total value), `description`, `symbol`, `quantity`, `price`, `commission`. The guide (https://docs.tradier.com/docs/history.md) shows details nested under a key named after the type, for example `"journal": {"description": "TFR FROM ACCT VA-00000-0", "quantity": 0}`; the reference page shows a flat event. Handle both.
- Identify flows: `type` in `ach`, `wire`, `check` (external). `journal` = transfer between Tradier accounts (guide example: `amount` 2500.00 "TFR FROM ACCT ..." and -3000.00 "6YA-00005 TO 6YA-00102"); `transfer` is not explained. Sign: positive in, negative out (from the guide's journal examples). No documented example for ach/wire/check.
- Currency: none; USD (as the plugin assumes). Depth: not documented.

### Value history
- Endpoint: `GET /v1/accounts/{account_id}/historical-balances?period=...` (https://docs.tradier.com/reference/brokerage-api-accounts-get-account-historical-balance.md; listed as "Get Account's Balances Overtime": "Get the historical account balances to track value over time").
- Parameter: `period` enum `WEEK`, `MONTH`, `YTD`, `YEAR`, `YEAR_3`, `YEAR_5`, `ALL`. No explicit dates.
- Response: `balances[]` of `{ "date": "2025-07-15", "value": 113302.4 }`, plus `delta` and `deltaPercent`. Daily points on trading days. Whether `value` equals `total_equity` is not stated.
- Depth: up to `ALL`.

### Verdict: full support. (Flows via `/history` have no date filter; value history exists only on the reference page, not in the guides.)

### Samples (from the docs)
```
GET /v1/accounts/6YA05678/history?type=ach&page=1&limit=100   (flat shape from the reference page)
{ "history": { "event": [ { "date": "2015-04-01T15:25:47.000Z", "type": "ach", "amount": 1240, "description": "..." } ],
  "page": 1, "total_pages": 1, "total_events": 1 } }
GET /v1/accounts/6YA05678/historical-balances?period=ALL
{ "balances": [ { "date": "2025-07-15", "value": 113302.4 }, { "date": "2025-07-16", "value": 114357.75 } ],
  "delta": 2780.66, "deltaPercent": 2.45 }
```
(The first sample uses the documented field names; the ach example itself is constructed.)

---

## 7. tastytrade

Plugin today: `https://api.tastyworks.com/accounts/{id}/balances`, OAuth refresh token to 15-minute bearer token, `User-Agent` required. Docs: https://developer.tastytrade.com (Markdown pages and OpenAPI files, parsed).

### Cash flows
- Endpoint: `GET /accounts/{account_number}/transactions` (https://developer.tastytrade.com/reference/transactions/getAccountsAccountNumberTransactions.md).
- Auth: same bearer token.
- Parameters: `start-date`, `end-date` (date), `start-at`, `end-at` (date-time), `per-page` (default 250, maximum 2000), `page-offset` (page number, default 0), `sort` (`Desc` default or `Asc`), `type` (one) or `types[]` (several; mutually exclusive), `sub-type[]`, `currency`, `action`, `symbol`, `instrument-type`, `underlying-symbol`, `futures-symbol`, `partition-key`.
- Response: `{ "data": { "items": [Transaction] }, "pagination": { "per-page", "page-offset", "item-offset", "total-items", "total-pages", "current-item-count" }, "context": ... }`. Stop when `page-offset + 1 >= total-pages`.
- Identify flows: `transaction-type` = `Money Movement` with `transaction-sub-type` = `Deposit` or `Withdrawal` (the schema lists these example sub-types: `Dividend`, `Deposit`, `Withdrawal`, `Credit Interest`, `Debit Interest`, `Assignment`, `Exercise`, `Expiration`, `Fee`, `Transfer`). `Transfer` and the type `Administrative Transfer` are in kind or between accounts. Other transaction types: `Receive Deliver`, `Trade`.
- Amount: `value` and `net-value` are decimal strings; direction in `value-effect` / `net-value-effect` (`Credit`, `Debit`, `None`), so the sign must be taken from the effect field. Currency: `currency`. Date: `transaction-date` (ledger date), `executed-at` (date-time). Unique id: `id` (integer).
- Depth: not documented.

### Value history
- Endpoint: `GET /accounts/{accountNumber}/net-liq/history` (https://developer.tastytrade.com/reference/net-liquidating-value-history/getNetLiqHistory.md): "net liquidating value history ... as open/high/low/close data".
- Parameters: `time-back` (`1d`,`1w`,`1m`,`3m`,`6m`,`1y`,`all`), or `start-time` / `end-time` (format `2011-12-03T10:15:30+01:00[Europe/Paris]`), `interval` (values not documented).
- Response: `{ "data": NetLiqOhlc, "context": ... }` where `NetLiqOhlc` has `open`, `high`, `low`, `close`, `totalOpen`, `totalHigh`, `totalLow`, `totalClose`, `pendingCashOpen/High/Low/Close`, `time`. The OpenAPI file types `data` as a single object although the endpoint returns a series; the real array shape was not verified. Use `close` (or `totalClose`) per day. Currency not in the response (account currency, USD).
- Depth: `time-back=all`.
- Also: `GET /accounts/{account_number}/balance-snapshots?time-of-day=EOD&start-date=...&end-date=...` (required `time-of-day` `BOD`/`EOD`, `per-page` max 2000, `currency` default USD) returns `AccountBalanceSnapshot` items twice a day; fields not inspected.

### Verdict: full support.

### Samples (constructed from the schema)
```
GET /accounts/5WX01234/transactions?types[]=Money%20Movement&sort=Asc&per-page=250&page-offset=0
{ "data": { "items": [ { "id": 252640963, "transaction-type": "Money Movement", "transaction-sub-type": "Deposit",
   "value": "1000.0", "value-effect": "Credit", "net-value": "1000.0", "currency": "USD",
   "transaction-date": "2024-01-12", "executed-at": "2024-01-12T21:00:00.000+00:00" } ] },
  "pagination": { "per-page": 250, "page-offset": 0, "total-items": 1, "total-pages": 1 } }
GET /accounts/5WX01234/net-liq/history?time-back=all
{ "data": { "open": 32100.5, "high": 32650.75, "low": 31980.25, "close": 32520.734, "time": "2024-01-12T21:00:00Z" }, "context": "/accounts/{accountNumber}/net-liq/history" }
```

---

## 8. Public.com (Individual API)

Plugin today: `https://api.public.com/userapiauthservice/personal/access-tokens` (secret to token), then `GET /userapigateway/trading/{id}/portfolio/v2`.

### Cash flows
- Endpoint: `GET https://api.public.com/userapigateway/trading/{accountId}/history` (https://public.com/api/docs/resources/account-details/get-history): "paginated list of historical account events - including trades, money movements, and position adjustments ... optional time-range filtering (start/end) and pagination via the nextToken".
- Auth: same bearer access token.
- Parameters: `start`, `end` (ISO 8601 with timezone, for example `2025-01-15T09:00:00-05:00`), `pageSize` (maximum not documented), `nextToken`.
- Response: `{ "transactions": [...], "nextToken", "start", "end", "pageSize" }`.
- Transaction fields (from the page's embedded OpenAPI): `timestamp`, `id`, `type` (`TRADE`, `MONEY_MOVEMENT`, `POSITION_ADJUSTMENT`), `subType` (`DEPOSIT`, `WITHDRAWAL`, `DEPOSIT_RETURNED`, `WITHDRAWAL_RETURNED`, `DIVIDEND`, `FEE`, `REWARD`, `TREASURY_BILL_TRANSFER`, `INTEREST`, `TRADE`, `TRANSFER`, `MISC`), `accountNumber`, `symbol`, `securityType`, `side`, `description`, `netAmount` (string), `principalAmount`, `quantity`, `direction` (`INCOMING`, `OUTGOING`), `fees`.
- Identify flows: `type` = `MONEY_MOVEMENT` and `subType` in `DEPOSIT`, `WITHDRAWAL`; reversals `DEPOSIT_RETURNED` (money out) and `WITHDRAWAL_RETURNED` (money in) should be counted too. `direction` gives the sign; whether `netAmount` is signed is not documented. `TRANSFER` and `TREASURY_BILL_TRANSFER` are unclear/internal; `REWARD` is a bonus (decide separately).
- Currency: no field; USD (as the plugin assumes). Depth: not documented.

### Value history: not available.
- The docs list (portfolio v2, history, orders, market data, tax lots, event contracts, instruments) has no endpoint for past account value. `portfolio/v2` is a snapshot only.

### Verdict: flows only.

### Sample (docs placeholder shape; values invented for the money movement)
```
GET /userapigateway/trading/{accountId}/history?start=2025-01-01T00:00:00Z&end=2025-12-31T00:00:00Z&pageSize=100
{ "transactions": [ { "timestamp": "2025-03-04T14:00:00Z", "id": "string", "type": "MONEY_MOVEMENT", "subType": "DEPOSIT",
   "accountNumber": "string", "description": "string", "netAmount": "1000.00", "direction": "INCOMING", "fees": "0" } ],
  "nextToken": "string", "start": "2025-01-01T00:00:00Z", "end": "2025-12-31T00:00:00Z", "pageSize": 100 }
```

---

## 9. eToro (Public API)

Plugin today: `GET https://public-api.etoro.com/api/v1/balances?includeSubAccounts=true`, headers `x-api-key`, `x-user-key`, `x-request-id` (uuid). Docs: https://api-portal.etoro.com (Markdown pages by appending `.md`; `curl` needs a browser User-Agent).

### Cash flows: not available for trading accounts.
- Checked the full endpoint index (https://api-portal.etoro.com/llms.txt). Only these relate to money movement: `GET /api/v1/money/accounts/cash/{accountId}/transactions` (eToro Money cash account only; fields `transactionType` bankTransfer/internalTransfer/card/balanceAdjustment, `direction` credit/debit, `amount`, `currency`, `postedAt`, cursor paging with `pageSize` max 500 and `pageToken`), internal transfer endpoints (`Transfer` group, write/eligibility), and partner-only deposit endpoints (First time deposit, crypto deposit). There is no list of deposits or withdrawals of the trading (brokerage) account. `GET /api/v1/trading/info/trade/history` returns closed positions only (see below).
- Closed positions: `GET /api/v1/trading/info/trade/history?minDate=YYYY-MM-DD&page=&pageSize=` (`minDate` required; "Keep each request's lookback to less than 1 year"). Fields: `netProfit`, `closeTimestamp`, `openTimestamp`, `positionId`, `instrumentId`, `investment`, `initialInvestment`, `fees`, `units`, ... This gives realized PnL, not cash flows.
- Crypto and cash withdrawal endpoints were withdrawn (changelog 2026-08-25 and 2026-07-29), so the API is also unstable.

### Value history
- Endpoints: `GET /api/v1/balances/history` (all account types) and `GET /api/v1/balances/{accountType}/{accountId}/history` (`accountType` lowercase: `trading`, `cash`, `options`, `crypto`, `moneyfarm`, `spaceship`, `plaee`).
- Auth: same key pair. Rate limit 60 requests per 60 s (shared).
- Parameters: `fromDate`, `toDate` (YYYY-MM-DD, inclusive; default `toDate` minus 30 days; "Must be within the last 12 months"; maximum range 365 days), `displayCurrency` (ISO 4217, default USD), `includeSubAccounts` (default false), and `accountTypes` csv on the aggregate endpoint.
- Description: "end-of-day balance snapshots ... History is available for the last 12 months."
- Response (`GetHistoricalBalancesResponse`): `gcid`, `displayCurrency`, `fromDate`, `toDate`, `snapshots[]`. Each snapshot: `date`, `totalCurrencyIso`, `totalCash`, `totalInvestedAmount`, `totalPnl`, `totalBalance` ("Total end-of-day equity across all accounts in native currency ... not available cash"), `displayTotal*`, `accountSnapshots[]` with `accountId`, `accountType`, `currency` (native currency), `cash`, `investedAmount`, `pnl`, `total`, `displayTotal`, `usdRate`, `exchangeRate`. Use `accountSnapshots[].total` with `accountSnapshots[].currency` for a single account. Snapshots are "one entry per day in the requested range".
- Depth: 12 months, so the app must store the series itself from first sync.

### Verdict: value history only.

### Sample (constructed from the documented schema)
```
GET /api/v1/balances/trading/{accountId}/history?fromDate=2026-01-01&toDate=2026-10-21&displayCurrency=USD
{ "fromDate": "2026-01-01", "toDate": "2026-10-21", "displayCurrency": "USD",
  "snapshots": [ { "date": "2026-01-01", "totalCurrencyIso": "USD", "totalBalance": 10250.4, "displayTotalBalance": 10250.4,
     "accountSnapshots": [ { "accountId": "123", "accountType": "Trading", "currency": "USD", "cash": 250.4,
        "investedAmount": 9800.0, "pnl": 200.0, "total": 10250.4 } ] } ] }
```

---

## 10. Indexa Capital

Plugin today: `https://api.indexacapital.com`, header `X-AUTH-TOKEN`. Official reference (RAML): https://indexacapital.com/en/api-rest-v1 (v1.6, downloaded and parsed).

### Cash flows: partial
- Endpoint: `GET /accounts/{account_number}/cash-transactions` ("Retrieve a list of all cash transactions of the given Indexa account"). No query parameters are documented (no date range, no paging); the response is an array.
- Fields in the example: `account_number`, `reference`, `date`, `amount` (signed: -3 for a fee), `currency` (EUR), `operation_code` (1101 "APERTURA", 224 "CARGO COMISIONES TPSO. FONDOS", 5324 "COMISION TRASPASO FONDO", 20 "SUSCRIPCION FONDOS INVERSION", 5120 "SUSCRIPCION FONDOS INVERSION SF", 4567 "RETROCESION COMISION MTTO C/C"), `operation_type` (Spanish text), `status` ("closed"), `fees`, `comments`, `instrument_transaction`.
- The docs do NOT show an example or a code for an incoming bank transfer or a withdrawal, and give no enum of `operation_code`/`operation_type`. Classifying a deposit or withdrawal needs the codes from real data (third-party MCP server dacmail/indexa-capital-mcp-server says the endpoint covers "contributions, withdrawals, fund subscriptions and redemptions, dividend reinvestments, fees" and itself says the shape is unconfirmed). Not safe to implement from docs alone.
- Related: `GET /accounts/{account_number}/instrument-transactions` (query `from_date`, `include_from_date`) lists fund purchases/sales, not flows; `GET .../pending-transactions`; `GET .../cash-requests` (withdrawal requests; response body "not documented yet").
- Currency: `currency` per row. Depth: not documented.

### Value history: not available as amounts
- `GET /accounts/{account_number}/performance` returns `performance` (`period[]`, `expected[]`, `worst[]`, `best[]`, `real[]`, base-100 index series), `history` (map of month-end date to cumulative return ratio, for example `"2016-12-31": 1.00918...`) and `benchmark` (per month `return`, `percentage_return`, `benchmark`). Monthly, return index only, no euro amounts, no parameters.
- `GET /accounts/{account_number}/portfolio` is current only (`total_amount`, `cash_amount`, `instruments_amount`, `instruments_cost`, `date`). The cost and value in it give unrealized PnL at one date.
- Practical alternative for this broker: use the `history` return index directly as the performance figure, or store `total_amount` daily in the app.

### Verdict: flows only (weak: deposit identification is unproven); a monthly return index is available.

### Sample (docs, trimmed)
```
GET /accounts/NK1NUTP1/cash-transactions
[ { "account_number": "INDEXA01", "reference": "416242141", "date": "2015-12-12", "currency": "EUR", "amount": 0,
    "operation_code": 1101, "operation_type": "APERTURA ", "status": "closed", "fees": 0, "comments": "APERTURA ..." },
  { "account_number": "INDEXA01", "date": "2016-03-31", "currency": "EUR", "amount": -3, "operation_code": 224,
    "operation_type": "CARGO COMISIONES TPSO. FONDOS", "status": "closed" } ]
GET /accounts/NK1NUTP1/performance   ->  "history": { "2016-12-31": 1.0091817281834736, "2017-01-31": 1.011664432599013 }
```

---

## 11. T-Invest (T-Bank)

Plugin today: REST gateway `https://invest-public-api.tbank.ru/rest/tinkoff.public.invest.api.contract.v1.OperationsService/GetPortfolio`, `Authorization: Bearer TOKEN`. Contract: https://github.com/RussianInvestments/investAPI/blob/main/src/docs/contracts/operations.proto (downloaded and parsed). JSON field names are camelCase, enums are strings.

### Cash flows
- Method: `OperationsService/GetOperationsByCursor` (POST). Also exists: `GetOperations` (no paging) and `GetBrokerReport` (async report).
- Auth: same token (read-only token is enough).
- Request fields: `accountId` (required), `instrumentId`, `from` and `to` (UTC timestamps), `cursor`, `limit` (default 100, maximum 1000), `operationTypes` (list of `OperationType`), `state` (`OPERATION_STATE_EXECUTED` etc.), `withoutCommissions`, `withoutTrades`, `withoutOvernights`.
- Response: `hasNext`, `nextCursor`, `items[]` (`OperationItem`): `cursor`, `brokerAccountId`, `id`, `parentOperationId`, `name`, `date`, `type`, `description`, `state`, `payment` (`MoneyValue`: `currency`, `units`, `nano`; units and nano carry the sign), `price`, `commission`, `yield`, `instrumentUid`, `figi`, `ticker`, `quantity`, `childOperations`, ...
- Identify flows (cash): `type` in `OPERATION_TYPE_INPUT` ("Пополнение брокерского счета"), `OPERATION_TYPE_OUTPUT` ("Вывод денежных средств"), `OPERATION_TYPE_INPUT_SWIFT` / `OPERATION_TYPE_OUTPUT_SWIFT`, `OPERATION_TYPE_INPUT_ACQUIRING` ("Перевод с карты") / `OPERATION_TYPE_OUTPUT_ACQUIRING` ("Перевод на карту"), `OPERATION_TYPE_INP_MULTI` / `OPERATION_TYPE_OUT_MULTI` (multi-currency deposit/withdrawal). Sign is in `payment` (not documented whether outputs are negative; take absolute value and the type).
- Flows in kind (securities, no cash; `payment` meaning not documented, value must be taken from market price): `OPERATION_TYPE_INPUT_SECURITIES` ("Перевод ценных бумаг из другого депозитария"), `OPERATION_TYPE_OUTPUT_SECURITIES`, `OPERATION_TYPE_TRANS_IIS_BS`, `OPERATION_TYPE_TRANS_BS_BS`.
- Not flows: trades, coupons, dividends (`OPERATION_TYPE_DIVIDEND`, `OPERATION_TYPE_DIV_EXT` pays out to a card), taxes, `OPERATION_TYPE_OUTPUT_PENALTY` and `OPERATION_TYPE_OUT_FEE` (fees for withdrawal), `OPERATION_TYPE_SERVICE_FEE`, `OPERATION_TYPE_BROKER_FEE`, overnight and variation margin types.
- Currency: `payment.currency` (lowercase codes such as `rub`, `usd`; the plugin uppercases). An account can hold several currencies, so the value of each flow needs conversion to the account value currency (the plugin asks `GetPortfolio` in `RUB`).
- Depth: no limit stated in the contract; `from`/`to` optional. Pacing: use `cursor`/`nextCursor` until `hasNext` is false. The docs page "operations_problems" (developer.tbank.ru/invest/services/operations/operations_problems) was not readable.

### Value history: not available.
- `GetPortfolio` returns the current portfolio only (the plugin reads `totalAmountPortfolio`). The service list is: `GetOperations`, `GetPortfolio`, `GetPositions`, `GetWithdrawLimits`, `GetBrokerReport`, `GetDividendsForeignIssuer`, `GetOperationsByCursor` and two streams. `GetBrokerReport` (request: `accountId`, `from`, `to`; async task id, then paged `brokerReport`) is a trade/cash report and not a NAV series.

### Verdict: flows only.

### Sample (constructed from the proto; REST JSON mapping)
```
POST .../OperationsService/GetOperationsByCursor
{ "accountId": "2000123456", "from": "2025-01-01T00:00:00Z", "to": "2025-12-31T00:00:00Z", "limit": 1000,
  "operationTypes": ["OPERATION_TYPE_INPUT","OPERATION_TYPE_OUTPUT"], "state": "OPERATION_STATE_EXECUTED" }
{ "hasNext": false, "nextCursor": "",
  "items": [ { "cursor": "1", "id": "...", "type": "OPERATION_TYPE_INPUT", "state": "OPERATION_STATE_EXECUTED",
    "date": "2025-03-04T10:00:00Z", "payment": { "currency": "rub", "units": "100000", "nano": 0 } } ] }
```

---

## 12. ALOR

Plugin today: `https://api.alor.ru/md/v2/Clients/MOEX/{portfolio}/summary`, refresh token to access token via `https://oauth.alor.ru/refresh`.

### Cash flows: not available
- Checked the official OpenAPI file (https://github.com/alor-broker/OpenAPI.SDK, `WarpOpenAPIv2.json`, 82 paths) and alor.dev. Account-related read paths: `/md/v2/Clients/{exchange}/{portfolio}/orders`, `/stoporders`, `/summary`, `/positions`, `/trades` (current session), `/risk`, `/fortsrisk`; `/md/v2/Stats/{exchange}/{portfolio}/history/trades` and `/md/stats/{exchange}/{portfolio}/history/trades` (past sessions: query `dateFrom`, `ticker`, `side`, `from`, `limit`, `orderByTradeDate`, `descending`, `withRepo`, `format`; "не более 1000 сделок за один запрос"); `/md/v2/Clients/legacy/{exchange}/{portfolio}/money` (deprecated snapshot). None lists deposits, withdrawals or money movements. A web search found no such endpoint. I could not read every page of alor.dev (the site returned 403 to non-browser requests), so absence is based on the spec and the search, and ALOR support (support@alor.ru) could confirm.

### Value history: not available
- `summary` and `money` are snapshots. No portfolio value over time. Trades history alone cannot give value.

### Verdict: neither.

---

## 13. Capital.com

Plugin today: `https://api-capital.backend-capital.com/api/v1/session` (API key header `X-CAP-API-KEY`, returns `CST` and `X-SECURITY-TOKEN`), then `GET /api/v1/accounts`. Docs: https://open-api.capital.com (OpenAPI embedded in the page; parsed).

### Cash flows
- Endpoint: `GET /api/v1/history/transactions` ("Returns the transaction history. By default returns the transactions within the last 10 minutes").
- Auth: headers `CST` and `X-SECURITY-TOKEN` (from the session). The token "identif[ies] the client's current account", so for accounts other than the active one the session must first switch with `PUT /api/v1/session` body `{ "accountId": "..." }` ("Switches active account"). The plugin lists several accounts, so this applies.
- Parameters (all optional): `from`, `to` (`YYYY-MM-DDTHH:MM:SS`, filtered by `dateUtc`), `lastPeriod` (seconds back from now, default 600; "not applicable if a date range has been specified"), `type` (one value).
- `type` values: `INACTIVITY_FEE, RESERVE, VOID, UNRESERVE, WRITE_OFF_OR_CREDIT, CREDIT_FACILITY, FX_COMMISSION, COMPLAINT_SETTLEMENT, DEPOSIT, WITHDRAWAL, REFUND, WITHDRAWAL_MONEY_BACK, TRADE, SWAP, TRADE_COMMISSION, TRADE_COMMISSION_GSL, NEGATIVE_BALANCE_PROTECTION, TRADE_CORRECTION, CHARGEBACK, ADJUSTMENT, BONUS, TRANSFER, CORPORATE_ACTION, CONVERSION, REBATE, TRADE_SLIPPAGE_PROTECTION`.
- Response: `{ "transactions": [ { "date", "dateUtc", "instrumentName", "transactionType", "note", "reference", "size", "currency", "status" } ] }`.
- Identify flows: `transactionType` = `DEPOSIT` or `WITHDRAWAL` with `status` = `PROCESSED`. Reversals: `WITHDRAWAL_MONEY_BACK` (withdrawal returned), `CHARGEBACK`, `REFUND`. `TRANSFER` is between own accounts. `BONUS`, `CREDIT_FACILITY`, `ADJUSTMENT`, `WRITE_OFF_OR_CREDIT` are not external money (decide per product). Amount: `size` (string); sign for withdrawals is not documented (only a DEPOSIT example, `"size": "20.0"`). Currency: `currency` per row.
- Depth and paging: not documented. No paging parameter. Because the default window is 10 minutes, always send `from` and `to`; no maximum range is stated for this endpoint (it is stated as 1 day for `/history/activity`), so test a long range and fall back to windows.

### Value history: not available.
- No endpoint returns balance or equity over time. `GET /api/v1/history/activity` is order/position activity (`epic`, `dealId`, `source`, `type`, `status`), range at most 1 day, no values. `GET /api/v1/accounts` is a snapshot (`balance.balance`, `deposit`, `profitLoss`, `available`).

### Verdict: flows only.

### Sample (docs, "Filter list within last 30 second by type")
```
GET /api/v1/history/transactions?from=2025-01-01T00:00:00&to=2025-03-01T00:00:00&type=DEPOSIT
{ "transactions": [ { "date": "2022-04-04T16:54:27.568", "dateUtc": "2022-04-04T16:54:27.568",
  "transactionType": "DEPOSIT", "note": "Deposit", "reference": "12345678", "size": "20.0",
  "currency": "USD", "status": "PROCESSED" } ] }
```

---

## 14. Akahu

Plugin today: `GET https://api.akahu.io/v1/accounts`, headers `Authorization: Bearer USER_TOKEN`, `X-Akahu-Id: APP_TOKEN`.

### Cash flows: not available for investment accounts
- Endpoints exist: `GET /v1/transactions` (all accounts) and `GET /v1/accounts/{id}/transactions` (query `start` exclusive, `end` inclusive, ISO 8601, `cursor`; response `{ "success", "items": [...], "cursor": { "next" } }`). Transaction fields: `_id`, `_account`, `date`, `description`, `amount`, `balance` (if available), `type` (`CREDIT`, `DEBIT`, `PAYMENT`, `TRANSFER`, `STANDING ORDER`, `EFTPOS`, `INTEREST`, `FEE`, `TAX`, `CREDIT CARD`, `DIRECT CREDIT`, `DIRECT DEBIT`, `ATM`, `LOAN`).
- The supported-integrations page (https://developers.akahu.nz/docs/integrations.md, updated 2026-10-20) lists "Funds and investment platforms" (Booster, Fisher Funds, Generate, Hatch, Kernel Wealth, Milford, Pie Funds, Sharesies, Sharesight, Simplicity, Stake, Superlife) with the columns "Account data" and "Parties" only. The "Transaction data" column exists for banks and "Other financial accounts" but not for investment platforms. So transactions are not returned for the accounts this plugin reads.
- If an account did return transactions: depth for a personal app is 730 days ("If you are using a personal app, it will be given access to 730 days (2 years) of transactions") counted from the date of consent (`access_granted_at` on `/me`), shortened by any limit of the institution.

### Value history: not available
- `/accounts` returns the current `balance` (`current`, `currency`) and `refreshed.balance` only; no balance history endpoint.

### Verdict: neither (for investment platforms). For bank-type accounts transactions exist but they do not describe investment flows.

---

## Cross-broker notes for the implementer

- Strongest value history: tastytrade (all history, OHLC), Alpaca (daily equity), Tradier (period ALL), IBKR (daily NAV, 365 days per request), eToro (12 months), SnapTrade (estimate, beta, 1 year).
- No value history at all: OANDA, Trading 212, Public.com, Indexa (amounts), T-Invest, ALOR, Capital.com, Akahu. For these the app has to store its own daily readings from first sync (the plugin `read()` already yields one value per refresh).
- Flows sign conventions that are documented: OANDA (amount sign), SnapTrade (amount sign), Alpaca (CSD +, CSW -), tastytrade (`value-effect`), Public.com (`direction`), IBKR (fixtures only). Undocumented: Trading 212, Tradier ach/wire, T-Invest, Capital.com, Indexa.
- Internal moves to decide per account: OANDA `ACCOUNT_TRANSFER`, Alpaca `JNLC`, Tradier `journal`, tastytrade `Transfer`, Capital.com `TRANSFER`, T-Invest `TRANS_BS_BS`, IBKR `Transfers` section.
- Items I could not verify and that need a real-account test: IBKR exact `type` string and whether `fd`/`td` accept more than 365 days; tastytrade `net-liq/history` real response shape and `interval` values; Alpaca `cashflow` semantics in portfolio history; Tradier `historical-balances` `value` meaning; Indexa deposit codes; Capital.com maximum range of `/history/transactions`; SnapTrade Personal keys with `userId`/`userSecret`.
