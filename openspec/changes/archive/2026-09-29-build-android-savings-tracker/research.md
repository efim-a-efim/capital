# Research — 2026-09-28

Documentation review; no live balance calls, credentials, or SLA verification. Recheck provider availability and terms before release. Free services can throttle or disappear.

| Area | Evidence | Decision |
|---|---|---|
| Android storage | [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files) grants a chosen directory; Android restricts root/Download/Android data folders. | Select a dedicated Documents/CapitalTracker folder. Persist URI grant; no broad storage permission. Sync-tool access must be tested on device. |
| Bitcoin | [Esplora](https://github.com/Blockstream/esplora/blob/master/API.md) exposes funded/spent chain and mempool statistics. [mempool API](https://mempool.space/docs/api/rest) offers another endpoint. | Blockstream default; mempool.space alternative. Confirmed funded minus spent; pending separate, excluded from goals. |
| Ethereum | [Ethereum JSON-RPC](https://ethereum.org/developers/docs/apis/json-rpc/) defines eth_getBalance. [PublicNode](https://www.publicnode.com/?network=Mainnet) lists Ethereum mainnet RPC. | PublicNode default; [Alchemy Free](https://www.alchemy.com/pricing) alternative with user key. Verify chain ID 1; query latest balance, explain reorg exposure. |
| TON | [TON Center v2](https://docs-next.ton.org/api/v2/overview) provides native balance. [TonAPI](https://docs.tonapi.io/tonapi/dapp/free-limits) documents free access. [Official branding](https://ton.org/media/) now calls native currency Gram, formerly Toncoin/TON. | TON Center default; TonAPI alternative. Stable asset identity ton-native, display “TON / GRAM”; do not select unrelated tokens by ticker. |
| TRON | [TRON API](https://developers.tron.network/docs/api), [TronGrid limits](https://developers.tron.network/reference/rate-limits), [PublicNode](https://www.publicnode.com/?network=Mainnet). | TronGrid with user's free key; PublicNode alternative. Confirmed native balance via walletsolidity/getaccount; exclude staked/resource value. |
| Fiat | [Frankfurter v2](https://frankfurter.dev/) exposes currencies, provenance, provider selection, daily rates without key. | Default broad daily feed; selectable central-bank source. Independent [ECB feed](https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html) alternative for its supported subset. No promise of every fiat currency. |
| Crypto prices | [CoinGecko simple price](https://docs.coingecko.com/reference/simple-price), [pricing](https://www.coingecko.com/en/api/pricing), [CoinPaprika free plan](https://coinpaprika.com/api/pricing/). | CoinGecko Demo with free user key; CoinPaprika alternative after asset-coverage check. Store provider IDs, batch supported requests, include attribution. |

## Findings that change the plan

- “Free” does not guarantee keyless access. Setup offers alternatives and explains free-key registration; never embeds a shared secret.
- Provider quota documentation disagrees over time; use bounded requests and runtime throttling, not promised requests/minute.
- Native address balance is not total wallet wealth: tokens, staking, Bitcoin change addresses, and DeFi require additional scope. Label this explicitly; manual holdings cover them.
- Storage-provider writes are not universally atomic. Immutable, validated snapshots avoid truncating the last good data.
- External folder sync is asynchronous. Preserve branches and resolve conflicts explicitly; never silently choose latest timestamp.
- “All data local” means application records stay local. Public balance queries necessarily disclose addresses to selected services; sync tools may upload files at the user's direction.

## Provider smoke test — 2026-09-29

`python3 scripts/check-providers.py` against public test addresses (BTC genesis address, ETH/TON/TRX zero addresses), no keys. Raw responses: `.tools/provider-smoke.json` (untracked).

| Provider | Endpoint | Status | Contract used | Asset ID / unit | Key | Attribution |
|---|---|---|---|---|---|---|
| Blockstream | `GET blockstream.info/api/address/{addr}` | 200 | `chain_stats.funded_txo_sum - spent_txo_sum`; `mempool_stats` ignored | sat, 8 dp | none | Blockstream Esplora |
| mempool.space | `GET mempool.space/api/address/{addr}` | 200 | same Esplora contract | sat, 8 dp | none | mempool.space |
| PublicNode ETH | `POST ethereum-rpc.publicnode.com` JSON-RPC | 200 | `eth_chainId` must be `0x1`; `eth_getBalance(latest)` hex `result` | wei, 18 dp | none | PublicNode |
| Alchemy | `POST eth-mainnet.g.alchemy.com/v2/{key}` | not live-tested | same JSON-RPC contract | wei | user free key | Alchemy |
| TON Center | `GET toncenter.com/api/v2/getAddressBalance?address=` | 200 | `ok:true`, `result` decimal string | nanoton, 9 dp | optional key header `X-API-Key` | TON Center |
| TonAPI | `GET tonapi.io/v2/accounts/{addr}` | 200 | `balance` integer; `status:"uninit"` still returns balance | nanoton, 9 dp | none | TonAPI |
| PublicNode TRX | `POST tron.publicnode.com/walletsolidity/getaccount` | 200 | `balance` (sun); `{}` = unactivated account, valid zero | sun, 6 dp | none | PublicNode |
| TronGrid | `POST api.trongrid.io/walletsolidity/getaccount` | not live-tested | same contract, header `TRON-PRO-API-KEY` | sun | user free key | TronGrid |
| Frankfurter | `GET api.frankfurter.dev/v2/rates?base=USD&quotes=` | 200 | array of `{date,base,quote,rate}`; EUR and RSD returned | USD→fiat, daily | none | Frankfurter |
| ECB | `GET ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml` | 200 | `Cube[@currency,@rate]` vs EUR, `Cube[@time]`; USD cross-rate | EUR base, daily | none | ECB reference rates |
| CoinGecko Demo | `GET api.coingecko.com/api/v3/simple/price` | not live-tested | ids `bitcoin,ethereum,the-open-network,tron`; `usd`, `last_updated_at` | USD | user free key `x-cg-demo-api-key` | CoinGecko |
| CoinPaprika | `GET api.coinpaprika.com/v1/tickers/{id}` | 200 | ids `btc-bitcoin,eth-ethereum,toncoin-the-open-network,trx-tron`; `quotes.USD.price`, `last_updated` | USD | none | CoinPaprika |

Notes: CoinPaprika now names TON native "Gram (prev. Toncoin)", symbol `GRAM`, under the unchanged id `toncoin-the-open-network`; the stable id mapping holds. Quota figures are not recorded: runtime pacing (1.1 s between requests, `Retry-After` honored, 3 attempts) is the enforced limit. Keyed providers are verified by "Test sources / refresh" in Settings with the user's own key.
