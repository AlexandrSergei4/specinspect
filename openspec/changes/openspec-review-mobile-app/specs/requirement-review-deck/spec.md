## ADDED Requirements

### Requirement: Review deck shows one card per requirement
The system SHALL present the active review deck as a sequence of cards where each card represents exactly one Requirement from the active collection.

#### Scenario: Deck loads with imported requirements
- **WHEN** the active collection contains one or more unreviewed requirements
- **THEN** the system shows the next unreviewed Requirement as the top review card

#### Scenario: Requirement contains multiple scenarios
- **WHEN** the top review card is rendered for a Requirement that contains multiple scenarios
- **THEN** the card shows all Scenario WHEN/THEN pairs within the same card instead of splitting them into separate cards

### Requirement: Review card content follows the OpenSpec structure
The system SHALL display the specification name at the top of the card, the Requirement title as the primary summary, the Scenario WHEN/THEN content in the center, and the full Requirement description in the lower section of the card.

#### Scenario: Card is displayed to the reviewer
- **WHEN** a review card becomes active
- **THEN** the card layout shows the specification name, Requirement title, Scenario WHEN/THEN content, and Requirement description in separate readable sections

### Requirement: Swipe gestures capture binary review decisions
The system SHALL interpret a right swipe as confirmation that the Requirement is correct and a left swipe as rejection that the Requirement is not correct.

#### Scenario: User swipes right
- **WHEN** the user swipes the active review card to the right
- **THEN** the system records the Requirement as approved and advances to the next eligible card

#### Scenario: User swipes left
- **WHEN** the user swipes the active review card to the left
- **THEN** the system records the Requirement as rejected and advances to the next eligible card

### Requirement: Deck excludes cards that are not eligible for the current mode
The system SHALL omit cards from the active review deck when they do not match the currently selected review mode.

#### Scenario: User is in unreviewed mode
- **WHEN** a Requirement already has a stored review decision and the active mode is unreviewed
- **THEN** the system does not show that Requirement in the active deck

#### Scenario: No eligible cards remain
- **WHEN** the current review mode has no more eligible cards to display
- **THEN** the system exits the deck flow and offers post-review actions and statistics
