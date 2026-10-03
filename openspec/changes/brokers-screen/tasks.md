## 1. Domain and storage

- [x] 1.1 `Account`, `Portfolio.accounts`, `Holding.accountId`, `linked()`, `ignored()`, `deleteAccount()`, validation and uniqueness; tests.
- [x] 1.2 Schema 6 with `withAccounts()` migration of schema-5 broker holdings; tests.

## 2. Providers

- [x] 2.1 `Providers.account(Account)`, account loop in `refresh` (bucket-scoped), `Observations.accounts`, merge and re-link; tests.

## 3. Interface

- [x] 3.1 Brokers tab and rail item, icon, FAB, Brokers screen with accounts, link state, Credentials section moved from Settings.
- [x] 3.2 Account editor (name, broker, id, SnapTrade picker, ignore-below threshold); holding editor links an existing account.
- [x] 3.3 Bucket rows read broker and id from the linked account.
- [x] 3.4 Strings in 15 languages (`I18nTest`).

## 4. Site and store

- [x] 4.1 New `screens/brokers` page, `accounts`, manual, bucket and settings screen pages, landing page, README and store notes; 15 languages, `site-check.py` extended with the new page.
- [x] 4.2 Screenshot `brokers.png` per language.

## 5. Verification

- [x] 5.1 Unit tests (`testDebugUnitTest`), instrumentation test on the emulator, manual flow on the emulator (add account, link, refresh error, delete).
- [ ] 5.2 Release v2.7.0 through the release workflow.
