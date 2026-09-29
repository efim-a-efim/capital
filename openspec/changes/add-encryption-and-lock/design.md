## Context

Snapshots are immutable JSON files `{payload, sha256}` in a user-selected folder, often synced by third-party tools. App-private storage holds the folder grant and API keys protected by an Android Keystore key. The app has no backend, so no server can throttle guesses or reset a password. minSdk is 26.

References: [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html), [Android Keystore](https://developer.android.com/privacy-and-security/keystore), [androidx.biometric releases](https://developer.android.com/jetpack/androidx/releases/biometric), [BiometricPrompt with CryptoObject](https://developer.android.com/identity/sign-in/biometric-auth).

## Goals / Non-Goals

**Goals:** every file in the data directory unreadable without the password; daily use without typing the password; switchable both ways without data loss and with full history kept.

**Non-Goals:** password recovery, remote wipe, a lock without encryption, per-record encryption, hiding file count or sizes, plausible deniability, protection against a rooted or compromised device while unlocked, encrypting or erasing backups and synced copies outside the data directory.

## Decisions

### 1. Key hierarchy
- **Data key**: random 256-bit key per folder, encrypts snapshot payloads with AES-256-GCM, fresh 96-bit random nonce per file.
- **Password key**: Argon2id over the password with a random 16-byte salt, wraps the data key. Parameters m=46 MiB, t=1, p=1, the first OWASP-recommended setting, stored in the file header so they can be raised later. Bouncy Castle `Argon2BytesGenerator` is already a dependency.
- Every encrypted snapshot carries the wrapped data key, salt and parameters in its header, so any single file plus the password is enough to open the folder on a new device.
- Password change creates a new data key and re-encrypts every snapshot (decision 6), so the old password opens nothing afterwards.
Alternative rejected: deriving the file key directly from the password. A separate data key lets the device-bound copies unlock files without the password.
Alternative rejected: PBKDF2. It is not memory-hard; it stays the fallback only if Argon2 proves too slow on low-memory devices in testing.

### 2. File format
`{"enc":1,"id":…,"parents":[…],"schema":…,"kdf":{"alg":"argon2id","m":…,"t":…,"p":…,"salt":…},"wrapped":…,"nonce":…,"ciphertext":…}`. The clear fields `id`, `parents`, `schema`, `kdf` and `wrapped` are bound as GCM associated data, so they cannot be altered undetected. The ciphertext is the existing `{payload, sha256}` envelope. Revision ids and parents stay readable so head and conflict detection work before unlocking; they are random UUIDs and reveal only the shape of the history.

### 3. Device-bound unlock
After the password is entered once, the data key is stored in app-private storage wrapped twice: by a PIN-derived key (Argon2id, own salt) and by a non-exportable Keystore AES key. Guessing the PIN therefore has to happen on the device, where the attempt counter applies. Without a PIN nothing is stored and the password is asked at each launch.
Biometric unlock stores a second copy wrapped by a Keystore key created with `setUserAuthenticationRequired(true)` and `setInvalidatedByBiometricEnrollment(true)`, released through `BiometricPrompt` with a `CryptoObject` and `BIOMETRIC_STRONG`. Enrollment changes invalidate the key.
The lock exists only with encryption. With encryption off there is nothing to unlock, and a PIN would be a screen gate over readable files.

### 4. Attempt limits and password fallback
A counter in app-private storage is increased before each PIN check and reset on success. From 5 failures waiting times grow from 30 seconds to 1 hour. At 10 failures the PIN and the device-bound copies are deleted.
"Use password" is always on the PIN screen and is not subject to PIN waiting times. The password path derives the key with Argon2id and opens the newest snapshot header, so it needs no device-bound material.

### 5. Password delay
Each password check draws a delay of 1000 to 5000 ms from `SecureRandom`, runs the key derivation, and reports the result only when both the derivation and the delay have finished. Accept and reject take the same path, so timing does not reveal the outcome. This slows guessing through the interface; resistance against offline guessing on copied files comes from Argon2id and the password strength.

### 6. Rewriting the directory
Used for switching on, switching off and password change. Requires one head, no conflict, no missing parents, no unsaved edits.
1. Write a marker with the operation and target form to app-private storage.
2. For every snapshot, oldest first: decode, write the same revision in the target form under a new file name, read it back and compare with the decoded revision, then delete the source file.
3. Record the new mode, delete the marker.
A revision is always readable in at least one form, so an interruption loses nothing. The scanner already tolerates two files with the same revision id when their content is equal. Invalid or partial files are left untouched and reported.
Switching on shows the plaintext-backup warning first when the export record is not empty. The record lives in app-private storage and holds the time of each plaintext export.
Another device that still writes the old form produces a mixed folder. The app treats it as an unfinished rewrite and offers to finish it.
Alternative rejected: writing one new root snapshot and deleting history. The history must survive the switch.

### 7. App lock behaviour
Lock state lives in the ViewModel. `onStop` records the time; `onStart` locks when the selected time has passed. Locked state renders only the lock screen, cancels refresh and clears decrypted data from the UI state. `FLAG_SECURE` is set while encryption is on. PIN entry uses an in-app keypad with text labels and 48 dp targets, with the "Use password" link below it.

### 8. Dependency
`androidx.biometric:biometric` 1.1.0, stable since 2021 and maintained by Google. The 1.2 and 1.4 lines are alpha and are not used.

## Risks / Trade-offs

- Forgotten password means lost data → accepted; explicit warning and password confirmation.
- Earlier plaintext backups stay readable → warning window with explicit acceptance.
- Argon2id at 46 MiB may be slow or fail on low-memory devices → measure on the oldest supported device; parameters are in the header and adjustable.
- Rewriting many snapshots takes time and storage → progress display, resumable, one file at a time so extra space is one snapshot.
- Rewriting breaks the version 1 rule of immutable files → limited to the three explicit operations, each file verified before its source is removed.
- Sync tools see every file replaced and may keep old versions in their own history → stated in the switch dialog.
- A 4-digit PIN has low entropy → Keystore binding plus attempt limits; longer PINs allowed; password always available.
- The random delay annoys on every password entry → accepted; the PIN and biometry are the daily path.

## Migration Plan

Encryption is off after upgrading and the app opens without a lock; nothing changes until the user switches encryption on. Encrypted folders cannot be opened by version 1, which rejects the unknown file form without modifying it. Rollback: switch encryption off, then downgrade.

## Open Questions

- Should the lock time choices include a longer value such as 15 minutes? Proposed: no.
