## Why

Snapshot files are plaintext financial records in a folder that the user often syncs to other devices or a cloud service. Anyone with the phone unlocked can also open the app. Version 1 deferred encryption because unattended startup needed a key-unlock design.

## What Changes

- **Password encryption** of the whole data directory. The user can switch it on or off at any time.
- **BREAKING** Switching encryption on encrypts every snapshot in the folder; switching it off decrypts every snapshot. A password change re-encrypts every snapshot. Files are replaced after verification. Version 1 never rewrote or deleted snapshots.
- Backups exported earlier without encryption stay readable. Switching encryption on shows a warning window about them, and the user must accept that risk to continue.
- **App lock** with a PIN and optional biometry. It exists only while encryption is on. With encryption off the app simply opens.
- The PIN screen always has a "Use password" link at the bottom. It works even while the PIN is locked after too many failures.
- The password is the last line of defence. Every password check waits a random 1 to 5 seconds before it accepts or rejects.
- No password recovery. The app says so before encryption is switched on.
- Content is hidden from screenshots and the recent-apps preview while encryption is on.

## Capabilities

### New Capabilities
- `data-encryption`: password-based encryption of all snapshots and backups, switching on and off, password change with re-encryption, password entry delay, multi-device use.
- `app-lock`: PIN and biometric lock tied to encryption, automatic locking, attempt limits, password fallback, protection of the device-bound key.

### Modified Capabilities
- `local-storage`: snapshots may be encrypted; private storage also holds lock and key material; stored snapshots are rewritten only by an encryption switch or password change.

## Impact

New `data/Crypto.kt` and `data/Lock.kt`; changes in `data/Snapshots.kt`, `data/FolderStore.kt`, `data/Secrets.kt`, `CapitalModel.kt`, `MainActivity.kt`, `ui/CapitalApp.kt`; tests. One new dependency: `androidx.biometric:biometric` 1.1.0, the current stable release. Argon2id comes from Bouncy Castle, which the app already ships. Files written with encryption cannot be read by version 1.
