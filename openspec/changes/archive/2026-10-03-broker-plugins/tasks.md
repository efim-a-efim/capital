## 1. Framework

- [x] 1.1 `BrokerPlugin`, `BrokerHost`, `Reading`, `Credential`, `Response`, `Brokers` registry.
- [x] 1.2 Interactive Brokers, OANDA, Trading 212 and SnapTrade ported as plugins; `Providers` reduced to host and result checks.
- [x] 1.3 `Model`, view model, editor, Credentials section and attribution derive from the registry.

## 2. Tests and docs

- [x] 2.1 `BrokersTest` (registry invariants, id normalisation, defaults); existing `ProviderTest` passes unchanged in behaviour.
- [x] 2.2 `BROKER-PLUGINS.md`, README link, one sentence on the site's accounts page (15 languages), store notes.
- [x] 2.3 Strings in 15 languages (`I18nTest`).

## 3. Verification and release

- [x] 3.1 Unit tests, instrumentation test, manual check of the account editor per broker on the emulator.
- [x] 3.2 Released as v2.9.0 (v2.8.0 was tagged by mistake on the previous commit and never built).
