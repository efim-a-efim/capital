# Acceptance — Brokers screen (2026-10-03, emulator Medium_Phone, API 36, debug build)

| # | Check | Result |
|---|---|---|
| M1 | Brokers tab in the bottom bar; empty state text, Credentials section (moved from Settings), setup-guide link | PASS — `screenshots/M1-brokers.png` |
| M2 | + → Add broker account: name, Broker choice, Flex Query id; saved account row shows Read-only · broker · id, Balance unknown, Not refreshed, *Not linked to a bucket*, Edit / Delete | PASS — `M2-brokers-account.png` |
| M3 | Bucket → Add holding → Tracking = Broker account shows the account picker only (no broker/id fields) | PASS — `M3-link-editor.png` |
| M4 | Linked holding in the bucket: Read-only · Interactive Brokers · 123456; after Refresh the account error *Interactive Brokers needs its credentials on the Brokers screen* appears under the holding | PASS — `M4-bucket-linked.png` |
| M5 | Brokers tab shows the same error and *Linked to Reserve* (opens the bucket); Delete confirmation names the holding; after confirming the account and the holding are gone | PASS — `M5-brokers-linked.png` |
| M6 | Seeded schema-6 snapshot with two accounts: values in own and default currency, link state, *Counted as 0: below €1.00* for the 0.42 EUR OANDA account with threshold 1 | PASS — `M6-brokers-seeded.png` |
| M7 | Account editor: Ignore balances less than checkbox, Minimum in EUR field (default 1), explanatory note | PASS — `M7-editor-threshold.png` |
| M8 | Language switch: Brokers screenshot captured in 15 languages (`docs/screenshots/<code>/brokers.png`), RTL layout correct for ar/ur | PASS |
| M9 | Unit tests 99/99 (`testDebugUnitTest`, includes `I18nTest`); instrumentation `AppTest` on the emulator | PASS |

Not recaptured: the other screen screenshots still show four bottom tabs (data set of the original captures is not on the emulator).
