# Encryption + lock acceptance (emulator-5554, build 2.0.0)

Setup deviations: app was NOT installed at start (`Activity class does not exist`); installed app/build/outputs/apk/debug/app-debug.apk with `adb install -r` (no gradle), chose newest folder CapitalTest-1790712828618. Save picker offered no "Documents" root, backups went to /sdcard/Download. Folder holds a stray 7-byte `capital-interrupted.json` ("partial") left by the test run; it is not a snapshot, stays plaintext, and is counted by the app in "n of m files" (m includes it).

| # | Result | Evidence |
|---|---|---|
| E1 | PASS | No lock at start. Encryption switch off, Change password/Set PIN/biometrics disabled, notes "PIN and biometric unlock are available when encryption is on..." and "Biometric unlock needs encryption to be on." Folder: 7 files at start (6 snapshots + interrupted), after 2 renames 9 files (8 snapshots + interrupted). JSON readable, contains Reserve. Also a new snapshot is written on every app open/refresh. |
| E2 | PASS (path differs) | Export saved to Downloads as capital-backup-2026-09-29.json (plaintext). |
| E3 | PASS | Warning text below. Cancel: switch off, files plaintext. "abc" -> "Password must be at least 8 characters"; mismatch -> "Passwords do not match". Encrypt: ~4.8 s total (dump-poll granularity ~1 s), progress "Encrypting…", then "Set a PIN?" dialog ("Not now" chosen). "Encryption is on" pop-up not caught (dialog covers / FLAG_SECURE blocks screenshots). |
| E4 | PASS | 8 snapshots all `"enc":1`, none contain Reserve/Cash/Emergency/Salary/900. Files were renamed `capital-<id>-<8hex>.json`. Count unchanged (9 incl. interrupted.json, which stays plaintext "partial"). Header: enc 1, id, parents, schema 4, key.kdf {alg argon2id, m 47104, t 1, p 1, salt}, key.wrapped, nonce, ciphertext. One identical key header in all files. Overview values unchanged (EUR 791.47 etc.). |
| E5 | PASS | Cold start: "Capital is encrypted", Password field, Unlock, "There is no password recovery."; no financial text. Wrong x3 -> "Wrong password", no file created/changed. Times tap->error (upper bounds incl. ~1-2 s uiautomator dump latency): 2.7 s, 6.7 s, 4.6 s (vary). Correct password: <=4.6 s. See bug B1 (timing). Every correct unlock writes a NEW encrypted snapshot (B4). |
| E6 | PASS | Rename to ReserveX: new file, `"enc":1`, no leaks. |
| E7 | PASS | Set PIN dialog ("4 to 12 digits.", Save PIN) -> saved; "PIN set" pop-up not caught. Lock "Immediately": HOME, 3 s, reopen -> "Enter PIN", pad, "Use password" at bottom, no financial text. 2468+OK unlocked in <1.5 s. |
| E8 | PASS | 5 x 1111: "Wrong PIN" (no remaining-attempts hint), after 5th: keypad disabled, "Too many wrong entries. Try again in 00:29" -> 00:27 counting. "Use password" visible; password screen has no "Back to PIN" while blocked. Password A unlocked (no PIN offer). Re-lock: keypad enabled, no message, 2468 works. |
| E9 | PASS (10-failure wipe NOT TESTED, needs 5+15+60 min waits) | After 30 s wait, 6th wrong -> "Try again in 00:58" (=01:00). Unlocked with Use password. |
| E10 | PASS | Force-stop + start -> PIN screen; "Use password" -> password screen with "Back to PIN"; password unlocks. |
| E11 | PASS | Device PIN 1111 set via locksettings, fingerprint enrolled via `emu finger touch 1` (8 touches). Switch on -> prompt "Unlock Capital" (negative button "Cancel") -> touch -> pop-up "Biometric unlock is on". Lock: prompt shows automatically (button "Use PIN"), touch unlocks. "Use PIN" -> PIN screen with extra "Use biometrics" button, PIN works. Device PIN cleared at end (`locksettings clear --old 1111`); fingerprint stays enrolled on the emulator. |
| E12 | PASS | screencap gives 15580-byte PNG 1080x2400, uniformly black (mean 0, variance 0). Recent-apps preview not checked. |
| E13 | PASS | Progress "Re-encrypting 14 of 16 files", pop-up "Password changed. Set your PIN again."; Settings shows "Set PIN" (PIN gone); 15 snapshots, one identical header, salt/wrapped differ from E4 (biometrics also reset). Cold start: A -> "Wrong password", B -> opens. |
| E14 | PASS | "capital-backup-2026-09-29 (1).json": `{"enc":1,...}`, its own new id, parents [], own kdf salt, no plaintext strings. Plaintext export: dialog "Export without encryption / This file will be readable by anyone who gets it." + password B, then picker, file readable JSON (`{"payload":"{\"schema\":4,...`). |
| E15 | PASS | "Turn off encryption" -> Decrypt, progress "Decrypting 9 of 17 files", pop-up "Encryption is off". All files plaintext (no enc), 17 files, ids of all 8 E4 snapshots present, all encrypted-era ids present plus 1 later. Switch off, PIN/biometrics/Change password disabled. Cold start opens directly. E15.png Overview OK. Files keep the `-<8hex>` suffix in names. |
| E16 | PARTIAL / resume path seen but text not captured | Kill after Encrypt tap: 2.0 s -> all plain; 2.5 s -> MIXED (19 plain, 3 enc); 2.6, 2.9, 3.1, 3.3 s -> all plain; 3.0 s / 3.3 s / 4 s -> all enc (window is ~0.3 s; a full rewrite of ~20 files is very fast). After the mixed kill: app asked password, loaded data (Overview ok); the Settings "finish-rewrite" button existed (my scripted lookup tapped it before I read the notice text) and the result was all files encrypted. The literal "An encryption change was interrupted." text was not verified on screen (it exists in Security.kt). End state: encryption OFF (28 plain files, app opens directly). |
| E17 | PASS | No FATAL EXCEPTION in `logcat -d -t 1500`. |

