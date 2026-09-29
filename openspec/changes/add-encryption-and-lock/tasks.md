## 1. Feasibility

- [ ] 1.1 Measure Argon2id 46 MiB on the emulator and the oldest available device; record timings and choose final parameters. _(Emulator measured; physical low-memory device pending.)_
- [x] 1.2 Prototype Keystore double wrapping and BiometricPrompt with CryptoObject on API 26 and current API; confirm invalidation on new enrollment.

## 2. Encryption

- [x] 2.1 Implement key derivation, data key wrapping and the encrypted file form with authenticated header; test round trip, tamper detection, wrong password, parameter changes.
- [x] 2.2 Read and write encrypted snapshots in the folder store; scan heads and conflicts from clear headers; test mixed folders and corrupt files.
- [x] 2.3 Implement the resumable directory rewrite for switching on and off, keeping ids and ancestry; test interruption after every step, verification failure, and that no source file is removed before its replacement is verified.
- [x] 2.4 Password change with new data key and full re-encryption; test that the old password opens no file and that an interrupted change resumes.
- [x] 2.5 Password check with random 1 to 5 second delay on accept and reject; test bounds and equal handling of both outcomes.
- [x] 2.6 Record plaintext backup exports; warning window with explicit acceptance when switching on; encrypted and plaintext export; restore of both forms.
- [x] 2.7 Password entry per device and second-device open.

## 3. App lock

- [x] 3.1 Lock available only with encryption; switching off removes PIN, biometry and key copies; app opens directly when encryption is off.
- [x] 3.2 PIN set, change, remove; device-bound double-wrapped key; attempt counter with waiting times and deletion at 10 failures; test across restarts.
- [x] 3.3 "Use password" always available on the PIN screen, also during waiting times and after PIN deletion; resets the counter.
- [x] 3.4 Biometric unlock with fallback and invalidation handling.
- [x] 3.5 Automatic locking by background time, locked state without financial content, refresh cancelled while locked, FLAG_SECURE.

## 4. Interface

- [x] 4.1 Settings section "Security": encryption switch with warnings, password change with progress, PIN, biometry, lock time; PIN and biometry unavailable with explanation while encryption is off.
- [x] 4.2 Lock screen with keypad, biometric prompt, waiting-time display and the "Use password" link at the bottom.
- [x] 4.3 Password screen with non-skippable progress during the delay.

## 5. Acceptance

- [x] 5.1 Unit tests, lint, debug build, instrumentation test.
- [x] 5.2 Emulator run: switch on, inspect every file, history intact, reopen, wrong password, second install opens the folder, password change, switch off, interrupted rewrite resumes.
- [ ] 5.3 Emulator run: backup warning accept and decline, PIN, waiting times, ten failures, "Use password" in each state, biometric unlock with an enrolled emulator fingerprint, recent-apps preview, no lock with encryption off. _(All done except the 10-failure wipe and the recent-apps preview; see acceptance.md.)_
- [x] 5.4 README, roadmap and privacy notes updated.
