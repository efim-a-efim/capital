## Why

Goals say what to save by when. They cannot answer the question a long-term saver asks: "when can I stop working, and how long will the money last?" A FIRE (financial independence, retire early) goal turns the existing goal, its bucket funding and a few assumptions into that answer, with a burndown chart.

## What Changes

- A goal can be of type **FIRE**. Its editor adds: birth date, FIRE date, contribution ("I can invest X per week / 2 weeks / month / year / N days"), monthly spend after FIRE, and the amount to have after FIRE (the goal target). Each of FIRE date, contribution, target and spend may be left blank; the app derives what it can from the rest and says what is missing.
- **Advanced** section with defaults taken from public sources: inflation 3 % (optimistic 2, pessimistic 5), growth 7 % (optimistic 10, pessimistic 4), withdrawal rate 4 %, life expectancy 95. All overridable per goal.
- All amounts are entered in today's money; the goal screen shows nominal values next to them. Funding from connected buckets counts as today's money, as for every goal.
- Goal screen for a FIRE goal: for each of the three scenarios the FIRE date (with age when the birth date is known), required contribution, FIRE number (today's / nominal), monthly spend, and how long the money lasts: until a date (age), "lasts N years, beyond life expectancy", or "money will never end 🥳". A burndown chart (accumulation, then drawdown) draws the three scenarios.
- **Live rates.** Inflation and market growth are refreshed with the other quotes from free, key-less public APIs, chosen in Settings → Free data providers: **Inflation** World Bank (default) / ECB (euro area only) / OECD / Off; **Market growth** World Bank (default) / Yahoo Finance (unofficial endpoint) / Off. "Off" keeps the built-in defaults. Fetched values replace the defaults for the goal's currency; Advanced overrides still win. The sources are listed in the Privacy Policy and the manual.
- Derived target and FIRE date are written into the goal's target and due date, so allocation, ordering, the Overview and Plans stay unchanged.
- Snapshot schema 7 (new optional `fire` block on a goal).
- Site: new "FIRE" section on the Goal screen page and the manual, sources for the defaults, messages; Financial features declaration reviewed (still a calculation on the user's own numbers); 15 languages, screenshots.

## Non-goals

- Monte Carlo or historical sequence simulation; three fixed scenarios only.
- Taxes, pensions, state benefits, part-time income after FIRE.
- Creating Plans from the contribution; Plans linked to the goal do not enter the FIRE calculation.

## Capabilities

### New Capabilities
- `fire-goal`: FIRE goal type, solver, scenarios, burndown.

### Modified Capabilities
- `external-data`: inflation and market-growth providers.
- `goal-allocation`: a FIRE goal's target and due date may be derived.
- `local-storage`: schema 7.
