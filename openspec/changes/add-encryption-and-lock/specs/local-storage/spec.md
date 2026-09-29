## MODIFIED Requirements

### Requirement: Portable local records
User financial records SHALL persist in a user-selected SAF directory as versioned JSON snapshots, encrypted when the user has switched encryption on. URI bootstrap metadata, device-bound encrypted API keys, app-lock state and device-bound protected key material SHALL be the only necessary private persistence. App data SHALL not use automatic cloud backup or an application backend.

#### Scenario: Reopen folder
- **WHEN** a compatible installation selects an existing valid directory
- **THEN** it loads records and asks separately for missing device-bound API keys.

#### Scenario: Reopen encrypted folder
- **WHEN** a compatible installation selects a directory with encrypted snapshots
- **THEN** it asks for the password before loading records.

### Requirement: Recoverable saves
Saves SHALL create immutable unique revisions with parent IDs and checksums, validate by reopening before reporting success, serialize in-process writes, and preserve previous valid revisions. The only exception SHALL be an explicit switch of encryption on or off, which removes snapshots of the previous form after the new form is written and verified.

#### Scenario: Interrupted write
- **WHEN** the process stops midway through a new snapshot
- **THEN** next startup ignores that invalid snapshot and loads the previous valid revision with a recovery notice.

#### Scenario: Interrupted encryption switch
- **WHEN** the process stops after the new-form snapshot is written and before old-form files are removed
- **THEN** next startup loads the newest valid revision and offers to finish removing old-form files.
