## 1. Domain

- [x] 1.1 `Fire`, `Period`, `Goal.fire`; validation; `SCHEMA = 7`.
- [x] 1.2 `domain/Fire.kt`: solver for the three scenarios, drawdown, messages; `Portfolio.solvedFire()` in the edit/resolve/merge pipeline.
- [x] 1.3 `FireTest`: each derivation, each message, never-ends, beyond-life, closed-form contribution against simulation, schema 7 round trip.

## 2. Live rates

- [x] 2.1 `Rate`, `Portfolio.rates`, provider choices "Inflation" and "Market growth"; World Bank, ECB, OECD, Yahoo parsers with size and shape checks; refresh with quotes.
- [x] 2.2 `ProviderTest` with recorded responses; `scripts/check-providers.py` reachability.

## 3. UI

- [x] 3.1 Goal editor: type choice, FIRE fields, Advanced section with defaults.
- [x] 3.2 Goal screen: scenario table, lasts-until line, Canvas burndown; goals list projection line.
- [x] 3.3 Instrumentation check on the emulator.

## 4. Documentation

- [x] 4.1 English: `docs/screens/goal.md` FIRE section, `docs/manual.md` (FIRE, defaults and sources), `docs/screens/goals.md`, index feature text, README, Financial features note, store notes, Privacy Policy operators (World Bank, ECB, OECD, Yahoo), Settings screen page.
- [x] 4.2 App strings and site in 14 languages (`I18nTest`, `site-check.py`), goal screenshots per language.

## 5. Release

- [x] 5.1 Full tests, emulator run, minor release.
