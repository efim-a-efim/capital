## Why

Snapshot files are plaintext financial records in a folder that the user often syncs to other devices or a cloud service. Anyone with the phone unlocked can also open the app. Version 1 deferred encryption because unattended startup needed a key-unlock design.

## What Changes

- **Password encryption** of all snapshot and backup files. The user can switch it on or off at any time. Switching it off decrypts the data.
- **BREAKING** Switching encryption on or off rewrites the folder: the current data is written in the new form and older snapshot files in the other form are removed after verification. Version 1 never deleted snapshots.
- **App lock** with a PIN and optional biometry, independent of encryption. Locks on launch and after time in the background.
- The password is asked once per device. After that the PIN or biometry unlocks a device-bound copy of the data key.
- No password recovery. The app says so before encryption is switched on and offers a plaintext backup export first.
- Locked or encrypted content is hidden from screenshots and the recent-apps preview.

## Capabilities

### New Capabilities
- `data-encryption`: password-based encryption of snapshots and backups, switching on and off, password change, multi-device use.
- `app-lock`: PIN and biometric lock, automatic locking, attempt limits, protection of the device-bound key.

### Modified Capabilities
- `local-storage`: snapshots may be encrypted; private storage also holds lock and key material; previous revisions are preserved except during an explicit encryption switch.

## Impact

New `data/Crypto.kt` and `data/Lock.kt`; changes in `data/Snapshots.kt`, `data/FolderStore.kt`, `data/Secrets.kt`, `CapitalModel.kt`, `MainActivity.kt`, `ui/CapitalApp.kt`; tests. One new dependency: `androidx.biometric:biometric` 1.1.0, the current stable release. Argon2id comes from Bouncy Castle, which the app already ships. Files written with encryption cannot be read by version 1.
