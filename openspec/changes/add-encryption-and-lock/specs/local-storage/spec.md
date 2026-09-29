## MODIFIED Requirements

### Requirement: Portable local records
User financial records SHALL persist in a user-selected SAF directory as versioned JSON snapshots, all encrypted when the user has switched encryption on. URI bootstrap metadata, device-bound encrypted API keys, app-lock state, the record of plaintext backup exports and device-bound protected key material SHALL be the only necessary private persistence. App data SHALL not use automatic cloud backup or an application backend.

#### Scenario: Reopen folder
- **WHEN** a compatible installation selects an existing valid directory
- **THEN** it loads records and asks separately for missing device-bound API keys.

#### Scenario: Reopen encrypted folder
- **WHEN** a compatible installation selects a directory with encrypted snapshots
- **THEN** it asks for the password before loading records.

### Requirement: Recoverable saves
Saves SHALL create immutable unique revisions with parent IDs and checksums, validate by reopening before reporting success, serialize in-process writes, and preserve previous valid revisions. Stored snapshots SHALL be rewritten only by an explicit encryption switch or password change, which replaces each file with the same revision in the new form after that new file is written and verified. Revision ids, ancestry and content SHALL be unchanged by such a rewrite.

#### Scenario: Interrupted write
- **WHEN** the process stops midway through a new snapshot
- **THEN** next startup ignores that invalid snapshot and loads the previous valid revision with a recovery notice.

#### Scenario: Interrupted rewrite
- **WHEN** the process stops midway through an encryption switch or password change
- **THEN** next startup finds each revision readable in at least one form, loads the newest revision and offers to finish the rewrite.
