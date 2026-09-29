## Context

Snapshots are immutable JSON files `{payload, sha256}` in a user-selected folder, often synced by third-party tools. App-private storage holds the folder grant and API keys protected by an Android Keystore key. The app has no backend, so no server can throttle guesses or reset a password. minSdk is 26.

References: [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html), [Android Keystore](https://developer.android.com/privacy-and-security/keystore), [androidx.biometric releases](https://developer.android.com/jetpack/androidx/releases/biometric), [BiometricPrompt with CryptoObject](https://developer.android.com/identity/sign-in/biometric-auth).

## Goals / Non-Goals

**Goals:** files unreadable without the password; daily use without typing the password; lock usable on its own; switchable both ways without data loss.

**Non-Goals:** password recovery, remote wipe, per-record encryption, hiding file count or sizes, plausible deniability, protection against a rooted or compromised device while unlocked, erasing copies already held by sync services.

## Decisions

### 1. Key hierarchy
- **Data key**: random 256-bit key per folder, encrypts snapshot payloads with AES-256-GCM, fresh 96-bit random nonce per file.
- **Password key**: Argon2id over the password with a random 16-byte salt, wraps the data key. Parameters m=46 MiB, t=1, p=1, the first OWASP-recommended setting, stored in the file header so they can be raised later. Bouncy Castle `Argon2BytesGenerator` is already a dependency.
- Every encrypted snapshot carries the wrapped data key, salt and parameters in its header, so any single file plus the password is enough to open the folder on a new device.
- Password change re-wraps the data key and writes one new snapshot. The data key itself does not change, so every older snapshot stays readable once the newest file has been opened with the new password. Older files still carry the old wrapping, so an old password can open old files; a user who needs that closed switches encryption off and on again, which writes a fresh data key and removes the old files.
Alternative rejected: deriving the file key directly from the password. A password change would force rewriting every snapshot.
Alternative rejected: PBKDF2. It is not memory-hard; it stays the fallback only if Argon2 proves too slow on low-memory devices in testing.

### 2. File format
`{"enc":1,"id":…,"parents":[…],"schema":…,"kdf":{"alg":"argon2id","m":…,"t":…,"p":…,"salt":…},"wrapped":…,"nonce":…,"ciphertext":…}`. The clear fields `id`, `parents`, `schema`, `kdf` and `wrapped` are bound as GCM associated data, so they cannot be altered undetected. The ciphertext is the existing `{payload, sha256}` envelope. Revision ids and parents stay readable so head and conflict detection work before unlocking; they are random UUIDs and reveal only the shape of the history.

### 3. Device-bound unlock
After the password is entered once, the data key is stored in app-private storage wrapped twice: by a PIN-derived key (Argon2id, own salt) and by a non-exportable Keystore AES key. Guessing the PIN therefore has to happen on the device, where the attempt counter applies. Without a PIN only the Keystore wrap is used.
Biometric unlock stores a second copy wrapped by a Keystore key created with `setUserAuthenticationRequired(true)` and `setInvalidatedByBiometricEnrollment(true)`, released through `BiometricPrompt` with a `CryptoObject` and `BIOMETRIC_STRONG`. Enrollment changes invalidate the key, which gives the required re-entry of the PIN.
Alternative rejected: a PIN compared against a stored hash with the key kept unprotected. That is a screen gate only and gives no protection once app storage is read.

### 4. Attempt limits
A counter in app-private storage is increased before each check and reset on success. From 5 failures waiting times grow from 30 seconds to 1 hour. At 10 failures the device-bound copies are deleted; with encryption on the password is required, with encryption off the lock is cleared only by reinstalling or by clearing app data, which also removes the folder grant.

### 5. Switching encryption
Requires one head, no conflict, no unsaved edits. Steps: offer backup export, write the current portfolio as a new root snapshot in the target form, read it back and compare, then delete every snapshot of the other form, then record the mode. A marker file in app-private storage lets an interrupted switch resume. History before the switch is dropped from the folder by design, because keeping plaintext history would defeat encryption. The user is told that synced copies and provider-side version history are outside the app's reach.

### 6. App lock behaviour
Lock state lives in the ViewModel. `onStop` records the time; `onStart` locks when the selected time has passed. Locked state renders only the lock screen, cancels refresh and clears decrypted data from the UI state. `FLAG_SECURE` is set while a PIN or encryption is active. PIN entry uses an in-app keypad with text labels and 48 dp targets.

### 7. Dependency
`androidx.biometric:biometric` 1.1.0, stable since 2021 and maintained by Google. The 1.2 and 1.4 lines are alpha and are not used.

## Risks / Trade-offs

- Forgotten password means lost data → explicit warning, backup offer, password confirmation.
- Argon2id at 46 MiB may be slow or fail on low-memory devices → measure on the oldest supported device; parameters are in the header and adjustable.
- Deleting snapshots breaks the version 1 rule of never deleting → limited to the explicit switch, after verification, with a resumable marker.
- Another device still running with the old form may write old-form snapshots after the switch → treated as a conflict; the user resolves it and the app offers to finish the cleanup.
- Sync services may keep deleted plaintext files in their history → stated in the switch dialog.
- A 4-digit PIN has low entropy → Keystore binding plus attempt limits; longer PINs allowed.

## Migration Plan

Encryption and lock are off after upgrading; nothing changes until the user switches them on. Encrypted folders cannot be opened by version 1, which rejects the unknown file form without modifying it. Rollback: switch encryption off, then downgrade.

## Open Questions

- Lock without encryption after 10 wrong PIN entries: there is no password to fall back on. Proposed: the lock stays closed with the longest waiting time and can be cleared only by clearing app data, which keeps the folder intact. Needs your decision.
- Should the lock time choices include "never while the device stays unlocked"? Proposed: no.
- Should plaintext history removal be optional for users who only want a lock? It is: the lock works without encryption and removes nothing.
