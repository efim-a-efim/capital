# Acceptance: broker accounts (emulator-5554, Medium_Phone, 2026-10-03)

No funded broker account was available, so live checks cover the endpoints' reachability and error paths; the success paths are covered by unit tests with captured response shapes.

| # | Result | Observation | Shot |
|---|--------|-------------|------|
| K1 | PASS | Settings shows "Broker accounts" with one button per credential (Interactive Brokers, OANDA, Trading 212 key and secret, SnapTrade client id and consumer key), the setup-guide button and the storage note. | K1-settings-brokers.png, K7-settings-de.png (German) |
| K2 | PASS | Holding editor: Tracking offers Manual / Wallet / Broker account; Broker choice lists the four brokers; id field label follows the broker (Flex Query id, OANDA account id, Trading 212 account number, SnapTrade account id). Saxo was removed afterwards (short-lived tokens); the K1/K2/K7 captures predate the removal. | K2-editor-account.png |
| K3 | PASS | Saved an Interactive Brokers holding (query 123456): row "Read-only · Interactive Brokers · 123456 · Balance unknown", source "Not refreshed", bucket total unaffected. Snapshot schema 5. | K3-account-row.png |
| K4 | PASS | Refresh without a token: "IBKR: Interactive Brokers needs your access token in Settings → Broker accounts"; previous state kept. | K4-refresh-no-token.png |
| K5 | PASS | Bogus token saved, refresh against the live Flex Web Service: "IBKR: Interactive Brokers error 1020 Invalid request or unable to validate request." (IBKR answers 1020 rather than 1015 for an unknown token). | K5-refresh-bad-token.png |
| K6 | PASS | SnapTrade in the editor: Fetch accounts and Connect buttons; Fetch without credentials reports "SnapTrade needs your access token in Settings → Broker accounts". | K6-editor-snaptrade.png |
| K7 | PASS | Live reachability (scripts/check-providers.py and manual probes): IBKR SendRequest 200 with FlexStatementResponse error XML, OANDA 401 JSON, Trading 212 401, SnapTrade status 200. | — |
| K8 | PASS | Unit tests 99/99 incl. I18nTest; `site-check.py` OK for all 14 languages. | — |

## Not covered

- Success paths of all four brokers (need funded accounts and real credentials); covered by ProviderTest with documented response shapes.
- Settings screenshots were not recaptured: the Broker accounts section is below the first screen, so the published captures do not change visibly. The bucket screenshot has no account holding.
