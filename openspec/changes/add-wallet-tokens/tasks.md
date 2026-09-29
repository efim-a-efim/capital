## 1. Domain

- [x] 1.1 Add `Token`, `Holding.tokens`, `Holding.tokensError`, token asset ids and validation limits; include known tokens in `bucketValue`/`incomplete`/`stale`, skip unknown ones.
- [x] 1.2 Add provider address encoding (TRX Base58Check, TON bounceable) with round-trip tests against real USDT contracts.
- [x] 1.3 Snapshot schema 2: accept schema 1, default missing token sources, reject schema 3; test.

## 2. Providers

- [x] 2.1 Token balance adapters: Blockscout, Ethplorer, TON Center v3, TonAPI, TronGrid; sanitize text, cap 100, drop invalid contracts and zero balances; test with captured responses.
- [x] 2.2 Contract quote lookups for CoinGecko and CoinPaprika, including 301 ticker resolution and not-listed handling; test listed, unlisted and failure.
- [x] 2.3 Refresh integration: token sources incl. "Off", lookup budget and rotation, quote removal for unlisted contracts, TRX decimals/symbol for known tokens, safe merge for edited or deleted holdings; test.

## 3. Interface

- [x] 3.1 Holding rows list tokens: known with quantity and converted value, unknown with "Unknown token · not counted" and shortened contract; token error notice.
- [x] 3.2 Settings shows token sources and updated address disclosure.

## 4. Acceptance

- [x] 4.1 Unit tests, lint and debug build pass.
- [x] 4.2 Emulator run with a real token-holding address: known token counted, unknown tokens listed and not counted, schema 1 folder opens.
- [x] 4.3 README and provider smoke script updated.

## 5. Exclusion and wallet summary

- [x] 5.1 Model and refresh exclusion (`Holding.excluded`, validation, known/stale, no lookups or quotes for excluded contracts) with tests.
- [x] 5.2 Editor "Fetch tokens" and per-token switches; stored tokens kept when a wallet is renamed.
- [x] 5.3 Wallet row shows native plus known token total; native balance is the first list entry.
- [x] 5.4 Unavailable values instead of zero for unconvertible buckets; bucket picker keyed by id.
- [x] 5.5 Emulator verification of the editor, exclusion and wallet summary.
