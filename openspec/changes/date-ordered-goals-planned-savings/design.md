## Context

`Goal.priority` is a user-entered integer; the allocation engine funds priority groups from highest to lowest and shares equally inside a group. Goals are stored in a list. Schema is 2 after wallet tokens. The UI is one scrolling Column per screen, not a lazy list.

## Goals / Non-Goals

**Goals:** date-first ranking with manual order inside a date; hidden priorities; planned savings with per-goal projection; lossless conversion of old data.

**Non-Goals:** recurring savings rules, reminders or notifications, turning a planned saving into a holding automatically, projections of price changes, per-goal links for planned savings, drag between dates.

## Decisions

### 1. Priority stays in the model as derived storage
`Portfolio.ranked()` sorts active goals by `(due, priority descending, list index)` and assigns `priority = count - index`, so ranks are distinct and the engine is untouched. Archived goals keep priority 0. `CapitalModel.edit` applies `ranked()` to every transformed portfolio before validation.
Reorder inside a date swaps the priorities of two neighbours, then `ranked()` renumbers.
A goal moved to another date gets priority 0 before ranking, which places it last within the new date.
Alternative rejected: a separate `order` field plus computed priority. It adds a field and a second source of truth for the same fact.

### 2. Conversion
`SCHEMA = 3`. `decodeRevision` applies `ranked()` when the stored schema is below 3. The sort key keeps the old priority as the tie-break inside a date, which preserves user intent where it does not contradict dates. `Portfolio.planned` defaults to empty, so older payloads decode unchanged. Originals are never rewritten; the next save writes schema 3.

### 3. Drag without a dependency
Each goal row in a date with two or more goals gets a drag handle. `pointerInput` with `detectDragGestures` on the handle tracks vertical offset; the row follows through `graphicsLayer`; crossing half the neighbour height swaps positions in a local list; release commits one edit. The handle carries semantics custom actions "Move up" and "Move down" for TalkBack and switch access.
Alternative rejected: `sh.calvin.reorderable`. It targets lazy lists and adds a dependency for a list of a few rows.

### 4. Planned savings and projection
`Planned(id, name, amount, currency, date)` in `Portfolio.planned`. Archived is derived: `date < today`. No stored state, so clock changes cannot corrupt data.
`Portfolio.allocate(extra)` adds one virtual bucket with value `extra` USD and an uncapped connection to every active goal. `Portfolio.project(today)` runs the engine once with no extra funds, then once per active planned saving in `(date, list index)` order with the cumulative amount. The contribution of a saving to a goal is the increase of that goal's funded total between consecutive runs. This reuses the engine, so caps, priorities and rerouting behave exactly as for buckets.
A goal closes at the first step where funded equals target. Without closure, maximum coverage is the final funded amount and its date is the last step that increased it.
Cost is one allocation per planned saving; acceptable for tens of entries.
Alternative rejected: a separate greedy pass over missing amounts. It would ignore that planned funds free bucket money for goals that only buckets can reach.

### 5. Interface
Goals: date headings, rows without priority, projection line per goal. Goal detail: "Planned savings" list with contributions. Goal editor: no priority field. Buckets: "Planned savings" section after buckets, rows show name, amount, date and "Planned" or "Archived · date passed". Editor kind "Planned" with name, currency, amount, date picker. Explanatory text changes to "Earlier dates are funded first; within a date, the order shown."

## Risks / Trade-offs

- Users who relied on equal sharing between same-priority goals lose it → stated as breaking; goals on one date fund in the shown order.
- Old priority that contradicts dates is overridden → intended by the new rule; conversion keeps old priority only inside a date.
- Projection assumes today's balances and rates → labelled as a plan, shown as reference text only.
- Custom drag can fight the parent scroll → drag starts only on the handle; move up and down actions are the fallback.

## Migration Plan

Open old folder → goals ranked in memory → first save writes schema 3. Older builds reject schema 3 with the upgrade message. Rollback: restore a backup exported before upgrading.
