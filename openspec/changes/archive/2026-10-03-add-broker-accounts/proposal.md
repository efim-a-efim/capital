## Why

Brokerage and forex balances can only be typed in by hand, so the largest part of many portfolios is always out of date. Both Interactive Brokers and OANDA publish an HTTPS interface that reads an account's value with a token the user creates, without any trading right in the app.

## What Changes

- New holding type **Account**, connected to a bucket exactly like a wallet: a broker (Interactive Brokers, OANDA, Trading 212 or SnapTrade) plus an account or query id. On refresh the app reads the account's total value in the account's base currency and stores it as the holding's quantity in that currency.
- Interactive Brokers through the Flex Web Service (token + Activity Flex Query id, two-step report retrieval, XML). OANDA through the v20 REST API (personal access token, account summary, JSON). Trading 212 through its Public API (API key + secret, account summary). SnapTrade Personal (client id + consumer key, signed requests, Connection Portal link and account picker in the editor) as an aggregator for brokers without their own public API. Only brokers that issue long-lived credentials are included; Saxo was evaluated and left out because its OpenAPI issues 20-minute OAuth tokens only. Credentials are stored like provider keys: encrypted on the device, never in snapshots or exports.
- Settings gains a **Broker accounts** section for the brokers' credentials and a link to the setup guide.
- Snapshot schema 5: `Holding.broker`. Schema 4 folders open unchanged; older builds refuse schema 5 as newer.
- Site: a new page `accounts` with the setup steps for each broker, links to the brokers' own documentation and a note that the brokers' menus may change; manual, screen docs, Privacy Policy, Data safety and Financial features updated. All in 15 languages.
- Out of scope: per-position breakdown, brokers without a public HTTPS reporting interface (MetaTrader investor passwords only work inside the terminal), practice/demo environments, brokers with short-lived OAuth tokens only (Saxo), write access of any kind.

## Capabilities

### New Capabilities
- `broker-accounts`: account holdings, read-only broker retrieval, token handling, display and documentation.

### Modified Capabilities
- `external-data`: refresh covers account holdings; provider list grows by two brokers; privacy disclosure names tokens and account ids.

## Impact

`domain/Model.kt` (Holding.broker, validation, account ids), `data/Providers.kt` (Flex Web Service, OANDA), `data/Snapshots.kt` (schema 5), `ui/CapitalApp.kt` (editor, rows, settings), tests, `scripts/check-providers.py`, README, `docs/` in 15 languages, `store/listing.md`. No new dependencies.
