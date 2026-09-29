## ADDED Requirements

### Requirement: PIN lock
The user SHALL be able to set, change and remove an app PIN of 4 to 12 digits. With a PIN set, the app SHALL require it at launch and after being in the background longer than a user-selected time, with choices immediately, 1 minute and 5 minutes. While locked, no financial content SHALL be displayed and no refresh SHALL run. The PIN SHALL never be stored in readable or reversibly encrypted form.

#### Scenario: Return from background
- **WHEN** the app returns after longer than the selected time
- **THEN** the lock screen is shown and no amounts are visible until the PIN is accepted.

#### Scenario: Remove PIN
- **WHEN** the user enters the PIN and removes it
- **THEN** the app opens without a lock screen.

### Requirement: Attempt limits
Wrong PIN entries SHALL be counted across app restarts. After 5 wrong entries the app SHALL enforce growing waiting times. After 10 wrong entries the device-bound key copy SHALL be deleted and, when encryption is on, the password SHALL be required. No data file SHALL be deleted by wrong entries.

#### Scenario: Ten wrong entries
- **WHEN** a wrong PIN is entered 10 times
- **THEN** the app asks for the encryption password and the folder is unchanged.

### Requirement: Biometric unlock
When a PIN is set and the device has enrolled strong biometrics, the user SHALL be able to switch biometric unlock on. Biometric unlock SHALL release the device-bound key only through the platform's cryptographic biometric prompt. The PIN SHALL always remain available as the fallback. A change of enrolled biometrics SHALL disable biometric unlock until the PIN is entered again.

#### Scenario: Unlock with fingerprint
- **WHEN** biometric unlock is on and the user authenticates
- **THEN** the app unlocks without the PIN.

#### Scenario: New fingerprint enrolled
- **WHEN** a biometric is added in system settings
- **THEN** the next unlock requires the PIN and biometric unlock must be switched on again.

### Requirement: Lock without encryption
The app lock SHALL work with encryption off. The app SHALL state that a lock without encryption protects the app screen only and that the files in the folder remain readable.

#### Scenario: Lock only
- **WHEN** a PIN is set and encryption is off
- **THEN** the lock screen appears and Settings states that files are not encrypted.

### Requirement: Screen privacy
While a PIN is set or encryption is on, the app SHALL prevent screenshots and SHALL hide its content in the recent-apps preview.

#### Scenario: Recent apps
- **WHEN** the user opens the recent-apps view
- **THEN** the app preview shows no financial content.
