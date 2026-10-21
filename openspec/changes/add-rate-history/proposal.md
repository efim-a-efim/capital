## Why

Totals use one cached quote per asset, so nothing can be valued at a past date and the quotes age while the app is closed. A daily rate history, kept fresh by a background job, gives past values for the PnL feature and fresher totals on open.

## What Changes

- **Rate history** in the data file: one daily USD price series per asset in use (`Portfolio.history`, key `rate:<ASSET>`). Filled from the providers' history endpoints (Frankfurter time series for fiat, DefiLlama `chart` for coins and tokens; new Settings category **Rate history**: Frankfurter + DefiLlama / Off) back to the earliest date the portfolio needs, and extended by every refresh.
- **Auto update** in Settings: **Auto update rates** Off / 24 h / 12 h / 6 h / 3 h / 1 h. A periodic platform job (`JobScheduler`, network required) fetches the current quote of every asset in use with the chosen providers, into an app-private cache; the next open or refresh merges the cache into the history and into the cached quotes, so totals show the newest rates.
- Schema 8; docs (Settings, manual, Privacy Policy data flows), 15 languages, settings screenshot.

## Non-goals

- Writing the data folder from the background job (SAF, encryption and sync conflicts stay in the foreground).
- Balances and broker values in the background: rates only.

## Capabilities

### New Capabilities
- `rate-history`: daily rate series and the background update.

### Modified Capabilities
- `external-data`: history providers; `local-storage`: schema 8.
