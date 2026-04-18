## ADDED Requirements

### Requirement: User can import a collection of OpenSpec specifications from a folder
The system SHALL allow the user to select a root folder that contains one or more specification subfolders, where each subfolder is treated as a single OpenSpec specification when it contains a `spec.md` file.

#### Scenario: Folder contains multiple specification subfolders
- **WHEN** the user selects a root folder that contains one or more child folders with a `spec.md` file
- **THEN** the system imports each matching child folder as a separate specification in the active collection

#### Scenario: Folder contains unsupported entries
- **WHEN** the selected root folder contains files or child folders without a `spec.md` file
- **THEN** the system ignores unsupported entries without preventing import of valid specifications

### Requirement: System parses imported OpenSpec files into reviewable requirements
The system SHALL parse each imported `spec.md` file into requirement cards by extracting every Requirement title, its descriptive body, and all nested Scenario blocks with WHEN and THEN statements.

#### Scenario: Requirement contains multiple scenarios
- **WHEN** an imported Requirement contains more than one Scenario block
- **THEN** the system creates one review card for that Requirement and attaches all parsed Scenario WHEN/THEN pairs to the same card

#### Scenario: Imported file contains valid OpenSpec requirement structure
- **WHEN** the imported `spec.md` file includes Requirement and Scenario sections in OpenSpec markdown format
- **THEN** the system makes each parsed Requirement available in the review deck

### Requirement: System replaces the active imported collection on new import
The system SHALL treat each new folder import as the latest active collection while preserving previously stored swipe decisions for requirement cards whose identity has not changed.

#### Scenario: User imports an updated version of the same collection
- **WHEN** the user imports a new folder and a Requirement matches a previously reviewed card identity
- **THEN** the system restores the saved review decision for that card in the new active collection

#### Scenario: User imports a different collection
- **WHEN** the user imports a new folder whose requirements do not match previously reviewed card identities
- **THEN** the system treats those requirements as unreviewed cards in the new active collection
