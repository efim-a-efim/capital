## Why

Some buckets are investment portfolios with intended weights per asset. Today the app shows only values, so the user must work out drift and what to buy with new money by hand.

## What Changes

- New bucket setting **Portfolio mode**, switchable at any time. Switching it off hides portfolio views and keeps the stored targets.
- Target percentage per asset in the bucket settings. Targets must total 100.
- Bucket detail shows real and target percentage per asset, measured in the base currency, which is the default currency from Settings.
- **Rebalancing calculator**: the user enters an amount to invest in the base currency and gets a buy list that brings the portfolio as close to the targets as possible. It only calculates. It never changes holdings.
- Bucket setting **Allow sells during rebalance**, off by default. When on, the calculator may also recommend sells.
- Snapshot schema 4 with conversion of older data.

## Capabilities

### New Capabilities
- `portfolio`: portfolio mode, target weights, real versus target display, rebalancing calculator and sell permission.

### Modified Capabilities

None. Allocation to goals, valuation and storage requirements are unchanged; new bucket fields have defaults.

## Impact

`domain/Model.kt` (bucket fields, asset weights, validation), new `domain/Rebalance.kt`, `data/Snapshots.kt` (schema 4), `ui/CapitalApp.kt` (bucket editor, weights table, calculator), tests, README. No new dependencies. No network use beyond the existing refresh.
