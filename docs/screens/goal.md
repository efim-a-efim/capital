---
layout: screen
lang: en
base: ""
key: "screens/goal"
screen: goal
title: Goal
---
# Goal

**What it is.** One goal with its funding. Reached by tapping a card on the [Goals]({{ page.base }}/screens/goals) tab; **← All goals** goes back.

**Header.** Name and badge, funded / target, due date, the projection line, and **Still needed**: target minus what is funded today.

**Buttons.** **Edit goal** changes name, currency, target and due date. **Archive** keeps the goal without counting it; an archived goal shows **Activate** instead. **Delete** removes the goal and its connections after a confirmation.

## FIRE {#fire}

A goal of type **FIRE** (financial independence, retire early) answers two questions: when can the money start to pay your living costs, and how long does it last. It is a projection on your own numbers; it recommends nothing. Set the type with **Goal type** (**Savings goal** / **FIRE**) in the editor.

**Editor fields.** Every field except the name and currency may be blank; the app derives what it can from the rest.

- **Birth date · YYYY-MM-DD · optional**: shows ages and sets the horizon to the life expectancy.
- **FIRE date · YYYY-MM-DD · blank to derive**: the day you stop saving and start spending. A date in the past means the spend period has started: the plan runs from today with the money the goal has now.
- **I can invest · blank to derive**: an amount per **week**, **2 weeks**, **month**, **year** or **N days** (then enter **Days**).
- **Contribution growth % per year · blank = inflation**: how much more you can invest each year, for example a yearly raise. Blank keeps the contribution's purchasing power.
- **Spend per month after FIRE · blank to derive**.
- **Amount to have after FIRE · blank to derive**: the FIRE number, the goal's target.
- **Advanced assumptions**: **Inflation % · average / optimistic / pessimistic**, **Growth % · average / optimistic / pessimistic**, **Withdrawal rate %**, **Work until age** (default 65) and **Life expectancy · years**. Each label ends with "· now {value}", the value in effect when the field is blank.

All amounts are in today's money of the goal currency. The screen also shows the value at the FIRE date in future money. The money the goal has today comes from its connected buckets, like any goal; [Plans]({{ page.base }}/screens/plans) do not enter the FIRE calculation.

**What is derived.**

- FIRE date: the first month from which the spend lasts until life expectancy (50 years without a birth date) and, when a target is given, the target is reached; not later than **Work until age**, else *Not reachable before* that birthday. Without a target the FIRE number becomes the amount that lifetime needs.
- Contribution: from target and FIRE date.
- Target: from spend (spend × 12 ÷ withdrawal rate) when the FIRE date is given, or from FIRE date and contribution.
- Spend: from target (target × withdrawal rate ÷ 12).

If FIRE date, contribution and target are all given, the date is kept and the screen shows the shortfall and the earliest date that is reachable. The derived target and date are written into the goal's target and due date, so ordering, allocation, the Overview and Plans work as for any goal. They are refreshed on every edit and every refresh.

**The FIRE section.** First the values that are the same in every scenario (what you entered, and the **Withdrawal rate**), one per line. Then one foldable row per scenario, **Average**, **Optimistic** and **Pessimistic**, with its growth and inflation rates, the derived FIRE date when you left it blank, and a badge: **Money is enough** (green) when the money lasts beyond life expectancy, **money will never end 🥳** (green) when it is never used up, **Not enough money** (yellow) when the FIRE date is not reachable or the money runs out earlier. Tap the row to unfold the values that differ by scenario:

| Row | Meaning |
|---|---|
| **FIRE date** | The date, with "age N" when the birth date is known, or *Not reachable before <date>* |
| **Contribution per month** | What must be invested each month |
| **FIRE number · today's money** | The amount to have, as the editor states it |
| **FIRE number · at the FIRE date** | The same amount after inflation |
| **Spend per month · today's money** | The monthly spend after FIRE |
| **Spend per month · at the FIRE date** | The same spend after inflation |
| **Spend now · per month** | 0 before the FIRE date. After it, what you can take out this month: the money the goal has today, paid out in equal amounts in today's money until life expectancy |
| **Spend now · per year** | The same, for 12 months |
| **Money lasts** | *until <date> · age N*, *N years · beyond life expectancy*, *money will never end 🥳*, or *short by …* when the date is not reachable |

