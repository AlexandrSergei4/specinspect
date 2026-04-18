## ADDED Requirements

### Requirement: Demo specification is available before the first import
The system SHALL provide a built-in demo specification collection when the user has not yet imported a custom specifications folder.

#### Scenario: User opens the app for the first time
- **WHEN** no custom specification collection has been imported yet
- **THEN** the system makes a demo specification collection available for immediate review

### Requirement: Demo collection uses the same review pipeline as imported collections
The system SHALL parse and present the demo specification using the same card generation, swipe handling, filtering, and statistics logic used for user-imported collections.

#### Scenario: User reviews the demo collection
- **WHEN** the user starts reviewing the built-in demo specification
- **THEN** the system shows review cards, saves decisions, and calculates statistics using the same behaviors as a regular imported collection

### Requirement: User can replace demo content with an imported collection
The system SHALL allow the user to import a custom specifications folder after using the demo collection and switch the active collection to the imported data.

#### Scenario: User imports custom specifications after trying the demo
- **WHEN** the user selects a custom specifications folder while the demo collection is active
- **THEN** the system replaces the active demo collection with the imported collection and uses the imported data for subsequent review flows
