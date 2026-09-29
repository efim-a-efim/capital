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

## Releases and CI

`.github/workflows/build.yml` builds the APKs on GitHub.

- **Tag `vX.Y.Z`:** runs tests and lint, builds, uploads the APKs as build artifacts and publishes a GitHub release with a changelog made from the commits since the previous tag. The tag must match `versionName` in `app/build.gradle.kts`.
- **Manual run** (Actions → Build APK → Run workflow): builds and uploads the APKs as build artifacts. A manual run on a tag also publishes the release.
- Branch pushes do not build.

`.github/workflows/release.yml` creates the next version. Only the repository owner can run it (Actions → release → Run workflow).

- **Inputs:** the branch to release from, default `main`, and the version part to increase: `patch` (default), `minor` or `major`.
- The next version is the latest `vX.Y.Z` tag increased by that part, following semantic versioning. With `v1.0.0` as the latest tag, `major` gives `v2.0.0`.
- It writes the version into `app/build.gradle.kts`, increases `versionCode`, commits to the branch, creates the tag and starts the build for it. The build then publishes the release.
- The branch must accept pushes from GitHub Actions. A protected branch that blocks them makes the workflow fail without creating a tag.

The debug APK is signed with a temporary key, so it cannot be installed over another build. For updates that install over each other, add these repository secrets and the workflow also builds a signed release APK:

| Secret | Content |
|---|---|
| `CAPITAL_KEYSTORE_BASE64` | `base64 -w0 your.jks` |
| `CAPITAL_KEYSTORE_PASSWORD` | keystore password |
| `CAPITAL_KEY_ALIAS` | key alias |
| `CAPITAL_KEY_PASSWORD` | key password |

Local builds of earlier versions are kept in `releases/`, which is not tracked.

## First use

1. Choose a dedicated local folder, e.g. `Documents/CapitalTracker`. Existing Capital folders reopen directly.
2. Settings → set default currency and optional provider keys.
3. Add a bucket, then manual holdings or public wallet addresses.
4. Add goals and connect their funding buckets. Goals with earlier dates fund first; drag goals that share a date to set their order.
5. Add planned savings on the Plans tab to see when each goal closes.
6. Refresh all or a bucket. Cold startup refreshes once; returning from background only reloads local files.

Amounts accept a decimal point or comma, without grouping separators. Each bucket displays native quantities plus converted values. Missing quotes mark totals incomplete; stale cached values remain usable with a warning. Goal allocations never transfer money or increase your total savings.

## Portfolio mode

Switch on **Portfolio mode** in a bucket's settings to treat it as an investment portfolio. Set a target percentage per asset; targets must total 100%. The bucket then shows value, real share, target and difference for each asset, calculated in your default currency. Switching the mode off hides these views and keeps the targets.

**Rebalance** asks for an amount to invest and lists what to buy to come as close to the targets as possible. It recommends sells only when **Allow sells during rebalance** is on for that bucket. It is a calculator: it never changes holdings and uses cached rates. Unknown and excluded tokens are not part of a portfolio. If any asset has no rate, shares are shown as unavailable rather than calculated from part of the portfolio.

## Encryption and lock

Settings → Security → **Encryption** encrypts every snapshot in the folder with a password, including older revisions. Switching it off decrypts them all. A password change re-encrypts every file, after which the old password opens nothing.

- **No password recovery.** A lost password means the data cannot be opened.
- **Earlier plaintext backups stay readable.** The app warns about them when you switch encryption on and cannot encrypt or delete them. The same holds for copies your sync tool already made.
- **PIN and biometrics** are available only while encryption is on. With encryption off the app opens directly. Without a PIN the password is asked at each launch.
- **"Use password"** is always available on the PIN screen, also while the PIN is blocked. After 10 wrong PINs the PIN is removed and only the password works. Wrong entries never delete data.
- Every password check waits a random 1 to 5 seconds.
- While encryption is on, screenshots and the recent-apps preview are blocked.
- Files use AES-256-GCM with a key derived by Argon2id. Revision ids stay readable so sync conflicts can be detected without the password.
- Version 1 cannot open encrypted folders.

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

Financial records live only in the selected folder. App-private storage holds the folder grant and encrypted provider keys. Snapshots use decimal strings, UUID revisions, parent IDs and SHA-256 checksums. Older schemas open unchanged and upgrade on the next save; the current schema is 4. Saves create a new file, close it, reopen it and verify it; existing snapshots are never truncated.

Use your preferred sync tool to sync the folder. Capital does not run its own sync service. Android folder access differs across sync tools; verify both apps can access your selected directory. Concurrent revisions produce a conflict screen; choose a version after reviewing it. Both originals remain. Incomplete sync blocks editing until missing parents arrive. Device clocks do not choose a winner.

- **Offline/provider failure:** previous observations remain; errors identify missing/stale data.
- **Interrupted/corrupt snapshot:** invalid revision ignored; last valid data remains.
- **Failed save:** edits stay in memory, with Retry save / Save copy. Export before discarding or closing.
- **Lost folder grant:** reconnect the same directory. Unsaved data can be saved to a new empty folder.
- **Backup:** Settings → Export backup. Restore validates before confirmation and creates a new revision.
- **Newer schema:** upgrade the app; older builds refuse to modify it.

Files are plaintext financial records unless encryption is on. Protect your device and sync destination. Snapshot cleanup is not implemented. Keep the complete snapshot ancestry when syncing; use Export backup for a standalone portable copy.

## Checks

```sh
./scripts/gradle testDebugUnitTest lintDebug
./scripts/gradle connectedDebugAndroidTest  # running emulator/device
python3 scripts/check-providers.py         # public test addresses; no user keys
```

The device test creates a uniquely named `CapitalTest-*` folder through Android's real folder picker. It does not delete existing folders. Unit checks cover exact money round-trip, allocation caps/priorities/rerouting/conservation, 100 generated graphs, canonical addresses, snapshot conflicts/corruption, provider partial failure, bounded retries, cancellation and safe observation merging.

Roadmap: [ROADMAP.md](ROADMAP.md). Implementation plan and remaining acceptance checks: [OpenSpec tasks](openspec/changes/archive/2026-09-29-build-android-savings-tracker/tasks.md). Emulator acceptance record: [acceptance.md](openspec/changes/archive/2026-09-29-build-android-savings-tracker/acceptance.md). Research: [sources](openspec/changes/archive/2026-09-29-build-android-savings-tracker/research.md). No release signing key is committed; the supplied APK is a debug build.
