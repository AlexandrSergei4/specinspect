## ADDED Requirements

### Requirement: System persists swipe history for each requirement card
The system SHALL save every swipe decision for a review card so that review progress survives app restarts and collection reloads.

#### Scenario: User reopens the app mid-review
- **WHEN** the user closes and reopens the application after reviewing some cards
- **THEN** the system restores the active collection and continues excluding already reviewed cards from unreviewed mode

#### Scenario: User reviews a card
- **WHEN** the user swipes a review card left or right
- **THEN** the system stores the decision with the corresponding card identity for later retrieval

### Requirement: Completed review offers replay filters
The system SHALL allow the user to start a new review pass using either only rejected cards or only approved cards after all cards in the current unreviewed pass have been processed.

#### Scenario: User wants to revisit rejected requirements
- **WHEN** the user chooses to review only rejected cards after completing the initial pass
- **THEN** the system starts a new deck containing only cards whose latest decision is rejected

#### Scenario: User wants to revisit approved requirements
- **WHEN** the user chooses to review only approved cards after completing the initial pass
- **THEN** the system starts a new deck containing only cards whose latest decision is approved

### Requirement: Replay mode preserves prior history while updating latest decision
The system SHALL keep historical swipe entries across replay sessions while treating the newest decision for each card as the active state for filtering and analytics.

#### Scenario: User changes a previous decision in replay mode
- **WHEN** the user swipes a previously rejected card to the right during a replay session
- **THEN** the system stores the new decision and uses the newer approved state as the current status of that card

#### Scenario: User starts multiple replay sessions
- **WHEN** the user repeats approved-only or rejected-only review sessions across time
- **THEN** the system retains the historical session records without duplicating cards in the active state model
