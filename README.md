# Capital

Native Android savings tracker. Kotlin, Compose Material 3, Android 8+.

## Build / install

Open this folder in Android Studio, or run:

```sh
./scripts/gradle assembleDebug testDebugUnitTest lintDebug
./scripts/gradle installDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Toolchain: Gradle 9.8.0, Android Gradle Plugin 9.4.1, Kotlin/Compose compiler 2.4.20, SDK/Build-Tools 37. JDK 25 from current Android Studio works. `scripts/gradle` selects that JDK on macOS; elsewhere set `JAVA_HOME`. Set `sdk.dir` in untracked `local.properties` or `ANDROID_HOME`. First build downloads dependencies. Gradle distribution has a pinned SHA-256 checksum.

## First use

1. Choose a dedicated local folder, e.g. `Documents/CapitalTracker`. Existing Capital folders reopen directly.
2. Settings → set default currency and optional provider keys.
3. Add a bucket, then manual holdings or public wallet addresses.
4. Add goals and connect their funding buckets. Goals with earlier dates fund first; drag goals that share a date to set their order.
5. Add planned savings on the Buckets screen to see when each goal closes.
6. Refresh all or a bucket. Cold startup refreshes once; returning from background only reloads local files.

Amounts accept a decimal point or comma, without grouping separators. Each bucket displays native quantities plus converted values. Missing quotes mark totals incomplete; stale cached values remain usable with a warning. Goal allocations never transfer money or increase your total savings.

## Wallet and provider scope

Native mainnet address balances: confirmed BTC, latest ETH, TON/GRAM, confirmed spendable TRX. Plus fungible tokens on tracked ETH (ERC-20), TON (jettons) and TRX (TRC-20) addresses. No staking, DeFi positions, NFTs, Bitcoin-based tokens, xpub discovery, signing or transactions. Record other investments manually as a quantity or labeled fiat value.

**Tokens and scam protection.** A token is identified by its chain and smart-contract address, never by name or symbol. It counts only when the selected crypto price provider lists that exact contract. Everything else shows as "Unknown token · not counted" with its contract address and is left out of totals, allocations and goals. A fake "USDT" with a different contract is therefore never counted. Open a wallet holding's editor to fetch its tokens and switch off any you do not want listed or counted. DefiLlama prices all tokens of a refresh in batched requests and only prices with a confidence of at least 0.9 count. With CoinGecko lookups are limited to 30 contracts per refresh and with CoinPaprika to 10, because its free tier allows 60 requests per hour, so a wallet with many airdropped tokens is classified over several refreshes. At most 100 token types per wallet are tracked, in the order the token source reports them. A Bitcoin address does not represent an entire HD wallet.

| Data | Default | Alternative | Key |
|---|---|---|---|
| BTC | Blockstream | mempool.space | None |
| ETH | PublicNode | Alchemy | Alchemy free key |
| TON / GRAM | TON Center | TonAPI | Optional TON Center key |
| TRX | TronGrid | PublicNode | TronGrid free key |
| ETH tokens | Blockscout | Ethplorer, Off | None |
| TON tokens | TON Center | TonAPI, Off | Optional TON Center key |
| TRX tokens | TronGrid | Off | Optional TronGrid key |
| Crypto prices | DefiLlama | CoinGecko Demo, CoinPaprika | None; CoinGecko needs a free key |
| Fiat | Frankfurter | ECB | None; currency coverage differs |

Sources are independently selectable. No automatic switch to another operator. Settings links each provider's site and includes attribution. “Test sources / refresh” tests only assets/addresses currently present in the portfolio. Public queries disclose those addresses and your IP to the selected operator. API keys are encrypted with Android Keystore and excluded from snapshots, exports and OS backup. Never enter a private key or seed phrase.

## Storage, sync and recovery

Financial records live only in the selected folder. App-private storage holds the folder grant and encrypted provider keys. Snapshots use decimal strings, UUID revisions, parent IDs and SHA-256 checksums. Schema 2 adds wallet tokens; schema 1 folders open unchanged and upgrade on the next save. Saves create a new file, close it, reopen it and verify it; existing snapshots are never truncated.

Use your preferred sync tool to sync the folder. Capital does not run its own sync service. Android folder access differs across sync tools; verify both apps can access your selected directory. Concurrent revisions produce a conflict screen; choose a version after reviewing it. Both originals remain. Incomplete sync blocks editing until missing parents arrive. Device clocks do not choose a winner.

- **Offline/provider failure:** previous observations remain; errors identify missing/stale data.
- **Interrupted/corrupt snapshot:** invalid revision ignored; last valid data remains.
- **Failed save:** edits stay in memory, with Retry save / Save copy. Export before discarding or closing.
- **Lost folder grant:** reconnect the same directory. Unsaved data can be saved to a new empty folder.
- **Backup:** Settings → Export backup. Restore validates before confirmation and creates a new revision.
- **Newer schema:** upgrade the app; older builds refuse to modify it.

Files are plaintext financial records. Protect your device and sync destination. Snapshot cleanup and portable encryption are not implemented. Keep the complete snapshot ancestry when syncing; use Export backup for a standalone portable copy.

## Checks

```sh
./scripts/gradle testDebugUnitTest lintDebug
./scripts/gradle connectedDebugAndroidTest  # running emulator/device
python3 scripts/check-providers.py         # public test addresses; no user keys
```

The device test creates a uniquely named `CapitalTest-*` folder through Android's real folder picker. It does not delete existing folders. Unit checks cover exact money round-trip, allocation caps/priorities/rerouting/conservation, 100 generated graphs, canonical addresses, snapshot conflicts/corruption, provider partial failure, bounded retries, cancellation and safe observation merging.

Roadmap: [ROADMAP.md](ROADMAP.md). Implementation plan and remaining acceptance checks: [OpenSpec tasks](openspec/changes/archive/2026-09-29-build-android-savings-tracker/tasks.md). Emulator acceptance record: [acceptance.md](openspec/changes/archive/2026-09-29-build-android-savings-tracker/acceptance.md). Research: [sources](openspec/changes/archive/2026-09-29-build-android-savings-tracker/research.md). No release signing key is committed; the supplied APK is a debug build.
