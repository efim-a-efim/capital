## ADDED Requirements

### Requirement: Password encryption of stored data
When encryption is on, every snapshot and every exported backup SHALL be encrypted with a key derived from the user's password, using authenticated encryption so that any modification is detected. The password SHALL never be stored. Revision id, parent ids and schema version SHALL stay readable without the key and SHALL be authenticated, so conflict detection works on encrypted folders. No financial record, name, address or amount SHALL be readable without the key.

#### Scenario: File opened outside the app
- **WHEN** an encrypted snapshot is opened in a text editor
- **THEN** no bucket, holding, goal, address or amount is readable.

#### Scenario: Tampered file
- **WHEN** one byte of an encrypted snapshot is changed
- **THEN** the snapshot is rejected as invalid and the last valid revision is used.

### Requirement: Switching encryption on
The user SHALL be able to switch encryption on at any time by choosing a password and confirming it. Before switching, the app SHALL state that a lost password cannot be recovered and SHALL offer a backup export. Switching SHALL write the current data encrypted, verify it by reading it back, and only then remove plaintext snapshot files from the folder. It SHALL warn that copies already synced elsewhere are outside the app's control. Switching SHALL be refused while a sync conflict is unresolved or unsaved edits exist.

#### Scenario: Switch on
- **WHEN** the user sets a password and confirms
- **THEN** the folder contains only encrypted snapshots and the data is unchanged in the app.

#### Scenario: Failure while switching
- **WHEN** writing or verifying the encrypted snapshot fails
- **THEN** no plaintext file is removed and encryption stays off.

### Requirement: Switching encryption off
The user SHALL be able to switch encryption off at any time after entering the password. Switching off SHALL write the current data as plaintext, verify it, and then remove encrypted snapshot files. Device-bound key material SHALL be deleted.

#### Scenario: Switch off
- **WHEN** the user enters the password and switches encryption off
- **THEN** the folder contains readable snapshots and the app no longer asks for a password.

### Requirement: Opening encrypted data
Opening a folder with encrypted snapshots SHALL ask for the password once per device, then keep a device-bound protected copy of the data key so that later launches need only the app lock, or nothing when no lock is set. A wrong password SHALL be reported without changing any file. Encrypted snapshots written by another device with the same password SHALL open.

#### Scenario: Second device
- **WHEN** an encrypted folder synced from another device is opened and the correct password is entered
- **THEN** the data loads and later launches do not ask for the password.

#### Scenario: Wrong password
- **WHEN** a wrong password is entered
- **THEN** the app reports it, stays locked and changes nothing.

### Requirement: Password change
The user SHALL be able to change the password after entering the current one. Existing snapshots SHALL remain readable in the app after the change. The app SHALL state that snapshots written before the change can still be opened with the old password, and SHALL offer to rewrite the folder with a new key.

#### Scenario: Change password
- **WHEN** the password is changed
- **THEN** the old password no longer opens the newest snapshot and the app, unlocked with the new one, reads all snapshots in the folder.

### Requirement: Backups
Backup export SHALL follow the encryption setting by default and SHALL offer an explicit plaintext export after password entry. Restore SHALL accept plaintext and encrypted backups and SHALL ask for the backup's password when needed.

#### Scenario: Restore encrypted backup
- **WHEN** an encrypted backup is selected and its password is entered
- **THEN** the restore preview shows its records before replacement.
