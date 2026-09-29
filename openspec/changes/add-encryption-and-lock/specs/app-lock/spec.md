## ADDED Requirements

### Requirement: Lock only with encryption
PIN lock and biometric unlock SHALL be available only while encryption is on. While encryption is off the app SHALL open directly with no lock screen, and the PIN and biometry settings SHALL be unavailable with a text explaining why. Switching encryption off SHALL remove the PIN and biometric unlock.

#### Scenario: Encryption off
- **WHEN** encryption is off
- **THEN** the app opens directly and Settings offers no PIN or biometry.

### Requirement: PIN lock
While encryption is on, the user SHALL be able to set, change and remove an app PIN of 4 to 12 digits. With a PIN set, the app SHALL require it at launch and after being in the background longer than a user-selected time, with choices immediately, 1 minute and 5 minutes. Without a PIN, an encrypted folder SHALL require the password at each launch. While locked, no financial content SHALL be displayed and no refresh SHALL run. The PIN SHALL never be stored in readable or reversibly encrypted form.

#### Scenario: Return from background
- **WHEN** the app returns after longer than the selected time
- **THEN** the lock screen is shown and no amounts are visible until the PIN is accepted.

#### Scenario: No PIN set
- **WHEN** encryption is on and no PIN is set
- **THEN** each launch asks for the password.

### Requirement: Password fallback
The PIN screen SHALL show a "Use password" link at the bottom at all times, including while PIN entry is blocked by a waiting time and after the PIN has been disabled by too many failures. Entering the correct password SHALL unlock the app, reset the failure counter, and let the user set a new PIN.

#### Scenario: PIN blocked
- **WHEN** PIN entry is blocked by a waiting time and the user taps "Use password" and enters the correct password
- **THEN** the app unlocks and the failure counter is reset.

#### Scenario: Forgotten PIN
- **WHEN** the user has forgotten the PIN and enters the correct password
- **THEN** the app unlocks and offers to set a new PIN.

### Requirement: Attempt limits
Wrong PIN entries SHALL be counted across app restarts. After 5 wrong entries the app SHALL enforce growing waiting times for PIN entry. After 10 wrong entries the PIN and the device-bound key copies SHALL be deleted and only the password SHALL unlock the app. No data file SHALL be deleted or changed by wrong entries.

#### Scenario: Ten wrong entries
- **WHEN** a wrong PIN is entered 10 times
- **THEN** the PIN screen offers only "Use password" and the folder is unchanged.

### Requirement: Biometric unlock
When a PIN is set and the device has enrolled strong biometrics, the user SHALL be able to switch biometric unlock on. Biometric unlock SHALL release the device-bound key only through the platform's cryptographic biometric prompt. The PIN and the password SHALL always remain available as fallbacks. A change of enrolled biometrics SHALL disable biometric unlock until the PIN or password is entered again.

#### Scenario: Unlock with fingerprint
- **WHEN** biometric unlock is on and the user authenticates
- **THEN** the app unlocks without the PIN.

#### Scenario: New fingerprint enrolled
- **WHEN** a biometric is added in system settings
- **THEN** the next unlock requires the PIN or password and biometric unlock must be switched on again.

### Requirement: Screen privacy
While encryption is on, the app SHALL prevent screenshots and SHALL hide its content in the recent-apps preview.

#### Scenario: Recent apps
- **WHEN** the user opens the recent-apps view
- **THEN** the app preview shows no financial content.
