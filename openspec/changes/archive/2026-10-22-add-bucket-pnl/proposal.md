## Why

A bucket shows what it is worth now, not how it got there. Investors want to know what the money earned over a day, a week, a month, a year and since the start, separated from what they paid in.

## What Changes

- **Per-bucket switch** **Investment PnL** (editor checkbox, off by default).
- **Data**: quantity history per holding (`Portfolio.history` keys `qty:<holdingId>`, sparse: a point when the value changes; for a linked account the account's total in its currency) and external cash flows (`Portfolio.flows`: holding, date, amount, currency, source). Broker flows come from a new optional plugin capability `flows(host, id, since)`; where the broker also offers a value history (`history(host, id, since)`) it backfills the quantity series. Manual and wallet holdings: a quantity change is a flow at that day's price.
- **Backfill** when the switch is turned on: daily values and quotes from the folder's past revisions, rates from the history providers, flows and value history from the brokers (as far back as each API allows).
- **Bucket screen** when on: an **Investment** section with PnL amount and time-weighted return for all time, last year, month, week and day; a value chart; a PnL chart with time-frame chips All / 1Y / 1M / 1W / 1D that focus the curve.
- Plugin support: Interactive Brokers (flows + daily NAV), Alpaca (flows + history), Tradier (flows + balances), tastytrade (flows + net-liq), SnapTrade (flows; balance history is beta and off by default → flows only), OANDA, Trading 212, Public.com, T-Invest, Capital.com (flows only), eToro (value history only, flows unavailable → PnL shown from history, deposits counted as PnL — labelled), Indexa (flows with undocumented codes → not implemented), ALOR, Akahu (neither → not implemented). The site's broker table gets a PnL column.
- Docs (bucket screen, manual, plugin guide), 15 languages, bucket screenshots, minor release.

## Non-goals

- Per-position PnL, realised vs unrealised, tax lots, dividends as separate lines.
- Rewriting history when a broker corrects past transactions (next refresh re-reads the last 90 days only).

## Capabilities

### New Capabilities
- `bucket-pnl`.

### Modified Capabilities
- `broker-accounts`: optional flows/history capability; `savings`: bucket switch; `local-storage`: flows and qty series.
