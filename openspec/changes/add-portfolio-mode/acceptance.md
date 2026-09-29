# Portfolio mode acceptance (emulator-5554, USD default currency)

| Step | Result | Screenshot |
|---|---|---|
| F1 setup | PASS. Invest USD; Dollars 7000 USD; Euros 2500 EUR shown as $2,842.82 (rate 1.13713 USD/EUR). Bucket $9,842.82 | F1-invest, F1-holdings |
| F2 goals unaffected | PASS. Before and after: Allocated to goals $600.00, Emergency $600.00 / $600.00, total $10,742.82 (also after relaunch) | F2-overview-before/after |
| F3 enable + targets | PASS. 60+35: "Total 95% · 5% missing"; Save refused: "Targets must total 100%. Now 95%."; 60+40: "Total 100%". Real 71.12/28.88, drift +11.12/-11.12 | F3-* |
| F4 buys only | PASS (UX issue: quantity precision) | F4-* |
| F5 sells allowed | PASS | F5-* |
| F6 off/on | PASS. Off: Portfolio section and Rebalance gone; on: 60/40 and sells still set | F6-off, F6-on |
| F7 unavailable | PASS | F7-* |
| F8 target not held | PASS (GBP rate not cached: shares unavailable naming GBP) | F8-* |
| F9 persistence | PASS. schema 4; Invest portfolio true, targets {USD:60, EUR:40}, allowSells true | F9-relaunch |
| F10 logcat | PASS. No FATAL EXCEPTION | - |

## Arithmetic
- Real: USD 7000/9842.82=71.1178% -> 71.12 (app 71.12); EUR 28.8822% -> 28.88; drift +/-11.12. Match.
- F4, amount 2000: T=9842.82+2000=11842.82. USD target 7105.692, deficit 105.69 (app "Buy $105.69"). EUR target 4737.128, deficit 1894.31 (app "Buy $1,894.31"). Sum 2000.00. EUR qty 1894.31/1.13713 = 1665.88 (app 1,665.876...). Results 60.00%/40.00%. Match.
- F4 amount 0: both "No trade", result 71.12% / 28.88%. Match.
- F4 "abc" and "-5": red text "Enter a nonnegative number (up to 18 decimals)", no rows. Match.
- F4 "1500,50": accepted. Deficit split would push USD negative, so all to EUR: USD "No trade" (result 61.71% = 7000/11343.32), EUR "Buy $1,500.50", 1,319.5547... EUR. Match least squares.
- F4 dialog says "Buys only. Allow sells in the bucket settings." After Close: no "Saved on device", holdings unchanged.
- F5 sells, amount 1000: T=10842.82. USD target 6505.692 -> "Sell $494.31", qty 494.3109... USD, result 60.00%. EUR target 4337.128-2842.82 = 1494.31 -> "Buy $1,494.31", qty 1,314.112... EUR, result 40.00%. Buy-sell = 1000.00. Match. Header "Sells allowed".
- F7: Krona ISK 1000: bucket row "Unavailable · USD"; Portfolio: "Portfolio shares unavailable: no value for ISK. Refresh or remove the target."; Rebalance: "Rebalance unavailable: no value for ISK. Refresh or remove the target." After delete Krona: weights back (71.12 / 28.88).
- F8: GBP 10, USD 50, EUR 40 saved; Portfolio: "Portfolio shares unavailable: no value for GBP. Refresh or remove the target." Removed GBP, USD 60: weights back.
- F9 snapshot: .../CapitalTest-1790710648307/capital-8ce2c42b-5137-4eaa-96bd-1302485efe40.json (newest by mtime, the folder is nested under /sdcard/Documents/CapitalTest-copy/...). envelope keys: payload, sha256; payload.schema=4.

## Bugs / UX
1. Quantity formatting in RebalanceDialog: raw 18+ digit decimals, e.g. "105.6890415164712695 USD", "1,665.876000000000001876 EUR", "1,314.112000000000001667 EUR". Also "0 USD". Should be rounded (2 decimals fiat, 6-8 crypto). USD quantity (105.689...) does not equal the shown buy amount $105.69.
2. Wording: for a held asset without a target (ISK) the notice says "Refresh or remove the target"; should say to refresh or remove the holding.
3. Save error "Targets must total 100%. Now 95%." appears at the top of the editor, far from the Total line at the bottom; it is not visible when the editor is scrolled down, and its appearance/disappearance shifts the fields by ~105 px (taps land on wrong field). The message does not state the missing amount ("Now 95%" only; spec says "stating the missing 5"; the "5% missing" is only in the Total line).
4. Portfolio rows in bucket detail have large vertical gaps between asset row and next asset (approx 140 px between subtitle and next name).
5. Editor Settings had no "Storage" text visible in Settings (folder URI not shown where the brief expected); found snapshot folder by find on /sdcard/Documents.
6. App auto-refreshes rates on relaunch ("Refreshed. Shared rates may revalue other buckets."), so ISK/GBP could become cached on later runs; this happened after F7/F8, so no effect.
7. A stray BACK during F7 setup left the holding editor unopened (test-driver mistake, no data changed).
