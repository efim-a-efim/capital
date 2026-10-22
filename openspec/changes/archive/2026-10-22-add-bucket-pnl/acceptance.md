# Acceptance — bucket PnL (2026-10-22, emulator Medium_Phone, debug build)

| # | Check | Result |
|---|---|---|
| P1 | `PnlTest` (11): zero PnL on unchanged value, price doubling → +100 % TWR, deposit is a flow not profit, broker deposit excluded for a linked account, null periods before the history, TWR with a mid-period flow, cross-currency bucket, incomplete on a missing rate, `recordedPnl` points/flows/no-op when off | PASS |
| P2 | `PluginTest` (22): flows and history for Interactive Brokers, Alpaca, Tradier, tastytrade, eToro (history), OANDA, Trading 212, SnapTrade, Public.com, T-Invest, Capital.com with documented responses, paging and the IBKR missing-section error | PASS |
| P3 | `BackfillTest` (2) and `ProviderTest` (49): revisions → `qty:`/`rate:` points (last per day, change only, cap, broken file skipped), refresh collects flows for PnL buckets only with the 90-day re-read window, merge by ref, error line names broker and account, rate-history start extended to the bucket's earliest point | PASS |
| P4 | Emulator: bucket "Growth" with a manual EUR holding, Investment PnL on, refresh → Investment section with five rows, Since note, chips, Value and Profit-and-loss charts | PASS — `screenshots/P1-bucket-pnl.png` |
| P5 | FIRE chart unchanged after the `LineChart` extraction | PASS — `screenshots/P2-fire-chart-after-refactor.png` |
| P6 | 14 translations (`site-check.py` OK), `I18nTest`, lint, assembleDebug; instrumentation `AppTest` ran green on the v2.16.0 code base (no UI test touched by this change) | PASS |

No funded broker account was available: flows and history follow the API references; details coded defensively are listed in `research.md` and the plugin comments (Capital.com preferred-account limit, Tradier synthetic refs, tastytrade net-liq shape).
Bucket screenshots were not retaken: the switch is off by default, so the existing captures stay accurate.
