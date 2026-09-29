# goal-ordering Specification

## Purpose
Ranking of goals by due date and manual order within a date, with hidden automatic priorities.
## Requirements
### Requirement: Date-based goal ranking
Active goals SHALL be ranked by due date, nearest first, then by the user-defined order within the same date. The rank SHALL determine funding priority: a higher-ranked goal is funded before any lower-ranked goal. Priorities SHALL be assigned automatically after every change to goals and SHALL be distinct for all active goals.

#### Scenario: Nearer date wins
- **WHEN** goal A is due 2027-01-01 and goal B is due 2027-06-01 and one bucket connected to both cannot fund both
- **THEN** A is funded first and B receives the remainder.

#### Scenario: Due date edited
- **WHEN** a goal's due date changes to a date that already has goals
- **THEN** it is placed last within that date and priorities are reassigned.

### Requirement: Order within a date
When two or more goals share a due date, the user SHALL be able to reorder them by dragging. The same reordering SHALL be available without dragging through accessibility actions "Move up" and "Move down". The new order SHALL be saved and SHALL change funding priority. Goals SHALL NOT be draggable across dates.

#### Scenario: Drag within a date
- **WHEN** the user drags the second goal of a date above the first
- **THEN** the order is saved and the dragged goal is funded before the other.

#### Scenario: Single goal on a date
- **WHEN** a date has one goal
- **THEN** no reorder control is shown.

### Requirement: Hidden priorities and date grouping
The Goals screen SHALL list dates in ascending order, each followed by its goals in rank order. Archived goals SHALL appear in a separate section after active dates. Priority values SHALL NOT be shown or editable anywhere in the UI, including conflict and restore summaries.

#### Scenario: Goals screen
- **WHEN** goals exist on three different dates
- **THEN** three date headings appear nearest first, each with its goals, and no priority number is visible.

### Requirement: Data conversion
Snapshots written before this change SHALL be converted when opened: goals are ranked by due date, then by their previous priority (higher first), then by previous list position. The original files SHALL remain unchanged. Converted data SHALL be written as the new schema on the next save.

#### Scenario: Old priorities on one date
- **WHEN** an old snapshot has two goals on the same date with priorities 5 and 1
- **THEN** the priority-5 goal is ordered first within that date.

#### Scenario: Old priority contradicts dates
- **WHEN** an old snapshot gives a later goal a higher priority than an earlier goal
- **THEN** after conversion the earlier goal ranks first.

