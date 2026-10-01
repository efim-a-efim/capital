---
layout: default
lang: en
base: ""
key: "financial-features"
title: Financial features declaration
class: doc
---
# Financial features declaration

<p class="meta">Answers for the Google Play Console form (Policy and programs → App content → Financial features), with the reasoning. Reviewed against version 2.2.1 on 30 September 2026.</p>

## Form answer

**Select all of the financial features the app provides:** **The app does not provide any financial features.**

## Why

Capital is a personal savings tracker. It records what the user already owns and shows how those savings map onto the user's own goals. Against each feature in the form:

| Feature in the form | Capital |
|---|---|
| Personal loan direct lender, loan facilitator, payday loans, line of credit, earned wage advances, microfinance, buy now pay later | No lending of any kind |
| Banking | No accounts, deposits or account access. Bank balances are typed in by the user |
| Mobile payments and digital wallets, money transfer and wire services | Cannot send, receive or hold money. Goal allocation is a calculation shown on screen; it moves nothing |
| Cryptocurrency wallet | Reads the balance of public addresses the user pastes. It never holds private keys or seed phrases and cannot sign or broadcast transactions, so it is not a wallet |
| Cryptocurrency exchange | No trading, no order routing, no fiat on-ramp |
| Rewards and incentives, crowdfunding and chit funds, prediction markets | None |
| Credit monitoring and reporting | None |
| Financial advice | None. The projection reports arithmetic on the user's own numbers ("planned savings close this goal on …"); it recommends no product, asset or action. The rebalance calculator lists purchases needed to match percentages the user set themselves |
| Insurance | None |
| In-app purchases, donations | None processed by the app. A tip screen shows the developer's public wallet addresses (the same ones as on this site); a transfer happens in the user's own wallet app, unlocks nothing and is invisible to the app |

The app also provides no in-app purchases and no paid features.

## If the reviewer disagrees

If Play review classifies the app as offering a financial feature anyway, the closest option is **Other** with this description:

> Read-only personal savings tracker. Users type in their balances or paste public blockchain addresses; the app fetches balances and market prices from third-party data sources and shows how the savings cover the user's own goals. No custody, no keys, no transactions, no lending, no trading, no advice.

Country-specific requirements for personal loan apps, and the United States cryptocurrency questions, do not apply because none of those features is selected.

## Related facts a reviewer may ask about

- Market data comes from third-party operators selected by the user (see the [Privacy Policy]({{ page.base }}/privacy)). The app shows the operator's name and site in Settings.
- Wallet queries use public, read-only blockchain APIs.
- The app runs entirely on the device and has no developer-operated server.
