## 1. Feasibility

- [ ] 1.1 Measure Argon2id 46 MiB on the emulator and the oldest available device; record timings and choose final parameters.
- [ ] 1.2 Prototype Keystore double wrapping and BiometricPrompt with CryptoObject on API 26 and current API; confirm invalidation on new enrollment.

## 2. Encryption

- [ ] 2.1 Implement key derivation, data key wrapping and the encrypted file form with authenticated header; test round trip, tamper detection, wrong password, parameter changes.
- [ ] 2.2 Read and write encrypted snapshots in the folder store; scan heads and conflicts from clear headers; test mixed folders and corrupt files.
- [ ] 2.3 Switch encryption on and off with verification, cleanup and resumable marker; test failure at each step and that no file is removed before verification.
- [ ] 2.4 Password entry per device, password change, encrypted and plaintext backup export, restore of both forms; test second-device open.

## 3. App lock

- [ ] 3.1 PIN set, change, remove; device-bound double-wrapped key; attempt counter with waiting times and key deletion; test across restarts.
- [ ] 3.2 Biometric unlock with fallback to PIN and invalidation handling.
- [ ] 3.3 Automatic locking by background time, locked state without financial content, refresh cancelled while locked, FLAG_SECURE.

## 4. Interface

- [ ] 4.1 Settings section "Security": encryption switch with warnings and backup offer, password change, PIN, biometry, lock time.
- [ ] 4.2 Lock screen with keypad, biometric prompt, waiting-time display and password fallback.
- [ ] 4.3 Texts for lock without encryption, synced copies, and lost password.

## 5. Acceptance

- [ ] 5.1 Unit tests, lint, debug build, instrumentation test.
- [ ] 5.2 Emulator run: switch on, inspect files, reopen, wrong password, second install opens the folder, switch off, interrupted switch resumes.
- [ ] 5.3 Emulator run: PIN, waiting times, ten failures, biometric unlock with an enrolled emulator fingerprint, recent-apps preview.
- [ ] 5.4 README, roadmap and privacy notes updated.
