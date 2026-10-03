---
layout: default
lang: en
base: ""
key: "manual"
title: User manual
class: doc
---
# User manual

<p class="meta">Capital 2.2 · Android 8.0 and newer</p>

## The idea

You keep money in several places: a savings account, cash, a brokerage, a crypto wallet. Capital calls each place a **bucket**. You want that money for several things: an emergency fund, a trip, a laptop. Capital calls each one a **goal**. You connect buckets to goals, and the app works out how far each goal is funded from what you have today. Add the savings you **plan** to make and it also tells you the date each goal closes.

Nothing in the app moves money. It is a mirror of what you own and a calculator for what it covers.

## First launch

1. **Choose a folder.** Pick a dedicated folder on the device, for example `Documents/Capital`. This is where every record is written. A folder that already holds Capital data opens directly.
2. **Settings → Default currency.** Totals and the Overview are shown in it.
3. Optionally set **provider keys** in Settings for operators that offer higher limits with a free key (Alchemy, TronGrid, TON Center, CoinGecko). Every chain and price source has a keyless default. Broker accounts need an **access token** from the broker, entered on the Brokers tab; see [Broker and forex accounts]({{ page.base }}/accounts).

## Buckets

Buckets tab → **+**. Give the bucket a name and a currency. Open it to add holdings:

- **Manual holding**: a name, a currency or asset code (EUR, USD, BTC, a stock ticker you value yourself…) and a quantity. Use it for bank balances, cash, anything the app cannot read.
- **Wallet holding**: choose the chain (BTC, ETH, TON, TRX) and paste one public address. On refresh the app reads the native balance and, for ETH, TON and TRX, the fungible tokens on the address.
- **Broker account**: choose one of the accounts added on the Brokers tab. On refresh the app reads the account's total value in its base currency. One account can be linked into one bucket.

Amounts accept a decimal point or comma, without grouping separators. Each bucket shows native quantities and their value in your default currency. If a quote is missing the total is marked incomplete; a stale cached value stays usable with a warning.

**Tokens.** A token is identified by its contract address, never by its name. It counts only when your selected price source lists that exact contract; everything else appears as *Unknown token · not counted* and stays out of totals. Open a wallet holding's editor to fetch its tokens and switch off the ones you do not want.

**Portfolio mode** (bucket settings) treats a bucket as an investment portfolio: set a target percentage per asset, see real share versus target, and use **Rebalance** to get a list of what to buy for a given amount. Sells are suggested only when *Allow sells during rebalance* is on. It is a calculator; it changes nothing.

## Goals

Goals tab → **+**. A goal has a name, a currency, a target amount and a due date. Open the goal and **Connect bucket** to say which buckets may fund it, optionally with a limit: a fixed amount, a percentage of the bucket, or a percentage of the goal.

How money is allocated:

- Goals with an earlier due date are funded first. Goals that share a date are funded in the order shown; drag the handle to reorder them.
- A bucket connected to several goals is split between them according to the limits, and never counted twice.
- The result is shown as *funded / target* and *Still needed*. On the Overview you see the total, what is allocated to goals and what is left.

**Badges.** *Funded* (green) when today's savings already cover the goal. *Funded in time* (green) when planned savings close it on or before its due date. *Not funded* (yellow) otherwise. The text under the goal says when it closes or how much it falls short.

**Archive** a goal to keep it without counting it. Archived goals are listed at the bottom.

## Plans

Plans tab → **+**. A planned saving is an amount you intend to add on a date, for example your salary savings at the end of each month. Plans are not part of your savings; they only extend the projection: "planned savings close this goal on 30 Oct 2026 · on time".

Money from plans is applied after today's buckets, to the goals in due-date order, so it only tops up what is still open. When a plan's date has passed it moves to the **Archived** section and is no longer counted: either you already moved the money into a bucket and the app sees it there, or the plan did not happen. Edit the date into the future to make it active again; delete it if it is obsolete.

## Brokers

Brokers tab → **+**. A broker account is a read-only connection: pick the broker (Interactive Brokers, OANDA, Trading 212 or SnapTrade), enter the account or query id and save; the broker's token or key is entered once under **Credentials** on the same tab and covers every account of that broker. The tab lists each account with its last value and the bucket it is linked to. **Ignore balances less than** an amount in your default currency (1 by default) makes a dust balance count as 0 in the bucket. Link it into a bucket with **Add holding → Broker account**; delete the holding to unlink it, delete the account to remove the connection. The setup steps, with links to the brokers' own documentation, are on [Broker and forex accounts]({{ page.base }}/accounts).

## Refresh

The refresh icon at the top reloads every wallet balance, broker account value and price. A bucket can be refreshed alone. The app refreshes once on a cold start; coming back from the background only reloads the local files. Refreshing needs internet; without it the previous values remain and are marked stale.

## Security

Settings → Security.

- **Encryption** encrypts every file in the folder, including older revisions, with a password. Switching it off decrypts them. There is **no password recovery**: a lost password means the data cannot be opened. Plaintext backups made before you switched encryption on remain readable; the app warns about them but cannot delete them.
- **PIN** and **biometrics** are available while encryption is on. *Use password* is always available on the PIN screen. After 10 wrong PINs the PIN is removed and only the password works. Wrong entries never delete data.
- While encryption is on, screenshots and the recent-apps preview are blocked.

## Sync, backup, recovery

Capital writes snapshot files with revision ids and checksums into your folder and never runs its own sync. Put the folder under any sync tool you already use. If two devices edit at the same time, the app shows a conflict screen and lets you pick a version; both originals stay on disk.

- **Export backup** (Settings) writes one portable file. **Restore** validates it before anything changes.
- A failed save keeps your edits in memory with *Retry save* and *Save copy*.
- If the folder grant is lost, reconnect the same folder.
- A file written by a newer version of the app is refused by an older one; update the app.

## Language

The app starts in the device language when it is one of the 15 supported ones, otherwise in English. Change it in Settings → Language.

## Installing outside Google Play

Download the APK from the [latest release]({{ site.repo }}/releases/latest) and open it; allow installation from that source when Android asks. Every release is signed with the same key, so new versions install over old ones and keep your settings. The folder with your data is never touched by an update or an uninstall.
