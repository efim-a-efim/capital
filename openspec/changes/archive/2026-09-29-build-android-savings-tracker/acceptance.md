# Acceptance record — 2026-09-29

Emulator: Medium_Phone AVD, API 17-era image name in results, emulator-5554. Screenshots: `screenshots/` keeps 8 representative captures; the full set stays local in `.tools/` (untracked).

Remaining manual checks (need a physical device and the user's own sync tool): real-device SAF folder access (task 1.2) and third-party folder sync of the snapshot folder (task 6.3).

# Acceptance run 2026-09-29 (emulator-5554, dev.capital debug)

Setup notes
- App had been uninstalled after the instrumentation run; reinstalled via `./scripts/gradle installDebug`. Grant was lost, folder re-picked via SAF.
- Test folders are NESTED (picker reopens inside the previous one): /sdcard/CapitalTest-1790632463302/CapitalTest-1790632826169/CapitalTest-1790632943331/CapitalTest-1790634389039 (used for A and B1/B2). Nothing was under /sdcard/Documents.
- Prior state was intact: Reserve, Cash 900 USD, goal Emergency (600 USD, p2). On load: "Ignored 1 invalid or interrupted snapshots" = capital-interrupted.json (7 bytes, injected by AppTest on purpose).
- Helpers: .tools/ui.sh (uiautomator tap/dump/shot), .tools/rename.sh.

## Scenario A (6.2)
| # | Step | Result | Observed | Screenshot |
|---|------|--------|----------|-----------|
| A1 | Add holding, Tracking=Wallet, chain BTC, address 1A1z..DivfNa, label Genesis | PASS | Mode selector is "Tracking" (Manual/Wallet); chain selector "Mainnet chain" (BTC default). Saved on device; holding shows "Read-only · 1A1zP1eP5QGe…" | A1-wallet-editor.png, A1-after-save.png |
| A2 | Settings: Crypto provider -> CoinPaprika | PASS | Choice dropdown under "Free data providers" (BTC=Blockstream, Fiat=Frankfurter kept; Crypto default CoinGecko changed to CoinPaprika) | A2-settings.png |
| A3 | Add goal Trip EUR 500 p1, connect Reserve, save | PASS | Goal detail shows connection row "Reserve / Auto - up to remaining need", "Missing rates or balances can limit funding" (pre-refresh) and rule note "Higher priority numbers are funded first. Equal priorities share bucket value. Allocation never moves money." Pre-refresh Trip = EUR 0 / 500 (no rates). | A3-goal-editor.png, A3-goal-detail.png, A3-goal-detail-scrolled.png |
| A4 | Emergency priority 2 -> 0 | PASS (order not observable) | Pre-refresh: Emergency still $600 funded, Trip EUR 0 (unconvertible goals excluded from allocation; Allocation.kt goalsUsd). Post-refresh both goals fully funded (Reserve ~ $4.78M), so priority order has no visible effect. Overview list shows Emergency(p0) above Trip(p1); Goals tab list showed Trip above Emergency earlier - ordering differs between tabs (check intended sort). | A4-emergency-p0.png, A4-goals-list.png, A5-goals-after-refresh.png |
| A5 | Refresh, wait 30 s | PASS | "Refreshed. Shared rates may revalue other buckets." Genesis: BTC 57.48 (57.47628667), $4,781,526.27. Overview: total EUR 4,195,479.10; incomplete banner gone. Note: Genesis value is in bucket currency (USD); EUR shown at Overview level only. BTC quote $83,257.88 via CoinPaprika. | A5-overview.png, A5-bucket-genesis.png |
| A6 | Offline reopen (wifi+data off, force-stop, relaunch) | PASS | No crash (crash log empty). Cached totals/goals shown. Message: "Genesis/BTC/EUR: Network unavailable or request timed out" plus banner "Cached / stale values - refresh when online. Allocations are estimates." Took ~40 s for refresh to time out ("Refreshing selected providers..." meanwhile; UI usable). | A6-offline-reopen.png, A6-offline-after-wait.png |
| A6b | Re-enable network, Refresh | PASS | "Refreshed. Shared rates may revalue other buckets." Stale banner cleared. | A6-online-refresh.png |

## Scenario B (6.3)
| # | Step | Result | Observed | Screenshot |
|---|------|--------|----------|-----------|
| B1 | Tap Save on edit then force-stop / am kill (2 attempts, 0 s and 0.4 s) | PASS (edit-lost branch only) | Relaunch: prior state intact, no crash, folder not corrupted. Edit was NOT persisted in either attempt (no new file before restart; edit lost, prior state kept). The "edit persisted" branch was not hit. Cold start auto-refresh added a snapshot each launch. File naming: all files `capital-<uuid>.json` except `capital-interrupted.json` (test-injected, 7 bytes, intentionally invalid; app ignores it and reports it). | B1-after-kill.png |
| B2 | rm -r folder in use, rename bucket | PASS | Save fails: "Cannot read folder. Reconnect it in Settings. Your changes remain in memory." Editor shows "Open save recovery"; recovery panel: "Unsaved changes - kept in memory...", buttons Retry save, Save copy to folder, Discard and reload; edited name "ReserveX" visible. "Reconnect" is not on the recovery panel; it is in Settings ("Reconnect / open folder"; not exercised here). | B2-save-failed.png, B2-recovery-actions.png |
| B2b | Save copy to folder -> new folder Documents/CapitalTest-copy | PASS | "Copy saved"; folder contains 1 capital-<uuid>.json; app now uses new folder. (Picker opened at storage root; had to navigate to Documents before creating the folder.) | B2-copy-saved.png |
| B3 | Sibling revisions H -> E1 (ReserveX1) / E2 (ReserveX2); restore E1 file; Reload local files | PASS | Conflict screen: "Sync conflict: choose a revision below. Both originals will be preserved." + "Editing is paused until storage is resolved."; lists revision db160bdc (ReserveX1) and ec824b8b (ReserveX2), each with "Keep this version". Editing UI hidden/blocked, bottom nav absent. Chose db160bdc -> confirm dialog "Use revision db160bdc? Both originals remain in the folder." -> new file c92fa428 with parents [db160bdc, ec824b8b] (length 2), data name ReserveX1. Folder has 4 files: H, E1, E2, merge. Local copies: .tools/b3/E1.json, M.json | B3-conflict.png, B3-editing-blocked.png, B3-resolved.png |
| B4 | Background requests | PASS | With app HOME'd: `dumpsys alarm | grep capital` empty. `dumpsys jobscheduler | grep capital` shows only system bookkeeping (uid 10241 "=false", "NOT active", debit tally 0), no registered jobs. `dumpsys activity services dev.capital`: nothing. Source grep for WorkManager/AlarmManager/Timer/JobScheduler/BroadcastReceiver: none (only Providers.kt `delay(1100)` in-foreground rate limit and CapitalModel `foreground` flag cancelling refresh in background()). Manifest has no service/receiver. | B4-home.png |
| B5 | Third-party sync tool (external folder sync) | USER-MANUAL | Cannot be done on emulator. | - |

## Findings / minor
1. B1 edit-persisted branch not observed; window between Save tap and file write is short. Not a defect.
2. Recovery panel lacks a direct "Reconnect" button (Settings has it). Message text points there.
3. Goal order differs between Overview (Emergency p0 first) and Goals tab (Trip p1 first) - verify intended.
4. Allocation order test (A4) inconclusive because Reserve funds both goals; to test ordering use a small bucket.
5. SAF picker nests test folders (each run's folder created inside previous). Cosmetic, test-only.
Network restored (wifi + data enabled) at end.

## Device checks (5.6)
Emulator-5554, 1080x2400 @420dpi (default). Build installed via ./scripts/gradle installDebug.

| # | Step | Result | Observed | Screenshot |
|---|------|--------|----------|-----------|
| C1 | 200% font (Overview, bucket, Goals, Settings, holding editor) | PASS with 1 cosmetic FAIL | All amounts fully visible, no truncation, actions reachable by scrolling, bottom nav labels intact. Cosmetic: Overview top-bar title wraps mid-word ("Overvi/ew") because Refresh+Settings actions squeeze it. Editor labels wrap to 2 lines but fields remain usable. Holding-editor Save reachable in top bar. | C1-overview, C1-bucket-scrolled, C1-goals, C1-settings, C1-holding-editor |
| C2 | Dark theme (Overview, Settings) | PASS | Text readable, banners (refresh/unsaved-changes) visible with distinct container colour, progress bars and bottom nav OK. Disabled controls (Retry save, provider buttons, "Refreshing…") while refresh runs are low-contrast (expected for disabled; not measured). Theme editor: "Appearance" selector offers System / Light / Dark. | C2-dark-overview, C2-dark-settings, C2-theme-options |
| C3 | Tablet 1600x2560 @240 (~1067dp) | PASS | NavigationRail with 4 items (Overview, Buckets, Goals, Settings; letter glyph icons O/B/G/S) shown; Buckets shows list pane (~220-250dp) + detail pane. Note: "← All buckets" back link still shown in detail pane on tablet (cosmetic). | C3-tablet-overview, C3-tablet-bucket |
| C4 | Touch targets (uiautomator, 420dpi = 2.625 px/dp) | PASS | All clickable nodes on Overview, Goals, Settings >= 48dp both axes (smallest: Refresh 74x48, Dismiss 135x48, Add goal 103x48). Only exception: TronGrid button 371x31dp, which is a scroll/nav-bar clipping artefact (node cut by bottom bar), not its real size (others in same class are 48dp). | ui dumps (scratch) |
| C5 | TalkBack | PASS | TalkBack installed; enabled, focus ring drew on banner text (Tab traversal works). Nav items expose label text (Overview/Buckets/Goals as child text of 132x80dp clickable); icons hidden as intended. No clickable node without text/desc on Overview, Goals, Settings (all clickable containers have named descendants). Spoken output itself not audible/verified. TalkBack disabled afterwards. | C5-talkback-1 |
| C6a | Decimal comma "12,5" in manual holding | PASS | Saved; displays "€12.50" (en-US). Field label says "no grouping separators". Holding deleted afterwards (confirm dialog), bucket back to $4,794,301.57 base. | C6-comma-entered, C6-comma-saved |
| C6b | German display | PASS (via per-app locale) | System route BLOCKED: `setprop persist.sys.locale` / `ctl.restart zygote` denied (no root). Used `cmd locale set-app-locales dev.capital --locales de-DE` instead: amounts show "4.207.635,46 €", "1.026,36 €", "600,00 $ / 600,00 $" (decimal comma, thousands point, symbol after). Priority/labels remain English (no translations, expected). | C6-de-overview |

Restored: font_scale 1.0, night no, wm reset, app locale cleared, accessibility off.

Findings: (1) Overview title wraps mid-word at 200% font. (2) Tablet detail pane keeps "← All buckets". (3) Unsaved-changes recovery banner was showing in Settings during dark check (state from earlier refresh/save; not investigated).
# Static a11y audit: CapitalApp.kt (2026-09-29)

## 1. Contrast (WCAG 2.x, >=4.5)
Overridden (l.42-43): primary, onPrimary, background, surface, surfaceVariant, onSurface, onSurfaceVariant. Rest = M3 baseline defaults (onBackground, secondary/tertiary/error containers, error). No custom amber/warning color exists: stale/incomplete use `Notice` (surfaceVariant/onSurfaceVariant).

| Pair | Light | Dark |
|---|---|---|
| onBackground/background (default on) | 16.30 PASS | 13.65 PASS |
| onSurface/surface | 13.78 PASS | 12.68 PASS |
| onSurfaceVariant/surfaceVariant (Notice) | 5.29 PASS | 5.36 PASS |
| onSurfaceVariant/surface (Note) | 6.00 PASS | 8.51 PASS |
| onSurfaceVariant/background | 6.00 PASS | 9.66 PASS |
| onSurface/surfaceVariant | 12.15 PASS | 7.98 PASS |
| onPrimary/primary | 7.05 PASS | 7.85 PASS |
| onSecondaryContainer/secondaryContainer (default) | 13.24 PASS | 7.19 PASS |
| onTertiaryContainer/tertiaryContainer (default) | 13.18 PASS | 7.20 PASS |
| onErrorContainer/errorContainer (default) | 12.77 PASS | 7.17 PASS |
| error/background (l.353 error text) | 6.22 PASS | 10.32 PASS |
| primary/background (TextButton labels) | 6.71 PASS | 10.16 PASS |

Note: unused-in-code containers listed for completeness. Disabled buttons (enabled=false) are exempt from WCAG.

## 2. Touch targets
Used: Button, OutlinedButton, TextButton (many), NavigationBarItem, NavigationRailItem, OutlinedTextField, AlertDialog buttons (TextButton). No DropdownMenuItem, IconButton, Checkbox.
- No `Modifier.size/height/heightIn` on interactive items; no `minimumInteractiveComponentSize` / `LocalMinimumInteractiveComponentSize` overrides. `height(24.dp)` l.~247 is a Spacer. **PASS** (M3 48dp default).
- `Item` TextButton has 14dp vertical padding, taller than 48dp.

## 3. TalkBack
| Item | Result |
|---|---|
| Nav icons Text("◫"/"▤"/"◎"), Text(target.take(1)) | FIXED: `Modifier.clearAndSetSemantics {}` (l.104, l.107; import l.19) |
| Icon() calls | none in file |
| Icon-only controls | none; all controls are text buttons |
| Stale/incomplete (l.142) | text: "Incomplete valuation..." / "Cached ..." (not color only) PASS |
| Errors (l.353, Notice l.180/355) | text, PASS |
| Nav selected state | provided by NavigationBarItem semantics |

## 4. 200% font scale
- No `maxLines`, `overflow`, or fixed-height containers on text. Amount text (Stat headlineSmall, Item titleLarge) wraps.
- Risks (no change made): fixed `Modifier.width(250.dp)` list pane (wide only, l.~110) narrows wrapped text; NavigationBar labels may wrap/clip in M3 item at 2.0 (system-managed); TopAppBar title single-line by default (M3 ellipsizes) plus 2 TextButton actions may squeeze title.

## 5. Decimal comma
- `number()` l.~300: `trim().replace(',','.').also { it.decimal() }` PASS. Connection cap (l.~336) also replaces ','.
- Display `money()` uses `NumberFormat.getCurrencyInstance()` / `getNumberInstance()` (default Locale) PASS. Date/time via localized DateTimeFormatter.
- Caveat: input parse assumes no grouping separators (e.g. "1,234.56" becomes "1.234.56" -> decimal() likely fails validation, shown as error).

## 6. Layout
- `wide=maxWidth>=840.dp` (l.98): NavigationBar (Overview/Buckets/Goals) hidden; NavigationRail (adds Settings) shown. CONFIRMED.
- List/detail: yes, partial. When wide and a bucket/goal is selected, a 250dp list pane (Item rows) sits beside detail. No pane when nothing selected (single column).
