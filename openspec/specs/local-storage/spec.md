# local-storage Specification

## Purpose
Portable snapshot persistence in a user-selected folder, recovery, schema conversion and sync conflicts.
## Requirements
### Requirement: Portable local records
User financial records SHALL persist in a user-selected SAF directory as versioned JSON snapshots. URI bootstrap metadata and device-bound encrypted API keys SHALL be the only necessary private persistence. App data SHALL not use automatic cloud backup or an application backend.

#### Scenario: Reopen folder
- **WHEN** a compatible installation selects an existing valid directory
- **THEN** it loads records and asks separately for missing device-bound API keys.

### Requirement: Recoverable saves
Saves SHALL create immutable unique revisions with parent IDs and checksums, validate by reopening before reporting success, serialize in-process writes, and preserve previous valid revisions.

#### Scenario: Interrupted write
- **WHEN** the process stops midway through a new snapshot
- **THEN** next startup ignores that invalid snapshot and loads the previous valid revision with a recovery notice.

### Requirement: Sync conflicts
The app SHALL detect concurrent revision heads on startup/resume and before/after saves, preserve competing data, and block new edits until explicit resolution. Resolution SHALL create a revision referencing both parents. It SHALL not select a winner using timestamps.

#### Scenario: Two devices edit offline
- **WHEN** external sync delivers sibling revisions
- **THEN** the app shows both summaries and lets the user inspect and explicitly choose a version while preserving both originals.

### Requirement: Storage failure and schema migration
Lost grants or failed writes SHALL never be shown as saved. Reconnect/save-copy SHALL be offered. Migrations SHALL preserve input revisions; unknown newer schemas SHALL be rejected without mutation. Backup/restore SHALL validate integrity and show affected records before replacement.

#### Scenario: Revoked access
- **WHEN** the directory grant disappears with unsaved edits
- **THEN** edits remain available in memory, Save reports failure, and reconnect/save-copy actions appear.

#### Scenario: Future schema
- **WHEN** a snapshot requires a newer app
- **THEN** opening stops with an upgrade message and the file stays unchanged.

