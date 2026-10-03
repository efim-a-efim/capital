## 1. Domain and storage

- [x] 1.1 `Holding.broker`, `brokerChoices`, `accountId(broker, raw)` validation, uniqueness, no tokens on accounts; tests.
- [x] 1.2 Schema 5; schema-4 snapshot opens; test.

## 2. Providers

- [x] 2.1 Interactive Brokers Flex Web Service adapter: SendRequest, GetStatement polling, error codes, NAV parsing, single-statement rule; tests with captured XML.
- [x] 2.2 OANDA account summary adapter; tests.
- [x] 2.2b Trading 212 summary, SnapTrade signed requests (account, account list, login link); tests. Saxo evaluated and dropped (short-lived tokens).
- [x] 2.3 Refresh integration and safe merge for account holdings (asset follows the broker's currency); tests.

## 3. Interface

- [x] 3.1 Holding editor: tracking type Account, broker choice, id field, guide link, duplicate message.
- [x] 3.2 Account rows in the bucket screen; Settings "Broker accounts" tokens, note and guide link; attribution links; disclosure texts.

## 4. Documentation (15 languages)

- [x] 4.1 English: `docs/accounts.md`, manual, screens/bucket, screens/settings, privacy, data-safety, financial-features, index, README, store listing, provider smoke script.
- [x] 4.2 App strings and site pages in the other 14 languages; `I18nTest` and site structure checks pass.

## 5. Acceptance

- [x] 5.1 Unit tests and debug build pass; emulator run of the editor, settings and rows.
- [ ] 5.2 Settings screenshots recaptured per language (not done: the new section is below the fold; see acceptance.md).
