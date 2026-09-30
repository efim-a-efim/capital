---
layout: default
lang: en
base: ""
key: "privacy"
title: Privacy Policy
class: doc
---
# Privacy Policy

<p class="meta">Capital for Android (package <code>dev.capital</code>) · Developer: {{ site.developer }} · Effective 30 September 2026</p>

## Summary

- Capital has no user accounts, no analytics, no advertising, no crash reporting and no servers run by the developer. The developer never receives your data.
- Your financial records are stored only on your device, in a folder you choose. You can encrypt them with a password.
- The only network traffic is the requests the app makes, on your instruction, to the price and blockchain data operators you select in Settings. Those requests carry the public wallet addresses, token contracts and currency codes you track, and any API key you entered for that operator.

## What the app stores on your device

**In the folder you choose.** Buckets, holdings, wallet addresses, goals, connections, planned savings, cached prices and the settings that belong to that data. Files are plain text unless you switch on encryption (Settings → Security). With encryption on, every file is encrypted with AES-256-GCM using a key derived from your password with Argon2id. There is no password recovery.

**In app-private storage** (not accessible to other apps):

| Item | Purpose |
|---|---|
| Grant to the selected folder | Reopen the folder on the next launch |
| Provider API keys you entered | Sent only to the operator that issued them; encrypted with a key held in Android Keystore; excluded from snapshots, exports and OS backups |
| Lock settings | Unlock the encrypted folder without the password: a copy of the data key, encrypted with a key derived from your PIN and bound to Android Keystore. The PIN itself is not stored |
| Language and theme choice | Interface preferences |

Android backup and device-to-device transfer are disabled for the app, so none of this is copied to Google or to another device by the system.

## What leaves your device

Capital contacts only the operators you choose in Settings, only over HTTPS, and only when you refresh or test sources. Each request is answered and discarded; the app keeps the returned balances and prices in your folder, not the request.

| Data sent | To whom | Why |
|---|---|---|
| Public wallet addresses you added | The blockchain data operator selected for that chain | Read the balance and token holdings of the address |
| Token contract addresses and asset ids | The crypto price operator you selected | Price the assets |
| Currency codes | The fiat rate operator you selected | Convert between currencies |
| The API key you entered for an operator | That operator only | Authenticate your own account with them |

Every operator also sees your IP address, as with any internet request. The operators are independent of the developer and process the request under their own terms and privacy policies, which are linked from Settings → Sources / attribution in the app:

| Data | Operators |
|---|---|
| Bitcoin | [Blockstream](https://blockstream.info), [mempool.space](https://mempool.space) |
| Ethereum and ERC-20 tokens | [PublicNode](https://publicnode.com), [Alchemy](https://www.alchemy.com), [Blockscout](https://www.blockscout.com), [Ethplorer](https://ethplorer.io) |
| TON and jettons | [TON Center](https://toncenter.com), [TonAPI](https://tonapi.io) |
| TRON and TRC-20 tokens | [TronGrid](https://www.trongrid.io), [PublicNode](https://publicnode.com) |
| Crypto prices | [DefiLlama](https://defillama.com), [CoinGecko](https://www.coingecko.com), [CoinPaprika](https://coinpaprika.com) |
| Fiat rates | [Frankfurter](https://frankfurter.dev), [European Central Bank](https://www.ecb.europa.eu) |

Nothing is sent anywhere else. No data is sold, shared for advertising, or used to build profiles. Public blockchain queries disclose that the address you track is of interest to someone at your IP address; use a VPN if that matters to you.

## What the app never does

- It never asks for, stores or transmits private keys or seed phrases. It cannot sign or send transactions.
- It never transfers money. Goal allocations are calculations shown to you and nothing else.
- It never contacts the developer. There is no telemetry, no update check inside the app, no push notifications.

## Permissions

| Permission | Use |
|---|---|
| Internet | Requests to the operators listed above |
| Folder access | Granted by you through the Android folder picker for the folder you select; the app cannot read other folders |
| Biometrics | Unlock with fingerprint or face through Android's own prompt; the app receives only success or failure, never biometric data |

## Sync and backups

Capital does not sync anything itself. If you place the folder under a sync tool (Syncthing, Nextcloud, Google Drive, …), that tool's privacy terms apply to the copies it makes. Files are plain text unless encryption is on; earlier plaintext copies made before you switched encryption on remain readable by whoever holds them.

**Export backup** in Settings writes a single file to a location you choose. It contains the same records and is protected only as well as that location.

## Deleting your data

Delete the folder you chose (and any copies your sync tool made) and uninstall the app. Uninstalling removes app-private storage, including provider keys and lock settings. The developer holds nothing to delete and cannot delete anything on your behalf. The operators you queried may keep request logs under their own retention rules.

## Children

Capital is a personal finance tool for adults. It is not directed at children under 13 and knowingly collects no data from them.

## Changes to this policy

The current version is always at [{{ site.url }}{{ page.base }}/privacy]({{ page.base }}/privacy). Material changes are listed in the release notes of the version that introduces them.

## Contact

{{ site.developer }} · [{{ site.contact }}](mailto:{{ site.contact }}) · [Issue tracker]({{ site.repo }}/issues)