## Warning window text (E3, verbatim)
Title: "Before you encrypt"
- "There is no password recovery. If you lose the password, the data cannot be opened."
- "You exported 1 plaintext backup, the last on 9/29/26, 10:19 PM."  (later "You exported 2 plaintext backups, the last on 9/29/26, 10:42 PM.")
- "Backups exported without encryption stay readable. The app cannot encrypt or delete them."
- "Copies already held by sync tools are outside the app's control."
Buttons: "Cancel", "I accept the risk". Password dialog: "Choose a password" / "There is no password recovery." / Password, Confirm password / Cancel, Encrypt.

## Password delay measurements (tap Unlock -> result, upper bounds)
Wrong: 2.7 s, 6.7 s, 4.6 s (a second series with a coarser poll: 7.0 s x3 = poll artefact). Correct: 4.6 s. Values vary, so a random delay is present; the 6.7 s sample exceeds 5 s + KDF only if KDF+dump latency > 1.7 s, plausible on emulator but not proven.

## Bugs / UX
- B1 (needs check): cold start that skipped the lock. Twice, after force-stop + immediate `am start`, the app opened with data visible / with the "Set a PIN?" dialog and NO password prompt (no PIN set, encryption on): (1) right after enabling encryption then ~1 min use; (2) right after unlocking with the password and tapping "Not now", force-stop within ~1 s. Not reproducible later (5 attempts incl. force-stop 5 s after unlock, always the password screen; waiting 65-70 s always locks). Suggests a persisted "recently unlocked" grace surviving process death, contradicting "No PIN: each launch asks for the password". Only tested under `am force-stop`.
- B2: after every password unlock a "Set a PIN?" dialog appears again (no "don't ask again").
- B3: "Wrong PIN" gives no count of remaining attempts before the 5-attempt block.
- B4: every correct unlock/launch writes a new snapshot (file count grows 9 -> 28 during the run); files created after encryption/decryption have different name forms (`capital-<id>.json` vs `capital-<id>-<8hex>.json`).
- B5: the stray `capital-interrupted.json` ("partial") counts toward "n of m files" and is never encrypted/removed (may be a test artefact).
- B6: pop-ups "Encryption is on" and "PIN set" cannot be evidenced (dialogs/secure window); PIN screen title jumps ~50 px when the wait ends or when the biometrics button is present (keypad shifts).
- B7: while waiting time is active the password screen has no "Back to PIN" (by design), so the user cannot return to the PIN screen until the wait ends; minor.
- Keyboard: the "Choose a password" dialog moves up when the keyboard opens; fields stay reachable, Encrypt visible. No undismissable dialogs found (risk dialog dismisses only through its buttons; Encrypt/Decrypt dialogs disabled during work, as intended).

---

# Follow-up on B1 (cold start that skipped the lock)

Checked separately on 2026-09-29 with encryption on and no PIN: unlock with the password, then `am force-stop` and `am start` after 0, 0, 0.3, 0.6, 1, 1, 2 and 5 seconds. The process id changed in every run. In 8 of 8 runs the password screen was shown and the window hierarchy contained no financial text. The data key is never stored without a PIN, so a new process cannot decrypt the folder. The two earlier observations are attributed to a stale hierarchy dump; screenshots are blocked while encryption is on.

# Not verified

- PIN removal after 10 wrong entries: needs about 80 minutes of waiting times. The waiting-time table and the wipe rule are covered by code review and unit tests of the pure functions only.
- Recent-apps preview: not inspected. Screenshots are confirmed blocked.
- Biometric invalidation after a new enrollment.
- Argon2id timing on a physical low-memory device. On the emulator a PIN unlock took under 1.5 s and a password unlock stayed within the delay window plus about 1 s.
- Opening the folder from a second installation on another device.
