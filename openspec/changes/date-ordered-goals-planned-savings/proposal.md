## Why

Numeric priorities are abstract: users think "the nearest deadline comes first". Users also save a planned amount from each salary and need to see when each goal closes and whether it closes in time.

## What Changes

- **BREAKING** Goals are ordered by due date, nearest first. Within one date the user orders goals by dragging. Priority numbers are assigned automatically from that order and hidden everywhere in the UI. The priority field leaves the goal editor. Equal-priority sharing between goals no longer occurs, because every goal has its own rank.
- Goals screen becomes a list of dates, each with its goals.
- New planned savings: name, amount, currency, date. Not tied to any bucket. Shown on the Buckets screen as "Planned savings". After its date a planned saving is archived, stays visible until deleted, and no longer counts.
- Projection: planned savings fund goals by the same priority rules as buckets. Each goal shows which planned savings contribute how much, and the date it closes or the maximum coverage it reaches.
- Snapshot schema 3 with automatic conversion of schema 1 and 2 data.

## Capabilities

### New Capabilities
- `goal-ordering`: date-based goal ranking, manual order within a date, hidden automatic priorities, data conversion.
- `planned-savings`: planned savings records, archiving by date, goal closure projection and its display.

### Modified Capabilities

None. `openspec/specs/` is empty; earlier changes are not archived.

## Impact

`domain/Model.kt` (Planned, ranking, validation), `domain/Allocation.kt` (extra unattached funds, projection), `data/Snapshots.kt` (schema 3), `CapitalModel.kt` (ranking on every edit), `ui/CapitalApp.kt` (goals by date, drag, planned savings list and editor, projection texts), tests, instrumentation test, README. No new dependencies.
