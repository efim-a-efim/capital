---
layout: screen
lang: en
base: ""
key: "screens/bucket"
screen: bucket
title: Bucket
---
# Bucket

**What it is.** One bucket with its holdings. Reached by tapping a card on the [Buckets]({{ page.base }}/screens/buckets) tab; **← All buckets** goes back.

**Header.** The bucket's value, then two numbers that only make sense together: **Allocated**, the part claimed by connected goals, and **Available**, the rest. **Edit bucket** opens the name, currency and portfolio switches. **Delete bucket** removes the bucket and its holdings after a confirmation.

**Holdings.** Each holding shows its name, its value in the bucket currency, how it is tracked (*Manual*, *Wallet* or *Broker account*), the native quantity, when the value was observed and when it was last fetched. **Edit / move** changes it or moves it to another bucket; **Delete** removes it.

**Add holding** opens the holding editor:

- **Manual**: a name, a currency or asset code and the quantity. Use it for anything the app cannot read.
- **Wallet**: pick the chain (BTC, ETH, TON, TRX) and paste one public address. The app reads the native balance on refresh and, on ETH, TON and TRX, the fungible tokens on that address. Open the editor again and press **Fetch tokens** to see them and switch off the ones you do not want counted.
- **Broker account**: pick the broker (Interactive Brokers, OANDA, Trading 212 or SnapTrade) and enter the account or query id; for SnapTrade, **Fetch accounts** lists the connected accounts to choose from. On refresh the app reads the account's total value in the account's base currency; the access token is entered in Settings → Broker accounts. **Setup guide for broker accounts** opens [Broker and forex accounts]({{ page.base }}/accounts), which lists the steps for each broker.

**Tokens and "not counted".** A token is identified by its contract address. It counts only when your price source lists that exact contract; otherwise it is listed as *Unknown token · not counted* and stays out of the totals. This is what keeps an airdropped fake "USDT" out of your savings.

**Portfolio mode.** When it is on, the screen adds a table with value, real share, target and difference per asset, plus a **Rebalance** button that asks for an amount and lists what to buy. Sells appear only when *Allow sells during rebalance* is on. Nothing is traded.
