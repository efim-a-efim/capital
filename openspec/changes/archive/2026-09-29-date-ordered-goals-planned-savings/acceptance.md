# Acceptance: date-ordered goals + planned savings (emulator-5554, 2026-09-29)

| Step | Result | Notes / screenshot |
|---|---|---|
| G1 | PASS | Older snapshots (schema 1 and 2) load, no "newer version" error (only "Ignored 1 invalid or interrupted snapshots" = capital-interrupted.json). Goals: heading "Sep 29, 2027" with Trip, Emergency. Zero "priority" in Goals/detail/editor/Overview dumps. New snapshot after rename Trip->Trip2: schema 3, Emergency priority 1, Trip2 priority 2, both due 2027-09-29 (old: Emergency 0, Trip 1 -> Trip first, kept). Cross-date conversion not exercised (both goals same date). G1-goals.png, G1-editor.png |
| G2 | PASS | Alpha, Beta (2027-03-01), Gamma (2027-06-01) + Emergency (2027-09-29). Alpha used the DatePicker (prev-month x6, day 1). Others used the text field `due`. G2-goals.png |
| G3 | PASS | Expected Alpha 600/600, Beta 300/600, Gamma 0/500, Emergency 0/600. Actual identical. |
| G4 | PASS | Drag Beta above Alpha: swapped, "Saved on device", Beta 600/600, Alpha 300/600. G4-before/after.png. Single-date goals (Gamma, Emergency) have no handle. Drag down 3 rows beyond group: stayed in date group (landed last in it), saved. G4-beyond.png |
| G5 | PASS (partial) | Handle content-desc "Reorder <name>", bounds 126x126 px at 420dpi = 48dp. TalkBack installed, not activated. Custom actions Move up/down are in code but not visible in uiautomator, not verified at runtime. |
| G6 | PASS | Editor has no priority field. Beta -> 2027-06-01: Beta listed after Gamma in Jun 1 group. G6-goals.png |
| G7 | PASS | Added Nov salary 500 (2026-11-25), Dec salary 500 (2026-12-25). Listed in date order with "· Planned". Overview total: EUR display was "€0.00 / Unavailable" before and after (no USD->EUR quote); after switching currency to USD total = $900.00 with 1100 planned - unchanged by planned. Projection lines below. Beta detail lists Nov salary +$400.00 (Nov 25, 2026) and Dec salary +$200.00 (Dec 25, 2026). G7-*.png |
| G8 | PASS | "Old" 50 USD 2026-08-29: "Aug 29, 2026 · Archived · date passed", listed after active ones (not date sorted), projections identical. Delete shows "Confirm change / Delete planned saving Old?"; disappears. (I created Old twice by mistake; both deleted.) G8-*.png |
| G9 | PASS | No FATAL EXCEPTION in logcat; only uiautomator AndroidRuntime START lines. |

## Projection arithmetic (Reserve 900 USD)
Rank: Alpha(600), Gamma(500), Beta(600), Emergency(600). Now: Alpha 600, Gamma 300, Beta 0, Emergency 0.
Oct 29 Salary +100 -> Gamma 100 (Gamma total 400). Nov 25 +500 -> Gamma 100 (closes), Beta 400. Dec 25 +500 -> Beta 200 (closes), Emergency 300 (short 300). Matches app.

## Verbatim projection lines
- Alpha: `Funded now`
- Gamma: `Planned savings close this goal on Nov 25, 2026 · on time`
- Beta: `Planned savings close this goal on Dec 25, 2026 · on time`
- Emergency: `Planned savings cover up to $300.00 of $600.00 by Dec 25, 2026 · short by $300.00`
- Before planned savings added (only Salary 100): Beta `Planned savings cover up to $400.00 of $600.00 by Oct 29, 2026 · short by $200.00`; unconnected goal `No planned savings reach this goal`

## Issues / UX notes
1. Connection editor bucket label "Reserve · 2535" shows a raw 4-char id prefix, confusing.
2. "Saved on device" banner pushes the list down ~163px, layout jumps (shifts tap targets).
3. Overview with display currency EUR and no rate: total "€0.00" while $900 exists; only "Unavailable · EUR" under Allocated. Misleading headline.
4. Editor date is a plain text field with a "Choose date" button; both work.
5. Archived planned savings sort after active ones (not by date) - fine but undocumented.
6. Small test folder was nested inside Documents/CapitalTest-copy (CapitalTest-1790703501105/CapitalTest-1790703576987); the copy folder now also contains one schema-3 snapshot from G1 and an app-created subfolder. Settings currency was changed EUR->USD in the test folder (G7).
7. Currently connected folder: small test folder (with Alpha/Beta/Gamma, Nov/Dec salary).
