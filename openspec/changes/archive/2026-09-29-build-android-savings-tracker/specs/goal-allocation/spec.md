## ADDED Requirements

### Requirement: Goal definition and lifecycle
A goal SHALL have purpose, positive target, currency, due date, integer priority (higher wins), and zero or more unique bucket connections. Archiving or deleting SHALL release allocation; past dates SHALL be allowed and labeled overdue when unmet.

#### Scenario: Unconnected goal
- **WHEN** a goal has no connections
- **THEN** progress is zero and the screen offers Connect bucket.

### Requirement: Connection ceilings
Connections SHALL support Auto, fixed goal-currency amount, percent of pre-allocation bucket value, or percent of goal target, with an optional goal-target percentage cap. Percentages SHALL be between 0 and 100. Ceilings SHALL never imply guaranteed funding or permit allocation beyond a goal's target.

#### Scenario: Percentage bases differ
- **WHEN** a €1,000 goal uses a €600 bucket with a 50% bucket limit and 20% goal cap
- **THEN** this connection can allocate at most €200.

### Requirement: Priority and equal sharing
The allocator SHALL process priorities descending from a consistent quote/balance snapshot, using the simultaneous capped equal-offer and residual-rerouting algorithm in design.md. Peer equality SHALL mean shared-bucket value, subject to target/connection caps. Lower priorities SHALL receive only remaining capacity.

#### Scenario: Equal priorities
- **WHEN** two uncapped peers each need at least €900 from a shared €900 bucket
- **THEN** each receives €450.

#### Scenario: Capped peer
- **WHEN** peers have targets €200 and €1,000 against €900
- **THEN** allocations are €200 and €700.

#### Scenario: Different priorities
- **WHEN** priority 2 needs €600 and priority 1 needs €600 from €900
- **THEN** allocations are €600 and €300.

#### Scenario: Multi-bucket saturation
- **WHEN** a €100 goal is equally connected to two €100 buckets
- **THEN** it receives €50 from each, without overfunding.

#### Scenario: Reroute to avoid stranded funds
- **WHEN** A=€100 connects G1/G2, B=€100 connects only G1, peers G1/G2 target €50/€100
- **THEN** G1 receives €50 from B and G2 €100 from A.

### Requirement: Conservation and stable results
Total allocation SHALL never exceed any bucket value, goal target, or connection cap. Results SHALL use exact decimal/integer arithmetic, stable UUID residual ties, and be independent of input collection order. Allocation SHALL only reserve value virtually. Stale/incomplete inputs SHALL visibly qualify results.

#### Scenario: Uneven smallest units
- **WHEN** one smallest allocation unit is shared by equal peers
- **THEN** a stable UUID tie determines the recipient and no extra unit is created.

### Requirement: Allocation explanations
Goal and bucket details SHALL show allocated, free, remaining and source contributions, and explain limiting caps, higher priority competition, missing rates, or no connection.

#### Scenario: Insufficient cap
- **WHEN** configured ceilings cannot fund a target despite spare bucket value
- **THEN** the UI identifies the limit and offers Edit connection without relaxing it automatically.
