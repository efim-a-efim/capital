# Acceptance — FIRE goal (2026-10-21, emulator Medium_Phone, debug build)

| # | Check | Result |
|---|---|---|
| F1 | Goal editor: Goal type Savings goal / FIRE; FIRE fields (birth, FIRE date, contribution + period, spend, amount); Advanced assumptions with the eight fields labelled "· now 3 / 2 / 5 / 7 / 10 / 4 / 4 / 95" | PASS — `screenshots/F1-editor.png` |
| F2 | Goal "Freedom": birth 1990-05-01, €1,000 per month, spend €2,500, blanks derived: target €750,000 (4 % rule), FIRE date Nov 2058 · age 68 (average), Dec 2049 · age 59 (optimistic), not reachable before Apr 2085 (pessimistic: 4 % growth under 5 % inflation); money lasts 83 years beyond life expectancy / never ends 🥳 / short by €214,402 | PASS — `F2-goal.png` |
| F3 | Burndown: three curves, dashed FIRE-date line, caption, legend, disclaimer | PASS — `F3-chart.png` |
| F4 | Refresh with the default providers: World Bank inflation and market growth fetched for EUR, listed under Quotes and freshness; derived FIRE date moved from 2058 to Apr 2053 and the goal's due date followed after the merge | PASS — `F4-settings-rates.png` |
| F5 | `FireTest` 15, `ProviderTest` +10 (recorded World Bank, ECB, OECD, Yahoo responses, errors, merge), `CoreTest` schema 7; full unit suite including `I18nTest` | PASS (exit 0) |
| F6 | Live reachability `scripts/check-providers.py`: World Bank ×2, ECB, OECD, Yahoo → 200 | PASS |
| F7 | 14 translations: `site-check.py` OK for every language; goal screenshot captured per language, RTL (ar, ur) mirrored correctly | PASS |
| F8 | Instrumentation `AppTest`, lint, assembleDebug | PASS (exit 0) |
