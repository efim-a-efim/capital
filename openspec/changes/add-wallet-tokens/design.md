## Context

Wallet holdings store one native quantity per address. Quotes are keyed by asset id (fiat code or chain name). Providers are selected per category in `Settings.providers`; validation requires the key set to equal `providerChoices`. All endpoints below were called live on 2026-09-29.

## Goals / Non-Goals

**Goals:** token balances on tracked wallets; identity by contract; unknown tokens visible but never counted; bounded refresh time.

**Non-Goals:** NFTs, staking, DeFi positions, LP shares, BTC-based tokens (Runes, BRC-20), token transfers, per-token manual overrides.

## Decisions

### 1. Data model
`Token(contract, symbol, name, units, decimals: Int?, checkedAt: Long?)` inside `Holding.tokens`, plus `Holding.tokensError`. `contract` is the canonical form from `canonicalAddress(chain, …)`. `units` is the raw integer balance; quantity is `units / 10^decimals`, truncated to 18 decimals. Symbol and name are trimmed, stripped of control characters, capped at 40 characters.
Quote asset id for a token: `"<CHAIN>:<canonical contract>"`, e.g. `ETH:0xdac17f…`. `validAsset` accepts it only when the contract re-canonicalizes to itself.
Alternative rejected: tokens as separate holdings. It breaks unique wallet ownership and lets users move a token away from its wallet.

### 2. Token sources
| Setting | Options | Endpoint | Notes |
|---|---|---|---|
| ETH tokens | Blockscout, Ethplorer, Off | `eth.blockscout.com/api/v2/addresses/{a}/token-balances`; `api.ethplorer.io/getAddressInfo/{a}?apiKey=freekey` | Blockscout: keep `type == "ERC-20"`. Ethplorer `freekey` is its documented public key. |
| TON tokens | TON Center, TonAPI, Off | `toncenter.com/api/v3/jetton/wallets?owner_address=&exclude_zero_balance=true&limit=100` (decimals in `metadata[jetton].token_info[0].extra.decimals`); `tonapi.io/v2/accounts/{a}/jettons` | Default matches the default TON balance operator. |
| TRX tokens | TronGrid, Off | `api.trongrid.io/v1/accounts/{a}` (key optional) → `data[0].trc20` list of `{contract: units}` | No symbol or decimals in the list. Read `decimals()` and `symbol()` through `walletsolidity/triggerconstantcontract`, only for tokens that turn out known, once, then cached in the token. One option only: TronScan now needs a key and PublicNode cannot enumerate tokens. |

Provider flags such as `is_scam`, `reputation`, `exchange_rate` are ignored: classification comes only from the price provider by contract.

### 3. Contract quotes
Use the selected `Crypto` provider. DefiLlama is the default since 2026-09-29: keyless, native coins and contracts in one API, batched.
- DefiLlama: `coins.llama.fi/prices/current/{keys}` with keys `coingecko:<id>` for native coins and `ethereum:<0x…>`, `tron:<T…>`, `ton:<EQ…>` for contracts, 50 keys per request. A key missing from the response means not listed. Entries with `confidence` below 0.9 or without a positive price count as not listed.
- CoinGecko: `simple/token_price/{ethereum|tron|the-open-network}?contract_addresses={one}&vs_currencies=usd&include_last_updated_at=true`. One contract per call on the free tier. `{}` means not listed.
- CoinPaprika: `v1/contracts/{eth-ethereum|trx-tron|toncoin-the-open-network}/{contract}` answers 301 with `Location: …/v1/tickers/{id}`; then `v1/tickers/{id}` over HTTPS. 404 means not listed. Redirects are not followed automatically; the id is parsed and validated.
Both need the provider address form: ETH lowercase hex, TRX Base58Check `T…`, TON bounceable url-safe `EQ…`. Raw TON and hex TRON forms return "not listed" (verified), so `Addresses.kt` gains an encoder.

### 4. Lookup budget
Requests are paced at 1.1 s. Budget per refresh: all held contracts with DefiLlama (batched), 30 contract lookups with CoinGecko, 10 with CoinPaprika. CoinPaprika's free tier allows 60 requests per hour and blocks for one hour with HTTP 402 (observed 2026-09-29); a listed contract costs two requests. Two thirds of the budget goes to contracts that already have a quote, the rest to unchecked ones, both ordered by `checkedAt` ascending and then by position in the wallet so wallets share the budget. A contract shared by several wallets is looked up once. The first provider failure stops further lookups for that refresh and keeps cached quotes. HTTP 402 is reported as quota reached.

### 5. Valuation
`bucketValue` and `incomplete` iterate native quantity plus tokens that have a quote and decimals. Tokens without a quote are skipped entirely. Token quotes use the 24 h crypto staleness rule.

### 6. Schema 2
`Revision.schema` default becomes 2. `decodeRevision` accepts 1 and 2, throws `FutureSchema` above 2, and fills missing `Settings.providers` keys from defaults before validation. New fields have defaults, so schema 1 payloads decode unchanged.

## Risks / Trade-offs

- A price provider lists a worthless or malicious token → it is counted at the listed price. Mitigation: listing by contract on CoinGecko or CoinPaprika is the stated definition of known; the UI shows the contract.
- Per-contract lookups are slow → budget and rotation; first classification of a spam-heavy wallet takes several refreshes.
- Blockscout and Ethplorer learn tracked ETH addresses → selectable, "Off" available, disclosed in Settings.
- TRX unknown tokens show contract and raw units only → acceptable; they are not counted.

## Migration Plan

Schema 1 folders open as before. First save writes schema 2. Older app builds refuse schema 2 with the upgrade message. Rollback: export a backup before upgrading.
