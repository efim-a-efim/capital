## ADDED Requirements

### Requirement: Daily rate history
The app SHALL keep a daily USD price series per asset in use and SHALL fill missing days from the chosen history providers (Frankfurter for fiat, DefiLlama for coins and tokens) back to the earliest date the portfolio needs; every refresh SHALL append the day's quote.

#### Scenario: Gap filled
- **WHEN** EUR has no rate for the last 30 days and the user refreshes
- **THEN** the EUR series gets those days from Frankfurter and today's quote.

### Requirement: Background rate update
Settings SHALL offer Auto update rates with Off, 24 h, 12 h, 6 h, 3 h and 1 h. When on, a periodic job SHALL fetch the current quote of every asset in use with the chosen providers and SHALL store it in an app-private cache; the next open or refresh SHALL merge the cache into the quotes and the history. The job SHALL never write the data folder.

#### Scenario: Fresh totals on open
- **WHEN** the job ran at 03:00 and the user opens the app at 09:00 without a network
- **THEN** totals use the 03:00 quotes and the quotes list shows them as observed at 03:00.
