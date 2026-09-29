## ADDED Requirements

### Requirement: Password encryption of stored data
When encryption is on, every snapshot in the data directory and every newly exported backup SHALL be encrypted with a key derived from the user's password, using authenticated encryption so that any modification is detected. The password SHALL never be stored. Revision id, parent ids and schema version SHALL stay readable without the key and SHALL be authenticated, so conflict detection works on encrypted folders. No financial record, name, address or amount SHALL be readable without the key.

#### Scenario: File opened outside the app
- **WHEN** any snapshot in an encrypted folder is opened in a text editor
- **THEN** no bucket, holding, goal, address or amount is readable.

#### Scenario: Tampered file
- **WHEN** one byte of an encrypted snapshot is changed
- **THEN** the snapshot is rejected as invalid and the last valid revision is used.

### Requirement: Switching encryption on
The user SHALL be able to switch encryption on at any time by choosing a password and confirming it. Before switching, the app SHALL state that a lost password cannot be recovered. Switching SHALL encrypt every snapshot in the data directory, including all earlier revisions, keeping revision ids and ancestry. Each encrypted file SHALL be verified by reading it back before its plaintext original is removed. Switching SHALL be refused while a sync conflict is unresolved, a sync is incomplete, or unsaved edits exist. When the switch ends, no plaintext snapshot SHALL remain in the directory.

#### Scenario: Switch on
- **WHEN** the user sets a password and confirms
- **THEN** every snapshot in the folder is encrypted, the revision history is intact and the data is unchanged in the app.

#### Scenario: Failure while switching
- **WHEN** writing or verifying an encrypted snapshot fails
- **THEN** the plaintext original of that snapshot is kept, the switch stops with a message, and it can be resumed.

#### Scenario: Interrupted switch
- **WHEN** the process stops midway through the switch
- **THEN** the next start detects the mixed folder, loads the data, and resumes the switch after the password is entered.

### Requirement: Warning about earlier plaintext backups
If at least one backup was exported without encryption, switching encryption on SHALL show a warning window stating that those backup files remain readable, that the app cannot encrypt or delete them, and when the last one was exported. The user SHALL have to accept this risk explicitly to continue; declining SHALL leave encryption off and change nothing. The window SHALL also state that copies already held by sync tools are outside the app's control.

#### Scenario: Plaintext backup exists
- **WHEN** the user exported a plaintext backup earlier and now switches encryption on
- **THEN** the warning window appears and encryption starts only after the user accepts the risk.

#### Scenario: Decline
- **WHEN** the user declines the warning
- **THEN** encryption stays off and no file is changed.

#### Scenario: No plaintext backup
- **WHEN** no backup was ever exported without encryption
- **THEN** no backup warning is shown.

### Requirement: Switching encryption off
The user SHALL be able to switch encryption off at any time after entering the password. Switching off SHALL decrypt every snapshot in the data directory, keeping revision ids and ancestry, verifying each plaintext file before its encrypted original is removed. Device-bound key material, the PIN and biometric unlock SHALL be deleted. Afterwards the app SHALL open without any lock.

#### Scenario: Switch off
- **WHEN** the user enters the password and switches encryption off
- **THEN** every snapshot in the folder is readable, and the app opens without password, PIN or biometry.

### Requirement: Opening encrypted data
Opening a folder with encrypted snapshots SHALL ask for the password once per device, then keep a device-bound protected copy of the data key so that later launches need only the app lock. A wrong password SHALL be reported without changing any file. Encrypted snapshots written by another device with the same password SHALL open.

#### Scenario: Second device
- **WHEN** an encrypted folder synced from another device is opened and the correct password is entered
- **THEN** the data loads and later launches ask for the PIN or biometry, not the password.

#### Scenario: Wrong password
- **WHEN** a wrong password is entered
- **THEN** the app reports it, stays locked and changes nothing.

### Requirement: Password entry delay
Every password check SHALL wait a random time between 1 and 5 seconds, chosen with a cryptographically secure random source, before it accepts or rejects. The delay SHALL apply equally to correct and wrong passwords and to every place a password is entered. The wait SHALL be shown as progress and SHALL not be skippable.

#### Scenario: Correct password
- **WHEN** the correct password is entered
- **THEN** the app unlocks after a wait of 1 to 5 seconds.

#### Scenario: Wrong password
- **WHEN** a wrong password is entered
- **THEN** the rejection appears after a wait of 1 to 5 seconds.

### Requirement: Password change
The user SHALL be able to change the password after entering the current one. A password change SHALL re-encrypt every snapshot in the data directory with a new data key, verifying each file before the old one is removed. Afterwards the old password SHALL open no snapshot in the directory. An interrupted change SHALL be resumable and SHALL never leave a snapshot unreadable.

#### Scenario: Change password
- **WHEN** the password is changed
- **THEN** every snapshot opens with the new password and none opens with the old one.

#### Scenario: Interrupted change
- **WHEN** the process stops midway through re-encryption
- **THEN** the next start loads the data and resumes the change after password entry.

### Requirement: Backups
While encryption is on, backup export SHALL be encrypted by default and SHALL offer an explicit plaintext export after password entry and a warning. The app SHALL record that a plaintext backup was exported and when. Restore SHALL accept plaintext and encrypted backups and SHALL ask for the backup's password when needed.

#### Scenario: Restore encrypted backup
- **WHEN** an encrypted backup is selected and its password is entered
- **THEN** the restore preview shows its records before replacement.
