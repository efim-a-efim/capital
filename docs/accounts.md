---
layout: default
lang: en
base: ""
key: "accounts"
title: Broker and forex accounts
class: doc
---
# Broker and forex accounts

Capital can read the total value of a brokerage or forex account the same way it reads a crypto wallet. You add the account to a bucket as a holding of type **Broker account**, and every refresh fetches the account's net asset value in the account's base currency. The app only reads: it uses the broker's reporting interface with a token you create yourself, it never places, changes or cancels an order, and it never moves money.

Capital connects only to interfaces whose credentials are long-lived: a token or key you create once and that stays valid until you revoke it (or, for Interactive Brokers, until the expiry you chose, up to a year). Supported today:

| Broker | Interface used | What is read |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (report retrieval only) | Net asset value on the last business day, in the account's base currency |
| [OANDA](#oanda) | v20 REST API, fxTrade live accounts | Net asset value at the time of the refresh, in the account's currency |
| [Trading 212](#trading-212) | Public API, Invest and Stocks ISA accounts | Total account value at the time of the refresh, in the account's primary currency |
| [SnapTrade](#snaptrade) | SnapTrade Personal, an aggregator covering many brokers | Total account value as the broker reports it to SnapTrade, in the account's currency |

## Before you start {#before-you-start}

- **What leaves the device.** On every refresh the app sends your access token and the account or query id to that broker, over HTTPS. The broker sees your IP address, as with any request.
- **Where the credentials are kept.** Settings → Broker accounts. They are encrypted with a key held in Android Keystore, are never written into your data folder, and are left out of exports and system backups. One set of credentials per broker covers every account you add for that broker.
- **What is stored in your folder.** The account id, the last value read and when it was read. Nothing else from the broker.
- **The broker's screens may change.** The steps below match the brokers' web sites as of October 2026. Brokers rename menus and move settings from time to time, so a step may look a little different when you follow it. The broker's own documentation, linked in each section, is the authoritative source: if a step here no longer matches, look for the same term on the broker's page.

## Interactive Brokers {#interactive-brokers}

Capital uses the **Flex Web Service**, Interactive Brokers' interface for fetching pre-configured reports. The token it uses can only generate and download reports; it cannot log in, trade or withdraw. Capital asks for the **Net Asset Value (NAV) Summary in Base** of an Activity Flex Query and takes the total of the latest report date, so the value is the close of the last business day.

### 1. Create the Flex Query

1. Log in to [Client Portal](https://www.interactivebrokers.com/portal) and open **Performance & Reports → Flex Queries** (on some accounts the menu is called *Reporting*).
2. Under **Activity Flex Query** press **+** (Create). Give the query a name, for example `Capital`.
3. In the **Sections** list enable exactly these two sections and fields (selecting all fields of a section works as well):
   - **Account Information**: *Account ID*, *Currency*.
   - **Net Asset Value (NAV) Summary in Base**: *Report Date*, *Total*.
4. In **Delivery Configuration** set **Format** to `XML` and **Period** to `Last Business Day`. The other options can keep their defaults.
5. Save the query, then press the **i** (information) icon next to it and note the **Query ID**, a number.

The query must cover one account. If you have linked accounts or an advisor structure, create one query per account and select only that account when you create it.

### 2. Enable the Flex Web Service and create the token

1. On the same **Flex Queries** page open **Flex Web Service Configuration**.
2. Switch the **Flex Web Service Status** on and save. A token is created.
3. To choose how long the token stays valid, press **Generate New Token**: anywhere from 6 hours to 1 year. Leave **Valid for IP address** empty for a phone, whose address changes. Generating a new token invalidates the previous one.
4. Copy the token.

### 3. Connect it in Capital

1. **Settings → Broker accounts → Access token: Interactive Brokers**, paste the token and save.
2. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, **Broker** to Interactive Brokers, enter the **Flex Query id** and save.
3. Press **Refresh**. The first run takes up to half a minute because the report is generated on request.

When the token expires the refresh reports *Token has expired; generate a new one in Client Portal*: generate a new token and paste it in Settings. Interactive Brokers allows one report request per second and ten per minute from one token, which a refresh never exceeds.

Interactive Brokers documentation: [Flex Web Service](https://www.interactivebrokers.com/docs/web-api/flex-web-service/introduction) · [Enable and create the access token](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/enable-and-create-access-token) · [Create a Flex Query](https://www.interactivebrokers.com/docs/web-api/flex-web-service/client-portal-configuration/create-a-flex-query) · [Activity Flex Query reference](https://www.ibkrguides.com/reportingreference/reportguide/activity%20flex%20query%20reference.htm) · [Net Asset Value (NAV) Summary in Base](https://www.ibkrguides.com/reportingreference/reportguide/net%20asset%20value%20%28nav%29%20summary%20in%20base.htm)

## OANDA {#oanda}

Capital calls the **account summary** of the OANDA v20 REST API and stores the account's NAV (balance plus unrealised profit or loss) in the account's currency. Only live **fxTrade** accounts are supported; practice accounts are not savings.

**An OANDA personal access token is not read-only.** It grants full API access to every sub-account of your login, including trading. Capital only ever calls the account summary, but anyone who obtains the token could trade with it. Treat it like a password: paste it only into Capital, and revoke it in the OANDA portal if you lose the phone.

### 1. Create the token

1. Log in to your OANDA fxTrade account management portal.
2. Open **My Services → Manage API Access** (on the older portal: *My Account → My Services → Manage API Access*).
3. Accept the API licence and press **Generate**. Copy the token; OANDA does not show it again. If you lose it, revoke it there and generate a new one.

### 2. Find the account id

The v20 account id has the form `001-001-1234567-001`, with hyphens. It is listed in the same portal next to each sub-account, and on the fxTrade platform under the account details.

### 3. Connect it in Capital

1. **Settings → Broker accounts → Access token: OANDA**, paste the token and save.
2. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, **Broker** to OANDA, enter the **OANDA account id** and save.
3. Press **Refresh**.

A margin account whose NAV is negative is reported as an error rather than counted as savings.

OANDA documentation: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital calls the **account summary** of the Trading 212 Public API and stores the total account value in the account's primary currency. The API covers **Invest** and **Stocks ISA** accounts; a key pair belongs to one account, and Capital keeps one key pair, so it reads one Trading 212 account.

### 1. Create the API key

1. In the Trading 212 app or web site open the menu (**☰**) → **Settings** → **API (Beta)** and accept the risk warning.
2. Press **Generate API key**. Give it a name, keep only the **Account data** permission (read), and choose *Unrestricted* IP access (a phone's address changes).
3. Submit. Copy both values: the **API Key** and the **API Secret Key**. The secret is shown once; if you lose it, delete the key and generate a new pair.

### 2. Connect it in Capital

1. **Settings → Broker accounts → API key: Trading 212** and **API secret: Trading 212**, paste each value.
2. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, **Broker** to Trading 212, enter the **Trading 212 account number** (the account id shown in the app, digits only) and save.
3. Press **Refresh**. Trading 212 allows one summary request every 5 seconds.

Trading 212 documentation: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) is an aggregator: you connect a brokerage account to SnapTrade once, and SnapTrade reads it for you. It covers many brokers that have no public API of their own. Capital uses **SnapTrade Personal**, the free plan for your own accounts, with your own client id and consumer key. Data on this plan is refreshed by SnapTrade about once a day.

What is sent: your client id and, as a signature, nothing of the consumer key itself (requests are signed with it). SnapTrade, not Capital, holds the connection to your broker; its terms and privacy policy apply to that connection.

### 1. Create the API key

1. Sign up at the [SnapTrade dashboard](https://dashboard.snaptrade.com/signup) and choose the **Personal** plan.
2. In the dashboard create an API key. Copy the **client id** and the **consumer key**; the consumer key is shown once.

### 2. Connect it in Capital

1. **Settings → Broker accounts → Client id: SnapTrade** and **Consumer key: SnapTrade**, paste each value.
2. Open the bucket, **Add holding**, set **Tracking** to **Broker account** and **Broker** to SnapTrade.
3. Press **Connect a brokerage through SnapTrade**. The SnapTrade Connection Portal opens in the browser; log in to your broker there (the link is valid for 5 minutes). Come back to Capital.
4. Press **Fetch accounts** and choose the account; its id fills the **SnapTrade account id** field. Save, then **Refresh**.

An account that SnapTrade has not finished syncing yet reports *SnapTrade has no total value for this account yet*; refresh again later.

SnapTrade documentation: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Other brokers {#other-brokers}

Capital connects only to interfaces that work from a phone over HTTPS with a token you can create yourself and that read without being able to trade. That rules out, for now:

- **MetaTrader 4 and 5** accounts. The investor password gives read-only access, but only inside the MetaTrader terminal; brokers publish no HTTPS interface for it.
- Brokers whose API needs a program running on a computer (for example the Interactive Brokers Client Portal Web API gateway; Capital uses the Flex Web Service instead) or an OAuth application registration.
- Brokers whose API issues only short-lived tokens through OAuth, for example Saxo Bank (access tokens last 20 minutes; the developer portal's 24-hour token covers the simulation environment only).
- Banks and brokers without a public API.

Many of these brokers are covered by [SnapTrade](#snaptrade). Otherwise enter the balance as a **Manual** holding and update the number when you check your statement. If your broker offers a simple token-based HTTPS endpoint that reads the account value, [open an issue]({{ site.repo }}/issues) with a link to its documentation.

## Messages and what to do {#messages}

| Message | What to do |
|---|---|
| *Interactive Brokers needs your access token in Settings → Broker accounts* / *OANDA needs your access token …* | Paste the token, key or client id in Settings → Broker accounts. |
| *Token has expired; generate a new one in Client Portal* | Generate a new Flex Web Service token and paste it. |
| *Token is invalid* | Copy the token again; a new token replaces the old one. |
| *Token is restricted to another IP address* | Generate the token without an IP restriction. |
| *Flex Query id is invalid* | Check the number; the query must be an Activity Flex Query of this login. |
| *Add the section Net Asset Value (NAV) Summary in Base with Report Date and Total to the Flex Query* | Edit the query and add the section and fields. |
| *Add the Currency field of Account Information to the Flex Query* | Edit the query and add the field. |
| *The query returned N accounts; make one Flex Query per account* | Create a query that covers one account. |
| *Statement is not ready yet; refresh again in a minute* | Interactive Brokers is still generating the report; refresh again. |
| *Access denied; check provider key or quota* | The OANDA token is wrong or revoked, or the Trading 212 key pair is wrong or lacks the Account data permission. |
| *SnapTrade has no total value for this account yet; sync the connection and retry* | SnapTrade has not synced the brokerage yet; refresh again later. |
| *No accounts connected yet. Connect a brokerage through SnapTrade first.* | Open the Connection Portal from the editor and connect a broker. |
| *Negative account value … is not supported* | The account is in debit; it adds nothing to your savings. |

The previous value stays visible after any of these, marked stale.
