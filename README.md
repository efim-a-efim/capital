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
4. Add goals and connect their funding buckets. Higher priority numbers fund first; peers share equally subject to limits.
5. Refresh all or a bucket. Cold startup refreshes once; returning from background only reloads local files.

Amounts accept a decimal point or comma, without grouping separators. Each bucket displays native quantities plus converted values. Missing quotes mark totals incomplete; stale cached values remain usable with a warning. Goal allocations never transfer money or increase your total savings.

## Wallet and provider scope

Native mainnet address balances only: confirmed BTC, latest ETH, TON/GRAM, confirmed spendable TRX. No tokens, staking, DeFi, xpub discovery, signing or transactions. Record other investments manually as a quantity or labeled fiat value. A Bitcoin address does not represent an entire HD wallet.

| Data | Default | Alternative | Key |
|---|---|---|---|
| BTC | Blockstream | mempool.space | None |
| ETH | PublicNode | Alchemy | Alchemy free key |
| TON / GRAM | TON Center | TonAPI | Optional TON Center key |
| TRX | TronGrid | PublicNode | TronGrid free key |
| Crypto prices | CoinGecko Demo | CoinPaprika | CoinGecko free key |
| Fiat | Frankfurter | ECB | None; currency coverage differs |

Sources are independently selectable. No automatic switch to another operator. Settings links each provider's site and includes attribution. “Test sources / refresh” tests only assets/addresses currently present in the portfolio. Public queries disclose those addresses and your IP to the selected operator. API keys are encrypted with Android Keystore and excluded from snapshots, exports and OS backup. Never enter a private key or seed phrase.

## Storage, sync and recovery

Financial records live only in the selected folder. App-private storage holds the folder grant and encrypted provider keys. Snapshots use decimal strings, UUID revisions, parent IDs and SHA-256 checksums. Saves create a new file, close it, reopen it and verify it; existing snapshots are never truncated.

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

Implementation plan and remaining acceptance checks: [OpenSpec tasks](openspec/changes/build-android-savings-tracker/tasks.md). Emulator acceptance record: [acceptance.md](openspec/changes/build-android-savings-tracker/acceptance.md). Research: [sources](openspec/changes/build-android-savings-tracker/research.md). No release signing key is committed; the supplied APK is a debug build.
