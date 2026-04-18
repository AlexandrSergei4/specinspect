## ADDED Requirements

### Requirement: System shows aggregate review progress for the active collection
The system SHALL present overall review metrics for the active collection, including total requirements, reviewed requirements, approved requirements, rejected requirements, and completion percentage.

#### Scenario: Review is partially complete
- **WHEN** the user opens the statistics view before all requirements are reviewed
- **THEN** the system shows current totals and the percentage of reviewed requirements completed so far

#### Scenario: Review is complete
- **WHEN** all requirements in the active collection have a latest review decision
- **THEN** the system shows a completed summary for the collection using the latest stored decisions

### Requirement: System reports rejection percentage by specification
The system SHALL calculate and display, for each imported specification, the number of reviewed requirements, the number of rejected requirements, and the percentage of requirements whose latest decision is rejected.

#### Scenario: Specification has rejected requirements
- **WHEN** a specification has one or more requirements whose latest decision is rejected
- **THEN** the system shows the rejected count and rejection percentage for that specification

#### Scenario: Specification has no rejected requirements
- **WHEN** all requirements in a specification have latest decisions of approved
- **THEN** the system shows a rejection percentage of zero for that specification

### Requirement: System identifies specifications with the highest mismatch level
The system SHALL allow the user to understand which specifications are least aligned by sorting or otherwise highlighting specifications with the highest rejection percentage.

#### Scenario: Multiple specifications have different rejection rates
- **WHEN** the statistics view compares more than one specification
- **THEN** the system highlights or orders results so the user can quickly identify the specifications with the highest rejection percentage
