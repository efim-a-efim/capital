## 1. Domain

- [x] 1.1 Add `Portfolio.ranked()`, apply it in `CapitalModel.edit`, restore and conflict resolution paths; test date order, order within a date, distinct priorities, archived goals, due-date move.
- [x] 1.2 Add `Planned` and `Portfolio.planned` with validation and limits; test.
- [x] 1.3 Add `allocate(extra)` virtual bucket and `project(today)`; test the two-salary example, partial coverage, archived savings ignored, unconvertible currency, no change to current allocation.
- [x] 1.4 Schema 3 conversion from schema 1 and 2; test same-date old priorities, contradicting priorities, originals unchanged, schema 4 rejected.

## 2. Interface

- [x] 2.1 Goals screen grouped by date, archived section, priorities removed from rows, editor, summaries and texts.
- [x] 2.2 Drag handle reorder within a date with "Move up" / "Move down" accessibility actions.
- [x] 2.3 Planned savings section on Buckets with add, edit, delete and archived label; editor with date picker.
- [x] 2.4 Projection line per goal in Goals list and planned-savings contributions in goal detail.

## 3. Acceptance

- [x] 3.1 Unit tests, lint, debug build; update the instrumentation test that typed a priority.
- [x] 3.2 Emulator run: old folder converts, goals grouped by date, drag reorder changes funding order, planned savings project closure dates, archived saving ignored.
- [x] 3.3 README updated.
