## ADDED Requirements

### Requirement: Portfolio mode
Each bucket SHALL have a portfolio mode setting that the user can switch on and off at any time. Switching it off SHALL hide portfolio views and SHALL keep the stored targets and sell permission. Portfolio mode SHALL NOT change bucket value, goal connections or allocation.

#### Scenario: Switch off and on
- **WHEN** the user switches portfolio mode off and later on again
- **THEN** the previously entered targets are still present.

#### Scenario: Goals unaffected
- **WHEN** portfolio mode is switched on for a bucket connected to goals
- **THEN** goal funding is unchanged.

### Requirement: Target weights
In portfolio mode the user SHALL be able to set a target percentage for each asset held in the bucket and for assets not yet held. An asset SHALL be identified by its asset id: fiat code, native chain asset, or chain plus token contract address. Targets SHALL be between 0 and 100 with up to two decimals and SHALL total exactly 100 before they can be saved. Held assets without a target SHALL be treated as target 0. Unknown and excluded tokens SHALL NOT be offered as targets and SHALL NOT be part of the portfolio.

#### Scenario: Targets do not total 100
- **WHEN** the user enters targets totalling 95
- **THEN** saving is refused with a message stating the missing 5.

#### Scenario: Asset not yet held
- **WHEN** the user adds a 10 percent target for an asset with no holding in the bucket
- **THEN** it appears with real weight 0 and target 10.

### Requirement: Real versus target weights
In portfolio mode the bucket SHALL show, for every asset with a holding or a target, its value in the base currency, real percentage, target percentage and the difference. The base currency SHALL be the default currency from Settings. Holdings of the same asset SHALL be combined. If any asset in the bucket cannot be valued in the base currency, percentages SHALL be shown as unavailable with the reason, never calculated from partial totals.

#### Scenario: Drifted portfolio
- **WHEN** a bucket holds 7,000 of asset A and 3,000 of asset B in base currency with targets 60 and 40
- **THEN** A shows real 70, target 60, difference +10 and B shows real 30, target 40, difference -10.

#### Scenario: Missing rate
- **WHEN** one held asset has no exchange rate
- **THEN** weights are shown as unavailable and the asset without a rate is named.

### Requirement: Rebalancing calculator
In portfolio mode the user SHALL be able to enter a non-negative amount to invest in the base currency and receive a recommendation per asset, given as base-currency amount and asset quantity. With sells not allowed, the recommendation SHALL contain only buys, SHALL spend exactly the entered amount, and SHALL minimise the sum of squared differences between resulting and target values. The result SHALL show resulting percentages and the remaining difference per asset. The calculator SHALL NOT change any holding, SHALL state that it is an estimate based on cached rates, and SHALL use exact decimal arithmetic with deterministic rounding.

#### Scenario: New money fixes the drift
- **WHEN** A is 7,000, B is 3,000, targets are 60 and 40, and the user invests 2,000
- **THEN** the recommendation is buy 1,800 of B and 200 of A, giving 7,200 and 4,800, which is 60 and 40.

#### Scenario: New money is not enough
- **WHEN** A is 9,000, B is 1,000, targets are 50 and 50, and the user invests 1,000
- **THEN** the recommendation is buy 1,000 of B and nothing of A, and the result shows A still above target.

#### Scenario: Zero amount without sells
- **WHEN** the amount is 0 and sells are not allowed
- **THEN** the calculator recommends no trades and shows current differences.

### Requirement: Sells during rebalance
Each bucket SHALL have a setting "Allow sells during rebalance", off by default. When on, the calculator SHALL recommend the buys and sells that reach the targets exactly for the total of current value plus invested amount. Sells SHALL be labelled as sells in text. Assets held in read-only wallets SHALL be included, with a note that trades happen outside the app.

#### Scenario: Rebalance with sells
- **WHEN** sells are allowed, A is 9,000, B is 1,000, targets are 50 and 50, and the user invests 1,000
- **THEN** the recommendation is sell 3,500 of A and buy 4,500 of B.

### Requirement: Data conversion
Snapshots written before this change SHALL open with portfolio mode off, no targets and sells not allowed. Original files SHALL remain unchanged.

#### Scenario: Old folder
- **WHEN** a version 1 folder is opened
- **THEN** all buckets load with portfolio mode off and nothing else changes.
