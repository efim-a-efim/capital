# Google Play listing

Copy-paste kit for Play Console. Assets in this folder; policy answers on the site.

| Console field | Value |
|---|---|
| App name (≤30) | `Capital – Savings Tracker` |
| Default language | English (United States) |
| Category | Finance |
| Tags | Personal finance, Budgeting, Savings |
| Free / paid | Free, no in-app purchases, no ads |
| Contact email | apps@fimych.dev |
| Website | https://capital.fimych.dev/ |
| Privacy policy URL | https://capital.fimych.dev/privacy |
| App icon | `icon-512.png` (512×512, 32-bit PNG) |
| Feature graphic | `feature-graphic.png` (1024×500) |
| Phone screenshots | `screenshots/phone-*.png` (1080×1920, 9:16), in file order |
| Tablet screenshots | Not provided; the phone set is accepted, tablet promotion is not claimed |
| Data safety | https://capital.fimych.dev/data-safety |
| Financial features | https://capital.fimych.dev/financial-features |
| Package | `dev.capital` |

## Short description (≤80)

```
Savings in buckets, goals with due dates, plans that show when each goal closes.
```

## Full description (≤4000)

```
Capital shows where your savings stand against what you are saving for.

BUCKETS
Put each place you keep money into a bucket: a savings account, cash, a brokerage, a crypto wallet. Type quantities in by hand, paste a public wallet address (BTC, ETH, TON, TRX) and let the app read the balance and tokens, or connect a brokerage account (Interactive Brokers, OANDA, Trading 212, or many others through SnapTrade) with a token and let the app read its value. Every bucket is valued in your default currency.

GOALS
Give each goal a target and a due date, then connect the buckets that may fund it, with limits if you like. Goals with earlier dates are funded first; drag goals that share a date to set their order. The app shows what each goal has, what it still needs, and marks it Funded, Funded in time or Not funded.

PLANS
Add the savings you intend to make – salary at the end of the month, a bonus in December. Capital projects them onto your goals and tells you the date each goal closes, or how much it falls short. Plans whose date has passed move to an archive and stop counting.

PORTFOLIO MODE
Treat a bucket as a portfolio: set target percentages per asset, see real share against target, and get a rebalance list for the amount you want to invest.

PRIVATE BY DESIGN
• No account, no analytics, no ads, no server. The developer never sees your data.
• Records are plain files in a folder you choose. Sync the folder with the tool you already use; conflicts are shown, not resolved behind your back.
• Optional encryption of the whole folder (AES-256-GCM, Argon2id) with PIN and biometric unlock.
• Wallets are read-only: the app never asks for a private key or seed phrase and cannot send anything.

DATA SOURCES YOU CHOOSE
Bitcoin: Blockstream or mempool.space. Ethereum: PublicNode or Alchemy, tokens from Blockscout or Ethplorer. TON: TON Center or TonAPI. TRON: TronGrid or PublicNode. Prices: DefiLlama, CoinGecko or CoinPaprika. Fiat rates: Frankfurter or the European Central Bank. Broker accounts: Interactive Brokers (Flex Web Service), OANDA (v20 API), Trading 212 and SnapTrade, read-only. Every source is selectable; keyless defaults work out of the box.

Tokens are identified by contract address, never by name, and only count when your price source lists that exact contract – so a fake "USDT" on your address is never counted.

LANGUAGES
English, Chinese, Hindi, Spanish, Arabic, French, Bengali, Portuguese, Russian, Indonesian, Urdu, German, Japanese, Marathi, Vietnamese.

Capital is a tracker and a calculator. It never moves money, gives no advice and holds no funds. Open source: https://github.com/efim-a-efim/capital
```

## Release notes (≤500, per release)

```
• Brokers tab: broker accounts have their own screen with their credentials; a bucket links an account with Add holding → Broker account
• Existing broker holdings are moved to the Brokers tab automatically
• Site: a Brokers screen page and updated setup guide, in 15 languages
```

## App content answers

Answers with reasoning are on the site so they stay next to the privacy policy:

- Data safety: https://capital.fimych.dev/data-safety
- Financial features: https://capital.fimych.dev/financial-features
- Ads: No. App access: all functionality available without login. Content rating: Everyone. Target audience: 18+. News / COVID / Government / Health: No.

## Upload

Play requires an Android App Bundle for new apps. Build it with the release key (`signing/`), then upload `app/build/outputs/bundle/release/app-release.aab`:

```sh
export CAPITAL_KEYSTORE=$PWD/signing/capital-release.jks CAPITAL_KEY_ALIAS=capital
export CAPITAL_KEYSTORE_PASSWORD=… CAPITAL_KEY_PASSWORD=…
./scripts/gradle bundleRelease
```

When Play App Signing asks for the app signing key, choose *Export and upload a key from a Java keystore* and upload the key from `signing/capital-release.jks`, so the Play build and the GitHub APK share one certificate and install over each other.

## After publishing

Set `play_url` in `docs/_config.yml` to the store link; the site then shows the Google Play button.
