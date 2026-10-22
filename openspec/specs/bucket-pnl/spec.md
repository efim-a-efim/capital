# bucket-pnl Specification

## Purpose
Investment PnL per bucket: quantity history, broker cash flows, time-weighted return and charts.
## Requirements

### Requirement: Investment PnL per bucket
A bucket MAY have Investment PnL on. The app SHALL then keep a quantity history for its holdings and external cash flows, SHALL compute the profit or loss in the bucket currency as value change minus net flows and the time-weighted return for all time, last year, last month, last week and last day, and SHALL show a value chart and a PnL chart with time-frame focus.

#### Scenario: Deposit is not profit
- **WHEN** a linked account was worth €10,000, received a €1,000 deposit and is worth €11,200 a month later
- **THEN** last-month PnL is +€200 and the return is computed on €11,000 of capital.

### Requirement: Broker flows and history
Plugins MAY implement flows and value history. The host SHALL read flows since the last known flow minus 90 days on every refresh and the history once when the switch is turned on. Brokers without flows SHALL be listed as PnL-unsupported on the site.

#### Scenario: Backfill from revisions
- **WHEN** the switch is turned on for a bucket with two years of refreshes in the folder
- **THEN** the value chart starts two years ago, built from the daily revisions and the rate history.
