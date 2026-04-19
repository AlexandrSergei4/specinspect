## ADDED Requirements

### Requirement: Dashboard is the default home screen when pets exist
The system SHALL open the Dashboard as the default tab when the user has at least one pet in the active location.

#### Scenario: User has pets
- **WHEN** app starts and at least one pet exists in the active location
- **THEN** system opens the Dashboard tab as the active screen

#### Scenario: User has no pets
- **WHEN** app starts and no pets exist in the active location
- **THEN** system opens the Pets tab as the active screen

### Requirement: No pets placeholder on Dashboard
The system SHALL display a placeholder prompting the user to add a pet when no pets exist.

#### Scenario: No pets in active location
- **WHEN** Dashboard is open and no pets exist
- **THEN** system shows an empty state with a prompt to add a pet

### Requirement: Last event section
The system SHALL display the most recent logged event as a card in a "Last Event" section.

#### Scenario: A recent event exists
- **WHEN** at least one event has been logged
- **THEN** Dashboard shows a "Last Event" section with a single event card identical to the Events Feed card format

#### Scenario: User taps the Last Event section
- **WHEN** user taps the Last Event section or card
- **THEN** system navigates to the Events Feed screen

### Requirement: Upcoming event section
The system SHALL display the next predicted event as a card in an "Upcoming Event" section.

#### Scenario: Upcoming event can be predicted
- **WHEN** there is sufficient event history to predict the next occurrence
- **THEN** Dashboard shows an "Upcoming Event" section with pet photo, event type, previous event date, and predicted next date

#### Scenario: User taps the Upcoming Event section
- **WHEN** user taps the Upcoming Event section or card
- **THEN** system navigates to a screen showing all upcoming events

### Requirement: Quick-add event buttons
The system SHALL display quick-add buttons for the 5 most frequently logged event types plus a generic "Add Event" button.

#### Scenario: User taps a quick-add event type button
- **WHEN** user taps one of the 5 quick-add buttons
- **THEN** system opens the Add Event wizard with that event type pre-selected (skipping step 2)

#### Scenario: User taps generic Add Event button
- **WHEN** user taps the generic "+" or "Add Event" button
- **THEN** system opens the Add Event wizard at step 1 (select pet)
