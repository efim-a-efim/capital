# Acceptance — more brokers (2026-10-04, emulator Medium_Phone, debug build)

| # | Check | Result |
|---|---|---|
| P1 | `PluginTest`: each new plugin against the broker's documented request (URL, auth headers, body) and response example; tastytrade invalid_grant, Capital.com missing session, eToro unknown account, host rejection of negative totals and bad currency codes | PASS (113/113 unit tests) |
| P2 | Live reachability with dummy credentials (`scripts/check-providers.py`): Alpaca 401, Tradier 401, tastytrade 400 invalid_grant, Public.com 401 invalid_secret, eToro 401, Indexa 401, ALOR 403, Capital.com 400 missing key, Akahu 401 | PASS |
| P3 | T-Invest on the emulator with a dummy token: Fetch accounts completes the TLS handshake under the scoped Russian Trusted Root CA and shows "Access denied" (HTTP 401) instead of a network error | PASS |
| P4 | Account editor lists all 14 brokers; Credentials section shows each plugin's credential buttons | PASS |
| P5 | Translations: 18 app strings and the accounts page (setup sections, support list by market, messages) in 14 languages; `I18nTest`, `site-check.py` OK; cross-language contamination from a translation agent in the Hindi page found and removed | PASS |
| P6 | Instrumentation `AppTest` on the emulator | PASS |

No broker test accounts were created: sandbox or paper environments of these brokers need a sign-up, so the integrations follow the API references and the documented examples.
