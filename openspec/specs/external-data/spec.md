# external-data Specification

## Purpose
Provider selection, refresh lifecycle, failure handling, privacy and freshness of balances and rates.
## Requirements
### Requirement: Free selectable providers
The app SHALL offer verified free service choices independently for BTC, ETH, TON, TRX, crypto quotes, and fiat quotes, showing coverage, source attribution, key requirements and errors. It SHALL never embed shared API secrets or switch operators without user selection.

#### Scenario: Key required
- **WHEN** the selected provider requires a free key but none exists
- **THEN** the UI offers key setup or another provider and manual tracking remains usable.

### Requirement: Explicit refresh lifecycle
Only cold startup or user Refresh all/Refresh bucket SHALL initiate balance/rate refresh. Resume SHALL reload local changes without network polling. Concurrent requests SHALL be deduplicated; backgrounding SHALL cancel unfinished network work.

#### Scenario: Repeated refresh taps
- **WHEN** the same bucket is already refreshing
- **THEN** another tap does not duplicate address queries.

#### Scenario: Background idle
- **WHEN** the app is backgrounded
- **THEN** it schedules no polling jobs, timers, or automatic retry work.

### Requirement: Partial failure handling
Refresh SHALL retain prior successful observations on timeout/auth/quota/parse failure, record per-item errors, and publish successful results. Retries SHALL be bounded, honor provider delays and stop on invalid credentials. Unknown first-fetch balances SHALL not become zero.

#### Scenario: One provider unavailable
- **WHEN** ETH refresh fails while BTC succeeds
- **THEN** new BTC and cached stale ETH remain visible with distinct statuses.

### Requirement: Native balance scope and quote identity
Automatic holdings SHALL track native mainnet address balances, with confirmed BTC excluding mempool, ETH latest-block, TON native and TRON confirmed spendable balances, plus fungible tokens as defined in the wallet-tokens spec. Quotes SHALL use stable asset IDs rather than ambiguous tickers, exact decimal parsing, positive rates and source timestamps.

#### Scenario: TON naming alias
- **WHEN** a provider names TON native currency TON or GRAM
- **THEN** both map to the same verified native asset and unrelated tokens do not.

### Requirement: Privacy and freshness
The UI SHALL disclose selected operators receive public addresses/IPs. Keys SHALL be encrypted on device, excluded from portable snapshots/logs, and HTTPS SHALL be required. Cached values older than 24h for wallet/crypto or 7 days for fiat, or affected by failed refresh, SHALL be visibly stale; these thresholds SHALL not trigger network work.

#### Scenario: Old cached rates offline
- **WHEN** the app opens without connectivity and has old quotes
- **THEN** it shows cached estimates with their dates and a stale label.

