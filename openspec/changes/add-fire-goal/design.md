## Design

### Model
- `Goal.fire: Fire? = null`. `Fire(birth: String?, fireDate: String?, contribution: String?, period: Period = MONTH, days: Int? = null, spend: String?, target: String?, inflation/growth × 3, withdrawal, life: Int, …)`; every rate nullable = default. `Period { WEEK, TWO_WEEKS, MONTH, YEAR, DAYS }`.
- `Fire.target == null` means the goal's `target` was derived; `Fire.fireDate == null` means `due` was derived. `Portfolio.solvedFire()` recomputes derived `target`/`due` (average scenario) and runs in the same pipeline as `linked()` (edit, resolve, merge). Starting capital for the derivation is the goal's current allocation; the result feeds the next allocation (fixed point reached on the next edit; `ponytail:` comment).
- `SCHEMA = 7`; no migration (`json` rejects unknown keys, so older apps refuse the file cleanly as a future schema).

### Solver — `domain/Fire.kt`, pure, unit-tested
Monthly steps in nominal terms; rates converted from annual with `(1+r)^(1/12)`. Inputs: C0 (funded today, goal currency), the Fire block, today.
- Contribution per month = amount × 30.4375 / days (week 7, two weeks 14, year 365.25/12 → amount/12, month = amount); the contribution keeps its purchasing power (inflated yearly). *Call: contributions are in today's money like every other amount.*
- Accumulate: `V += V·g + c(t)` until the FIRE date. Target nominal at month n = `T·(1+I)^(n/12)`.
- Derivation order per scenario: target ← spend × 12 / withdrawal when spend given and target blank; spend ← target × withdrawal / 12 when blank. Then: FIRE date ← first month `V ≥ T_nominal` (blank date, contribution and target known); contribution ← closed form `(T_nom − C0·(1+g)^n) / Σ_k (1+g)^(n−k)·(1+i)^k`, floored at 0 (blank contribution, date and target known); target ← `V(n)` deflated (blank target and spend, date and contribution known).
- Drawdown from the FIRE date: `V += V·g − S·(1+i)^(t/12)` until `V ≤ 0` or 200 years. Lasts-until = month of exhaustion; age from birth date. Horizon = birth + life expectancy (no birth date: FIRE date + 50 years, *call*). "Never ends" when not exhausted within 200 years and the real value at the end ≥ the real value at the FIRE date.
- Messages (one per case): no target, spend, or date+contribution → "Enter a target, a monthly spend, or both a FIRE date and a contribution"; no date and no contribution → "Enter a FIRE date or a contribution"; FIRE date not reached within the horizon → "Not reachable before {age/date} with this contribution"; date in the past, birth date in the future, life expectancy below age at FIRE → validation errors. When date, contribution and target are all given, the date is kept and the screen shows the shortfall or surplus at that date plus the earliest reachable date.

### Live rates — `Providers.kt`, same rules as quotes (HTTPS, size cap, no silent fallback)
- `Portfolio.rates: List<Rate>`; `Rate(kind: "inflation" | "growth", area, values: List<Pair<year, pct>>, source, observedAt, fetchedAt, error?)`, one per currency used by a FIRE goal; refreshed with quotes, kept on failure with the error shown like a quote error.
- Currency → area: USD→USA, EUR→EMU, GBP→GBR, JPY→JPN, INR→IND, BRL→BRA, RUB→RUS, CNY→CHN, others→WLD. Growth index per currency (Yahoo): USD ^GSPC, EUR ^GDAXI, GBP ^FTSE, JPY ^N225, INR ^BSESN, BRL ^BVSP, CNY 000001.SS, others ^GSPC (World Bank: same country codes, WLD has no index → USA).
- Endpoints (verified 2026-10-21): World Bank `api.worldbank.org/v2/country/{areas}/indicator/FP.CPI.TOTL.ZG?format=json&per_page=…` and `…/CM.MKT.INDX.ZG` (annual %); ECB `data-api.ecb.europa.eu/service/data/ICP/M.U2.N.000000.4.ANR?format=csvdata` (monthly, euro area); OECD `sdmx.oecd.org/public/rest/data/OECD.SDD.TPS,DSD_PRICES@DF_PRICES_ALL,1.0/…` (SDMX-JSON, no RUS/CNY/IND/BRA coverage guaranteed → error for those); Yahoo `query1.finance.yahoo.com/v8/finance/chart/{symbol}?range=30y&interval=1mo` (monthly closes; unofficial, so labelled as such in Settings and the manual). IMF DataMapper and Stooq block automated clients; FRED needs a key.
- From series to scenario rates (*call*): inflation average = mean of the last 20 annual values, optimistic / pessimistic = lowest / highest 5-year rolling mean in that window; growth average = CAGR over the whole series (≤ 30 years), optimistic / pessimistic = best / worst 10-year rolling CAGR. Price indexes exclude dividends; the manual says so. Resolution order per rate: Advanced override → fetched → built-in default.

### Defaults and sources (shown in the manual)
- UBS Global Investment Returns Yearbook 2026, 1900–2024, world: equities 9.8 % nominal, bonds 4.6 %, inflation 2.9 %. → growth 7 (≈ 60/40), optimistic 10, pessimistic 4; inflation 3, optimistic 2 (central-bank targets), pessimistic 5.
- Bengen 1994 / Trinity 1998: 4 % initial withdrawal, inflation-adjusted.

### UI
- Goal editor: `Choice` "Type: Savings goal / FIRE". FIRE shows the fields above, dates with the existing `dateField`, period with `Choice`, "N days" field when DAYS, an **Advanced** toggle revealing the eight numeric fields pre-filled with defaults.
- Goal screen: "FIRE" heading, a scenario table (Average / Optimistic / Pessimistic columns), the lasts-until line, the burndown drawn with Compose `Canvas` (no library): x = years from today, y = value in today's money, three paths, a vertical line at the FIRE date, horizon marker. Goals list card: projection line "FIRE on {date} · age {n}" (average).
- Overview, Plans, allocation: unchanged.
