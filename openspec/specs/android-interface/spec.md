# android-interface Specification

## Purpose
Screens, navigation, editing flows, status messages and accessibility of the Android app.
## Requirements
### Requirement: Complete core navigation
The app SHALL provide Overview, Buckets and Goals destinations, Settings, and create/edit/detail flows defined in design.md. Overview SHALL distinguish total savings, allocated/free value, and goal targets.

#### Scenario: First launch
- **WHEN** no storage directory exists
- **THEN** onboarding offers choose/reopen folder, currency, optional provider setup, and privacy disclosure before financial records are saved.

### Requirement: Actionable editors
Editors SHALL use appropriate numeric/date inputs, show currency and percentage basis, validate fields before Save, support Cancel, and preview allocation changes. Wallet entry SHALL support paste. Destructive changes SHALL explain affected records and require confirmation.

#### Scenario: Connection editing
- **WHEN** a user chooses percent of bucket
- **THEN** the field explicitly says “% of bucket,” shows optional goal cap separately, and previews the resulting contribution.

### Requirement: Accessible restrained presentation
UI SHALL use Material 3 components, light/dark themes, at least 48dp targets, semantic labels, locale-aware amounts/dates, text status alongside color and adequate contrast. At 200% text scaling and compact widths, content SHALL remain reachable without clipped values/actions; wide layouts SHALL support rail/list-detail navigation.

#### Scenario: Large text
- **WHEN** font scale is 200%
- **THEN** amount rows reflow, actions remain reachable, and bottom navigation does not overlap content.

### Requirement: Recovery states
Every primary screen SHALL support loading, empty, cached/offline, partial-error and retry states. Storage/conflict errors SHALL expose recovery actions. Source/freshness details SHALL be reachable from affected holdings and totals.

#### Scenario: Incomplete total
- **WHEN** an unpriced holding is present
- **THEN** the overview marks the total incomplete and opens a list of affected holdings with corrective actions.


### Requirement: Interface language
The interface SHALL be available in English, Chinese, Hindi, Spanish, Arabic, French, Bengali, Portuguese, Russian, Indonesian, Urdu, German, Japanese, Marathi and Vietnamese. Settings SHALL offer a language choice with a "System default" option. Until the user chooses, the app SHALL use the device language when it is supported and English otherwise. The choice SHALL be stored on the device, not in the data folder, and SHALL apply to the lock screens. Arabic and Urdu SHALL use a right-to-left layout. Provider names, asset codes and user-entered names SHALL NOT be translated.

#### Scenario: Unsupported device language
- **WHEN** the app is installed on a device set to a language outside the list
- **THEN** the interface is shown in English.

#### Scenario: Change language
- **WHEN** the user picks a language in Settings
- **THEN** the interface switches at once and stays in that language after a restart.

### Requirement: Help button
The top bar SHALL show a help button next to refresh and settings on every main screen, including the start screen. It SHALL open the documentation site page for the screen currently shown (start, overview, buckets, bucket, goals, goal, plans, settings) in the interface language: English at the site root, other languages under their code. The site SHALL document every screen with a screenshot in that language. The bottom of Settings SHALL link to the Privacy Policy, Data safety and Financial features pages of the site in the interface language.

#### Scenario: Help from a bucket in Russian
- **WHEN** the interface language is Russian, a bucket is open and the user taps the help button
- **THEN** the browser opens `https://capital.fimych.dev/ru/screens/bucket`.