Under the folds, a "Rates: …" line names the provider the rates come from, or *built-in defaults*. The chart below is a burndown: the value of the money in today's money from today to the end of the money or the horizon, with three coloured curves, one per scenario. The left axis shows amounts in the goal currency, the bottom axis the years (today, the FIRE date, the end). The dashed line marks the FIRE date. The note under it reads *A projection on your own numbers; it recommends nothing.* On the [Goals]({{ page.base }}/screens/goals) tab the goal shows *FIRE on <date> · age N*, or *not reachable*.

**How it is calculated.** In monthly steps. Until the FIRE date the contribution grows by **Contribution growth % per year** (by inflation when blank, so it keeps its purchasing power), and the money grows at the scenario's rate. After the FIRE date the spend grows with inflation and what remains keeps growing at the scenario's rate. The horizon is the birth date plus life expectancy (95 by default), or 50 years after the FIRE date without a birth date. *Money will never end* means the money is not used up in 200 years and is worth at least as much in today's money at the end as at the FIRE date. **Spend now** uses the money the goal has today at the latest prices, not the planned spend, and the scenario's rates from the latest refresh: when markets fall, the money and the spend now fall with them, so the money is kept for the recovery; when they rise, it rises.

**Defaults and sources.** Without an override or fetched rates the app uses growth 7 % (optimistic 10 %, pessimistic 4 %), inflation 3 % (2 %, 5 %) and a withdrawal rate of 4 %. They come from the UBS Global Investment Returns Yearbook 2026 (world, 1900–2024: equities 9.8 % a year nominal, bonds 4.6 %, inflation 2.9 %; 7 % is about a 60/40 mix), the 2 % inflation targets of central banks, and Bengen (1994) and the Trinity study (1998) for the 4 % rule. See also [FIRE]({{ page.base }}/manual#fire) in the manual.

**Live rates.** **Refresh** fetches inflation and market growth for the currency of every FIRE goal, from the providers chosen in [Settings]({{ page.base }}/screens/settings). The order is: your **Advanced assumptions** override, then the fetched value, then the default. A failed fetch keeps the previous series and shows the error. Both growth sources are price indexes without dividends, so they understate the total return.

**Messages.**

| Message | What to do |
|---|---|
| *Enter a target, a monthly spend, or both a FIRE date and a contribution* | Fill in one of the three; without them nothing can be derived. |
| *Enter a FIRE date or a contribution* | The target is known but neither the date nor the contribution is; give one of them. |
| *The birth date is in the future* | Correct the birth date. |
| *The withdrawal rate must be above zero* | Enter a withdrawal rate above 0 in **Advanced assumptions**, or clear it to use the default. |
| *Not reachable before <date>* | With this contribution and these rates the target is not reached before that date (the end of the horizon); raise the contribution, lower the target or spend, or choose another scenario. |
| *No rate data for <currency>* | The inflation or growth provider has no series for this currency; choose another provider or enter the rates in **Advanced assumptions**. |
| *ECB does not cover <currency>* | The ECB provides euro-area inflation only; choose another **Inflation** provider in Settings. |
| *OECD does not cover <currency>* | The OECD has no series for this currency (for example RUB); choose another **Inflation** provider in Settings. |

**Funding sources.** The buckets that may fund this goal. **Connect bucket** adds one with a contribution limit:

- **Auto — up to remaining need**: the bucket gives whatever the goal still needs, after earlier goals took their share.
- **Fixed amount in goal currency**.
- **% of bucket**: at most that share of the bucket's value.
- **% of goal**: at most that share of the target.

The preview in the editor shows what the connection would contribute today. Limits are ceilings: goal order, available savings and other connections can reduce the contribution. Each listed source shows what it gives now and why it gives no more; **Edit connection** changes the limit, **Disconnect** removes it.

**Planned savings.** The plans that reach this goal, each with the amount the projection assigns to it. A plan that is used up by earlier goals does not appear here.

**Reading the projection.** "Planned savings close this goal on 20 Dec 2026 · on time" means the cumulative plans up to that date cover the remaining need before the due date. "Cover up to … · short by …" means they do not; add a plan, move the date, or lower the target.
