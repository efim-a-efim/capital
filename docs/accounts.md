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

Capital connects only to interfaces whose credentials are long-lived: a token or key you create once and that stays valid until you revoke it, until an expiry you chose, or for at least several months (T-Invest tokens lapse after three months without use, ALOR tokens after a year). Every broker below is available whatever language the app is set to. Supported today:

| Broker | Interface used | What is read |
|---|---|---|
| [Interactive Brokers](#interactive-brokers) | Flex Web Service (report retrieval only) | Net asset value on the last business day, in the account's base currency |
| [OANDA](#oanda) | v20 REST API, fxTrade live accounts | Net asset value at the time of the refresh, in the account's currency |
| [Trading 212](#trading-212) | Public API, Invest and Stocks ISA accounts | Total account value at the time of the refresh, in the account's primary currency |
| [SnapTrade](#snaptrade) | SnapTrade Personal, an aggregator covering many brokers | Total account value as the broker reports it to SnapTrade, in the account's currency |
| [Alpaca](#alpaca) | Trading API, live accounts | Equity (cash plus positions), in US dollars |
| [Tradier](#tradier) | Brokerage API | Total equity, in US dollars |
| [tastytrade](#tastytrade) | Open API with a personal OAuth grant | Net liquidating value, in US dollars |
| [Public.com](#public) | Individual API | Total account value, in US dollars |
| [eToro](#etoro) | Public API | Balance of the chosen account (for a trading account: cash plus invested positions), in its currency |
| [Indexa Capital](#indexa-capital) | REST API, read-only token | Portfolio total on the last valuation date, in the account's currency |
| [T-Invest](#t-invest) | T-Invest API (T-Bank) | Total portfolio value, in roubles |
| [ALOR](#alor) | ALOR OpenAPI | Portfolio valuation on the Moscow Exchange, in roubles |
| [Capital.com](#capital-com) | Public API, live accounts | Balance including open profit and loss, in the account's currency |
| [Akahu](#akahu) | Akahu personal app, a New Zealand aggregator | Balance of a connected account (Sharesies, Hatch, Kernel, KiwiSaver and others), in its currency |

## Before you start {#before-you-start}

- **What leaves the device.** On every refresh the app sends your access token and the account or query id to that broker, over HTTPS. The broker sees your IP address, as with any request.
- **Where the credentials are kept.** Brokers tab → Credentials. They are encrypted with a key held in Android Keystore, are never written into your data folder, and are left out of exports and system backups. One set of credentials per broker covers every account you add for that broker.
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

1. **Brokers → Credentials → Access token: Interactive Brokers**, paste the token and save.
2. **Brokers → +**: enter a name, set **Broker** to Interactive Brokers, enter the **Flex Query id** and save.
3. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, choose the account and save.
4. Press **Refresh**. The first run takes up to half a minute because the report is generated on request.

When the token expires the refresh reports *Token has expired; generate a new one in Client Portal*: generate a new token and paste it under Credentials. Interactive Brokers allows one report request per second and ten per minute from one token, which a refresh never exceeds.

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

1. **Brokers → Credentials → Access token: OANDA**, paste the token and save.
2. **Brokers → +**: enter a name, set **Broker** to OANDA, enter the **OANDA account id** and save.
3. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, choose the account and save.
4. Press **Refresh**.

A margin account whose NAV is negative is reported as an error rather than counted as savings.

OANDA documentation: [v20 REST API](https://developer.oanda.com/rest-live-v20/introduction/) · [Authentication and personal access tokens](https://developer.oanda.com/rest-live-v20/authentication/) · [Account endpoints](https://developer.oanda.com/rest-live-v20/account-ep/)

## Trading 212 {#trading-212}

Capital calls the **account summary** of the Trading 212 Public API and stores the total account value in the account's primary currency. The API covers **Invest** and **Stocks ISA** accounts; a key pair belongs to one account, and Capital keeps one key pair, so it reads one Trading 212 account.

### 1. Create the API key

1. In the Trading 212 app or web site open the menu (**☰**) → **Settings** → **API (Beta)** and accept the risk warning.
2. Press **Generate API key**. Give it a name, keep only the **Account data** permission (read), and choose *Unrestricted* IP access (a phone's address changes).
3. Submit. Copy both values: the **API Key** and the **API Secret Key**. The secret is shown once; if you lose it, delete the key and generate a new pair.

### 2. Connect it in Capital

1. **Brokers → Credentials → API key: Trading 212** and **API secret: Trading 212**, paste each value.
2. **Brokers → +**: enter a name, set **Broker** to Trading 212, enter the **Trading 212 account number** (the account id shown in the app, digits only) and save.
3. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, choose the account and save.
4. Press **Refresh**. Trading 212 allows one summary request every 5 seconds.

Trading 212 documentation: [Public API](https://docs.trading212.com/api) · [How to get your API key](https://helpcentre.trading212.com/hc/en-us/articles/14584770928157-Trading-212-API-key)

## SnapTrade {#snaptrade}

[SnapTrade](https://snaptrade.com) is an aggregator: you connect a brokerage account to SnapTrade once, and SnapTrade reads it for you. It covers many brokers that have no public API of their own. Capital uses **SnapTrade Personal**, the free plan for your own accounts, with your own client id and consumer key. Data on this plan is refreshed by SnapTrade about once a day.

What is sent: your client id and, as a signature, nothing of the consumer key itself (requests are signed with it). SnapTrade, not Capital, holds the connection to your broker; its terms and privacy policy apply to that connection.

### 1. Create the API key

1. Sign up at the [SnapTrade dashboard](https://dashboard.snaptrade.com/signup) and choose the **Personal** plan.
2. In the dashboard create an API key. Copy the **client id** and the **consumer key**; the consumer key is shown once.

### 2. Connect it in Capital

1. **Brokers → Credentials → Client id: SnapTrade** and **Consumer key: SnapTrade**, paste each value.
2. **Brokers → +**: enter a name and set **Broker** to SnapTrade.
3. Press **Connect a brokerage through SnapTrade**. The SnapTrade Connection Portal opens in the browser; log in to your broker there (the link is valid for 5 minutes). Come back to Capital.
4. Press **Fetch accounts** and choose the account; its id fills the **SnapTrade account id** field. Save.
5. Open the bucket, **Add holding**, set **Tracking** to **Broker account**, choose the account and save, then **Refresh**.

An account that SnapTrade has not finished syncing yet reports *SnapTrade has no total value for this account yet*; refresh again later.

SnapTrade documentation: [Getting started](https://docs.snaptrade.com/docs/getting-started) · [Personal vs Commercial](https://docs.snaptrade.com/docs/personal-vs-commercial) · [Supported brokerages](https://snaptrade.com/brokerage-integrations) · [Pricing](https://snaptrade.com/pricing)

## Connecting an account in Capital {#connect}

The sections below say how to create the credential at each broker. In Capital the steps are the same for all of them:

1. **Brokers → Credentials**: press the broker's credential buttons and paste each value.
2. **Brokers → +**: enter a name, choose the **Broker**, then press **Fetch accounts** and pick the account (or type its id) and save.
3. Open a bucket, **Add holding**, set **Tracking** to **Broker account**, choose the account and save. Press **Refresh**.

## Alpaca {#alpaca}

Alpaca issues a key id and a secret per account; they stay valid until you regenerate them. Only live accounts are read: keys of a paper account do not work against the live API.

1. Log in to the [Alpaca dashboard](https://app.alpaca.markets), switch to your live account and, on the home page, under **API Keys**, press **Generate New Keys**.
2. Copy the **API Key ID** and the **Secret Key**; the secret is shown once.
3. In Capital paste them as **API key: Alpaca** and **API secret: Alpaca**, then follow [Connecting an account](#connect). **Fetch accounts** shows the account number of the key.

Alpaca documentation: [Authentication](https://docs.alpaca.markets/docs/authentication) · [Get account](https://docs.alpaca.markets/reference/getaccount-1)

## Tradier {#tradier}

The API token from your Tradier settings never expires.

1. Log in to Tradier and open [Settings → API Access](https://web.tradier.com/user/api). Copy the **API Access Token** of your brokerage account (not the sandbox token).
2. In Capital paste it as **Access token: Tradier**, then follow [Connecting an account](#connect).

Tradier documentation: [Authentication](https://docs.tradier.com/docs/authentication) · [Get balances](https://docs.tradier.com/reference/brokerage-api-accounts-get-account-balance)

## tastytrade {#tastytrade}

tastytrade uses a personal OAuth grant: you create an application for yourself and a grant whose refresh token never expires. Capital trades it for a 15-minute access token on each refresh.

1. On [my.tastytrade.com](https://my.tastytrade.com) open **Manage → My Profile → API → OAuth Applications** and press **+ New OAuth client**. Give it a name, any HTTPS redirect URI (for example `https://capital.fimych.dev`) and only the **read** scope. Save and copy the **Client Secret**; it is shown once.
2. Press **Manage** next to the application, then **Create Grant**, and copy the **refresh token**.
3. In Capital paste them as **Refresh token: tastytrade** and **Client secret: tastytrade**, then follow [Connecting an account](#connect).

tastytrade documentation: [OAuth2 and personal grants](https://developer.tastytrade.com/docs/authentication/oauth2) · [Balances](https://developer.tastytrade.com/reference/balances-and-positions/getAccountsAccountNumberBalances)

## Public.com {#public}

Public's Individual API is meant for your own accounts. The secret key is long-lived and revocable; Capital trades it for a five-minute access token on each refresh.

1. In Public's web app open the **API** page of your settings and generate a **secret key**.
2. In Capital paste it as **Secret key: Public.com**, then follow [Connecting an account](#connect).

Public documentation: [Quickstart](https://public.com/api/docs/quickstart) · [Access tokens](https://public.com/api/docs/resources/authorization/create-personal-access-token) · [Portfolio](https://public.com/api/docs/resources/account-details/get-account-portfolio-v2)

## eToro {#etoro}

eToro keys are long-lived; you can give them an expiry date and an IP list, and you can make them read-only. Your eToro account must be verified.

1. In eToro open **Settings → Trading → API Key Management** and press **Create New Key**. Choose the **Real** environment, the **Read** permission, no IP list, and optionally an expiration date. Confirm with the SMS code.
2. Copy the **Public API Key** and the **User Key**; the user key is shown once.
3. In Capital paste them as **Public API key: eToro** and **User key: eToro**, then follow [Connecting an account](#connect). **Fetch accounts** lists your trading, cash and other eToro accounts.

eToro documentation: [Authentication](https://api-portal.etoro.com/core/getting-started/authentication) · [Balances](https://api-portal.etoro.com/api-reference/balances/get-aggregated-balances) · [Getting started](https://builders.etoro.com/get-started)

## Indexa Capital {#indexa-capital}

The token from Indexa's private area is read-only. It is tied to your e-mail, password and device: after a password change, generate it again.

1. In Indexa's private area open **User settings → Applications** and copy the token.
2. In Capital paste it as **Access token: Indexa Capital**, then follow [Connecting an account](#connect). Pension and investment accounts are both listed.

Indexa values funds once per business day; the observation date is that valuation date.

Indexa Capital documentation: [REST API](https://indexacapital.com/en/api-rest-v1) · [Connecting with the API](https://support.indexacapital.com/es/esp/api-conectar)

## T-Invest {#t-invest}

T-Bank's T-Invest API accepts a token you issue in the investment settings. A token lapses three months after its last use and must be used within seven days of issue; a weekly refresh keeps it alive. Choose a **read-only** token.

1. Open the [T-Invest settings](https://www.tbank.ru/invest/settings/) and issue a **T-Invest API token** for the exchange with **read-only** access (all accounts or one). Confirmation of trades by code must be off to issue it. Copy the token; it is shown once.
2. In Capital paste it as **Access token: T-Invest**, then follow [Connecting an account](#connect).

T-Bank serves this API under the Russian Trusted Root CA, which Android does not include. Capital trusts that certificate for the T-Invest API address only (`invest-public-api.tbank.ru`), and no other connection.

T-Invest documentation: [Tokens](https://developer.tbank.ru/invest/intro/intro/token) · [GetPortfolio](https://developer.tbank.ru/invest/api/operations-service-get-portfolio)

## ALOR {#alor}

ALOR issues a refresh token valid for one year; Capital trades it for a 30-minute access token on each refresh. ALOR offers no read-only token: the token could trade, Capital only reads.

1. Sign in to the [ALOR developer portal](https://alor.dev), bind your trading account, open **API Access Tokens** and press **Create Token**. Copy the refresh token.
2. In Capital paste it as **Refresh token: ALOR**, then follow [Connecting an account](#connect). **Fetch accounts** lists the portfolios of the account (stock market D…, currency market G…, derivatives 7500…); add one per portfolio.

ALOR documentation: [Refresh token](https://alor.dev/docs/en/api/access/authorization/refresh-token) · [Access token](https://alor.dev/docs/en/api/access/authorization/access-token)

## Capital.com {#capital-com}

Capital.com keys are valid for one year by default, or until the date you choose. They carry trading rights (Capital.com has no read-only keys); Capital only reads. A key has its own password, which is not your account password.

1. Turn on two-factor authentication, then open **Settings → API integrations** and press **Generate API key**. Give it a label and a **custom password**, keep or set the expiry, and confirm with the 2FA code. Copy the key; it is shown once.
2. In Capital paste **API key: Capital.com**, your login e-mail as **Login e-mail: Capital.com** and the custom password as **API key password: Capital.com**, then follow [Connecting an account](#connect). Only live accounts are read.

Capital.com documentation: [Public API](https://open-api.capital.com/)

## Akahu {#akahu}

[Akahu](https://www.akahu.nz) connects New Zealand banks, investment platforms and KiwiSaver schemes; a free personal app reads your own accounts. Akahu refreshes the data about once a day.

1. Sign up at [my.akahu.nz](https://my.akahu.nz) and connect your providers (for example Sharesies, Hatch, Kernel, Simplicity, Milford or your KiwiSaver scheme).
2. Open the **Developers** page, accept the developer terms and copy the **App ID Token** and the **User Access Token**.
3. In Capital paste them as **App ID token: Akahu** and **User access token: Akahu**, then follow [Connecting an account](#connect).

Akahu documentation: [Personal apps](https://developers.akahu.nz/docs/personal-apps) · [Accounts](https://developers.akahu.nz/reference/get_accounts) · [Supported providers](https://developers.akahu.nz/docs/integrations)

## Popular brokers by market {#by-market}

How the most used brokers in the markets of Capital's languages can be connected, as of October 2026. *Direct* means a section above; *SnapTrade* means through [SnapTrade](#snaptrade); otherwise the reason it cannot be read, and the balance can be kept as a **Manual** holding.

| Market | Broker | How |
|---|---|---|
| United States | Interactive Brokers, Alpaca, Tradier, tastytrade, Public.com | Direct |
| United States | Fidelity, Charles Schwab, Vanguard, Robinhood, E\*TRADE, Webull, TradeStation, Empower, Wells Fargo, Chase | SnapTrade |
| United States | Merrill, SoFi, Firstrade, Betterment, Wealthfront, Acorns, M1 | No public API |
| Canada | Questrade, Wealthsimple, TD Direct Investing, BMO InvestorLine, CIBC Investor's Edge, Webull Canada | SnapTrade |
| Canada | RBC Direct Investing, Scotia iTRADE, National Bank Direct Brokerage | No public API |
| United Kingdom and Ireland | Trading 212, eToro, Interactive Brokers | Direct |
| United Kingdom and Ireland | AJ Bell | SnapTrade |
| United Kingdom and Ireland | Hargreaves Lansdown, Interactive Investor, Freetrade, Vanguard UK, Nutmeg, Moneybox | No public API |
| United Kingdom and Ireland | IG | Not possible: every session needs the account password |
| Europe | Indexa Capital (Spain), eToro, Trading 212, Interactive Brokers | Direct |
| Europe | DEGIRO, BUX | SnapTrade |
| Europe | Trade Republic, Scalable Capital, MyInvestor, Bourse Direct, Boursorama, flatex, ING, Revolut | No public API for investments |
| Europe | XTB | Not possible: the API was closed in March 2025 |
| Europe | Saxo, comdirect | Not possible: only short-lived tokens or TAN sessions |
| Europe | Bitpanda, Freedom24 | Not possible: the API returns no total account value |
| Russia and Kazakhstan | T-Invest, ALOR | Direct |
| Russia and Kazakhstan | BCS | Not possible: no total value, and its token expires after 90 days |
| Russia and Kazakhstan | Finam | Not yet: the currency of the account value is not documented |
| Russia and Kazakhstan | Sber, VTB, Alfa-Investments, Halyk Finance, Freedom Broker | No public API, or no total value in it |
| India | Zerodha, Upstox | SnapTrade (SEBI rules end API sessions daily, so the connection needs frequent renewal) |
| India | Groww, Angel One, ICICI Direct, Dhan, Kotak Neo, HDFC Securities, 5paisa | Not possible: SEBI rules end every API session daily |
| Pakistan and Bangladesh | All exchange brokers | No public API |
| China, Hong Kong and Taiwan | moomoo | SnapTrade |
| China, Hong Kong and Taiwan | Futu, Tiger Brokers, Longbridge | Not yet: key lifetime or response format not fully documented, or keys cannot be limited to reading |
| China, Hong Kong and Taiwan | East Money, Huatai, CITIC, Yuanta, Fubon | No public web API (desktop terminals or certificate SDKs only) |
| Japan | OANDA Japan (accounts that qualify for API access) | Direct, as OANDA |
| Japan | SBI Securities, Rakuten Securities, Monex, Matsui | No public API |
| Australia and New Zealand | CommSec, Stake | SnapTrade |
| Australia and New Zealand | Sharesies, Hatch, Kernel, Simplicity, KiwiSaver schemes | Akahu (New Zealand accounts) |
| Middle East and Africa | eToro | Direct |
| Middle East and Africa | Al Rajhi Capital, SNB Capital, Derayah, EFG Hermes, Thndr, Sarwa, Baraka, EasyEquities | No public API for individuals |
| Southeast Asia | Stockbit, Ajaib, Bibit, IPOT, VPS | No public API |
| Southeast Asia | SSI, TCBS, DNSE | Not possible: 8-hour tokens with a one-time code, or cash balance only |
| Latin America | XP, Nubank, Inter, BTG Pactual, Itaú, GBM, InvertirOnline, Fintual | No public API for individuals, or password-only logins |
| Forex and CFD | OANDA, Capital.com | Direct |
| Forex and CFD | MetaTrader brokers (XM, Exness, Pepperstone, IC Markets, Admirals) | Not possible: no HTTPS read access |
| Forex and CFD | cTrader brokers, FXCM, Forex.com | Not possible: app registration, deprecated API or password logins |

## Other brokers {#other-brokers}

Capital connects only to interfaces that work from a phone over HTTPS with a token you can create yourself and that read without being able to trade. That rules out, for now:

- **MetaTrader 4 and 5** accounts. The investor password gives read-only access, but only inside the MetaTrader terminal; brokers publish no HTTPS interface for it.
- Brokers whose API needs a program running on a computer (for example the Interactive Brokers Client Portal Web API gateway; Capital uses the Flex Web Service instead) or an OAuth application registration.
- Brokers whose API issues only short-lived tokens through OAuth, for example Saxo Bank (access tokens last 20 minutes; the developer portal's 24-hour token covers the simulation environment only).
- Banks and brokers without a public API.

Many of these brokers are covered by [SnapTrade](#snaptrade). Otherwise enter the balance as a **Manual** holding and update the number when you check your statement. If your broker offers a simple token-based HTTPS endpoint that reads the account value, [open an issue]({{ site.repo }}/issues) with a link to its documentation. Every broker in Capital is a small plugin; developers can add one by following [the plugin guide]({{ site.repo }}/blob/main/BROKER-PLUGINS.md).

## Messages and what to do {#messages}

| Message | What to do |
|---|---|
| *Interactive Brokers needs its credentials on the Brokers screen* / *OANDA needs its credentials …* | Paste the token, key or client id under Credentials on the Brokers tab. |
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
| *tastytrade rejected the refresh token or client secret; create a new grant* | Create a new grant for the application and paste its refresh token; check the client secret. |
| *Capital.com did not open a session; check the API key, login and key password* | The key, the e-mail or the key's custom password is wrong, or the key has expired. |
| *Account not found; choose it again* | The broker no longer lists this account; edit it and pick it from **Fetch accounts**. |

The previous value stays visible after any of these, marked stale.
