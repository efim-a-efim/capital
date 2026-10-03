## Context

Holdings are manual (quantity typed) or wallets (`address` on a chain). Refresh iterates holdings with an address. Keys live in `Secrets`, keyed by provider name, and the UI offers key entry per provider. Endpoints below were checked against the brokers' documentation on 2026-10-03; neither can be exercised without a funded account, so the adapters are tested with captured response shapes.

## Decisions

### 1. Data model
`Holding.broker: String?` (null = not an account). For an account, `address` holds the id (Flex Query id `[0-9]{1,20}` for Interactive Brokers, `NNN-NNN-NNNNNNN-NNN` for OANDA), `asset` the base currency, `quantity` the last total value or null. `source` is the broker name. Uniqueness key `"<broker>:<id>"` shares the wallet owner set.
Alternative rejected: separate `Account` entity. Everything that works on holdings (buckets, allocation, stale, editor, rows) would need a second path.

### 2. Interactive Brokers: Flex Web Service
- `GET https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService/SendRequest?t={token}&q={queryId}&v=3` → `<FlexStatementResponse><Status>Success</Status><ReferenceCode>…</ReferenceCode>…`. Pacing 1 request/s, 10/min.
- `GET …/GetStatement?t={token}&q={referenceCode}&v=3` → `<FlexQueryResponse>` or `<FlexStatementResponse>` with `ErrorCode`. 1009/1019 (generating) and 1018 (throttled) are retried up to 6 times with 5 s / 10 s waits; other codes fail: 1012 expired, 1015 invalid token, 1013 IP restriction, 1014 invalid query, 1011 inactive, 1003/1021 not available.
- Report: `FlexStatement` (exactly one) → `EquitySummaryInBase/EquitySummaryByReportDateInBase` rows; the row with the greatest `reportDate` gives `total`; currency from the row or from `AccountInformation`. Required query sections: Account Information (Account ID, Currency), Net Asset Value (NAV) Summary in Base (Report Date, Total); format XML; period Last Business Day. Totals can be negative on margin; a negative total is reported as an error, not stored.
- User-Agent `Java` (documented requirement in older versions, used by ibflex). XML parsing reuses the entity-free DOM parsing from the ECB adapter.
- Token in the URL is the service's design; messages never include it.

### 3. OANDA v20
`GET https://api-fxtrade.oanda.com/v3/accounts/{id}/summary` with `Authorization: Bearer {token}` → `account.NAV`, `account.currency`, `account.id`. Live only; practice accounts are not savings. 401/403 → existing "Access denied" path. A personal access token grants full account access; the documentation says so and tells the user to keep it like a password.

### 3b. Trading 212, SnapTrade; Saxo left out
- Trading 212: `GET https://live.trading212.com/api/v0/equity/account/summary`, HTTP Basic `apiKey:apiSecret`, 1 request per 5 s; `id` must equal the holding's account number, `totalValue` in `currency`. Invest and Stocks ISA accounts only; one key pair per account, so one Trading 212 account per device.
- SnapTrade (Personal API key): requests carry `clientId` and `timestamp` query parameters and a `Signature` header, HMAC-SHA256 over the canonical JSON `{"content":null,"path":"/api/v1/…","query":"…"}` with the consumer key. `GET /accounts/{id}` → `balance.total.{amount,currency}` (daily data on the free plan); `GET /accounts` fills the editor's picker; `POST /snapTrade/login` returns the Connection Portal URL (5 minutes) that the app opens in the browser.
- Saxo was evaluated and dropped: live access needs an OAuth application approved by Saxo, access tokens last 20 minutes and refresh tokens about 40, and the developer portal's 24-hour token is simulation-only. The user requires long-lived credentials, so no Saxo adapter ships.
- Credentials: `brokerCredentials` maps a broker to its Secrets names (Trading 212 key + secret, SnapTrade client id + consumer key); Settings shows one button per name.

### 4. Settings
A "Broker accounts" heading with a token button per broker (`KeyDialog`, reused), a note on storage and read-only use, and a "Setup guide" button to `/<lang>/accounts`. Brokers are not in `providerChoices` (no choice of operator to make), so `Settings.providers` and its validation are unchanged.

### 5. Schema 5
New field with a default. `encodeDefaults = true` writes `"broker": null`, which schema-4 builds reject as an unknown key, so the schema number is raised to make them say "needs a newer version" instead of "invalid snapshot". Decoding needs no conversion.

### 6. Documentation
`docs/accounts.md` (+14 translations): what is read, what is sent, token storage; step lists per broker with links to the broker's pages; brokers without a public HTTPS reporting interface (MetaTrader investor password, most retail forex brokers) stay manual; a note that broker menus and labels change and that the broker's own documentation is authoritative.
