# Acceptance — rate history and Auto update rates (2026-10-22, emulator Medium_Phone, debug build)

| # | Check | Result |
|---|---|---|
| R1 | `ProviderTest`: Frankfurter range parsing (inverse rate, chunks of 365 days), DefiLlama chart parsing (UTC day, 20 keys / 1000 days per request), per-asset and whole-request errors, merge union + `recorded()`, earliest-day rule (minimum of stored series, oldest observation, one year back), full refresh fetches history while a bucket refresh does not | PASS |
| R2 | `RatesJobTest`: cache merge replaces only newer quotes and adds history points on UTC dates, snapshot round trip, cache cap 2000 | PASS |
| R3 | Live reachability (`scripts/check-providers.py`): Frankfurter range and DefiLlama chart → 200 | PASS |
| R4 | Emulator: Settings → Auto update rates → Every 1 h; `cmd jobscheduler run -f dev.capital 1001` wrote `rates-cache.json` (1 of 1 quotes); after restart the cache was merged and deleted, "Last background update" shown | PASS |
| R5 | Emulator: full refresh with Rate history on fills the EUR series in the data file: schema 8, `rate:EUR` with 257 points from 2025-10-21 to 2026-10-22 (found and fixed: the merge call dropped the fetched history) | PASS |
| R6 | 14 translations: `site-check.py` OK; `I18nTest`; lint; assembleDebug | PASS |
| R7 | Instrumentation `AppTest` | PASS (exit 0) |

Settings screenshots unchanged: the captured area (Preferences and the first provider rows) does not include the new choice.
