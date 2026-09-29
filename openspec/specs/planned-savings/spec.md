# planned-savings Specification

## Purpose
Planned savings records and the projection of when goals close.
## Requirements
### Requirement: Planned savings records
The app SHALL keep a list of planned savings, each with a name, a positive amount, a currency and a planned date. Planned savings SHALL NOT belong to any bucket and SHALL NOT change bucket values, totals, allocated or unallocated amounts. They SHALL be listed on the Plans tab in date order with add, edit and delete. Delete SHALL require confirmation.

#### Scenario: Add planned saving
- **WHEN** the user adds "October salary", 500 EUR, 2026-10-25
- **THEN** it appears on the Plans tab and total valued savings are unchanged.

### Requirement: Archiving by date
A planned saving whose date is before today SHALL be shown as archived with a text label, SHALL remain visible until the user deletes it, and SHALL NOT contribute to projections. A planned saving dated today SHALL still count. Archived planned savings SHALL be listed after active ones under an "Archived" heading, newest first, in a muted tone, and SHALL stay editable and removable. The state SHALL follow the date only: a planned saving edited to today or a later date SHALL become active again and count in projections.

#### Scenario: Date passes
- **WHEN** the device date moves past a planned saving's date
- **THEN** the saving is labelled archived and goal projections no longer include it.

#### Scenario: Reactivate by edit
- **WHEN** the user changes the date of an archived planned saving to a future date
- **THEN** it moves back to the active list and goal projections include it.

### Requirement: Goal closure projection
The app SHALL project goal funding from the current allocation plus active planned savings applied in date order. Planned funds SHALL be available to every active goal and SHALL follow the same priority rules as bucket funds. Within each priority level bucket funds SHALL be applied first and planned funds SHALL only cover what buckets leave open, so a goal that buckets fund in full takes no planned money. For each planned saving the projection SHALL record how much it adds to each goal. A planned saving whose currency cannot be converted SHALL be skipped and the projection marked incomplete. The projection SHALL NOT change the current allocation.

#### Scenario: Two goals, two salaries
- **WHEN** goal A (nearer date) lacks 300 and goal B lacks 400, and savings of 500 are planned on 1 Nov and 1 Dec
- **THEN** the 1 Nov saving adds 300 to A and 200 to B, the 1 Dec saving adds 200 to B, A closes on 1 Nov and B closes on 1 Dec.

#### Scenario: Not enough planned
- **WHEN** planned savings cover only part of a goal's missing amount
- **THEN** the goal has no closing date and its maximum coverage and the date it is reached are reported.

### Requirement: Projection display
Each goal in the Goals list SHALL show one projection line: already funded; or the date it closes with whether that is on time or how many days after the due date; or the maximum amount covered, the date that maximum is reached and the remaining shortfall; or that no planned savings reach it. Goal detail SHALL list each contributing planned saving with its date and the amount it brings to that goal in the goal's currency. Status SHALL be conveyed by text, not color alone. Each active goal SHALL also carry one badge next to its name: green "Funded" when it is funded now; green "Funded in time" when it is not funded now and planned savings close it on or before its due date; yellow "Not funded" otherwise. All badges SHALL use the same dark text colour on the badge colour as background. Archived goals SHALL carry no badge.

#### Scenario: Closes late
- **WHEN** a goal due 1 Nov is projected to close with a saving dated 25 Nov
- **THEN** its line reads that it closes on 25 Nov, 24 days after the due date.

#### Scenario: Goal detail
- **WHEN** a goal receives 300 from "October salary" and 100 from "November salary"
- **THEN** its detail lists both savings with dates and those amounts.

