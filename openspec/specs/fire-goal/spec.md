# fire-goal Specification

## Purpose
FIRE goal type: solver, scenarios, burndown and live inflation and market-growth rates.
## Requirements

### Requirement: FIRE goal type
A goal MAY be of type FIRE. It SHALL accept a birth date, a FIRE date, a recurring contribution (amount per week, two weeks, month, year or N days), a monthly spend after FIRE and an amount to have after FIRE, all in today's money of the goal currency. FIRE date, contribution, target and spend MAY each be blank. A contribution growth percent per year MAY be given; when blank the contribution SHALL keep its purchasing power.

#### Scenario: Derive the FIRE date
- **WHEN** the FIRE date is blank and the spend is known (given, or from the target)
- **THEN** each scenario derives it as the first month from which the spend lasts until life expectancy (50 years without a birth date) and, when a target was given, the target is reached; the goal's due date takes the average scenario's date and each scenario's caption shows its own.

#### Scenario: Derive the contribution
- **WHEN** target and FIRE date are given and the contribution is blank
- **THEN** the screen shows the monthly contribution that reaches the inflated target on that date per scenario, zero when today's funding already suffices.

#### Scenario: Derive target from spend
- **WHEN** spend is given and the target is blank
- **THEN** the target is spend × 12 ÷ withdrawal rate (today's money) and the nominal value at the FIRE date is shown next to it.

#### Scenario: Not enough data
- **WHEN** neither target nor spend is given and date or contribution is blank
- **THEN** the screen shows "Enter a target, a monthly spend, or both a FIRE date and a contribution" and no results.

### Requirement: Scenarios and defaults
The calculation SHALL run for average, optimistic and pessimistic growth and inflation rates, defaulting to 7/10/4 % growth and 3/2/5 % inflation, with a 4 % withdrawal rate, a working age of 65 (a derived FIRE date is not searched past it) and a life expectancy of 95, all overridable in the goal's Advanced section. The manual SHALL cite the sources of the defaults.

#### Scenario: Override
- **WHEN** the user sets growth to 5 % in Advanced
- **THEN** the average scenario uses 5 % and the others keep their values.

### Requirement: Burndown
After the FIRE date the value SHALL decrease by the inflation-adjusted spend each month and grow by the scenario's rate. The screen SHALL say until which date (and age, when the birth date is known) the money lasts; when it lasts beyond life expectancy it SHALL say for how many years; when it is never exhausted it SHALL say "money will never end 🥳". The screen SHALL show the values equal in every scenario once, then one foldable row per scenario with its growth and inflation rates and a badge: "Money is enough", "money will never end 🥳" or "Not enough money". A chart SHALL draw the value in today's money from today to the end of the money or the horizon for the three scenarios, with the FIRE date marked.

#### Scenario: Money outlives the saver
- **WHEN** the drawdown ends after the life-expectancy age under a scenario
- **THEN** that scenario shows "lasts {n} years, beyond life expectancy".

### Requirement: Allocation unchanged
A FIRE goal SHALL be funded, ordered and projected like any goal, using its (possibly derived) target and due date.

#### Scenario: Derived values refresh
- **WHEN** a connected bucket's value changes and the goal's target or date was derived
- **THEN** the next edit or refresh recomputes them.

### Requirement: Live inflation and growth
Settings SHALL offer an Inflation provider (World Bank, ECB, OECD, Off) and a Market growth provider (World Bank, Yahoo Finance, Off). On refresh the app SHALL fetch the series for each currency used by a FIRE goal and derive the three scenario rates from it; a fetch failure SHALL keep the previous values and show the error. Off SHALL use the built-in defaults. An Advanced override SHALL always win.

#### Scenario: Euro goal with ECB
- **WHEN** Inflation is set to ECB and the goal is in EUR
- **THEN** the euro-area HICP series is used; a goal in USD shows "ECB does not cover USD".

### Requirement: Spend now
Each scenario SHALL show **Spend now** per month and per year in today's money. Before the FIRE date it SHALL be 0. From the FIRE date on it SHALL be the goal's current funds (latest refreshed prices) paid out in equal real monthly amounts until life expectancy (50 years after the FIRE date without a birth date, at least 12 months), at the scenario's growth and inflation. A FIRE date in the past SHALL be accepted and mean that the spend period has started.

#### Scenario: Markets fall after FIRE
- **WHEN** the goal's funds fall by half after a refresh
- **THEN** Spend now falls by half.

#### Scenario: Still saving
- **WHEN** the FIRE date is in the future
- **THEN** Spend now is 0.
