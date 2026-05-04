package com.alki.specinspect.data.demo

import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.openspec.OpenSpecParser

/**
 * Демо-спецификация, поставляемая с приложением.
 * Состоит из трёх subspec, описывающих фичу мэтчинга по ценностям.
 */
object DemoSpecification {
    const val ID = "demo-spec"
    const val NAME = ID

    fun build(): Specification {
        val subspecs = listOf(
            OpenSpecParser.parseSubspec(
                name = "questionnaire-onboarding",
                content = QUESTIONNAIRE_ONBOARDING,
                idPrefix = "$ID::questionnaire-onboarding",
            ),
            OpenSpecParser.parseSubspec(
                name = "answer-review-and-confirmation",
                content = ANSWER_REVIEW,
                idPrefix = "$ID::answer-review-and-confirmation",
            ),
            OpenSpecParser.parseSubspec(
                name = "dual-mode-match-discovery",
                content = DUAL_MODE_DISCOVERY,
                idPrefix = "$ID::dual-mode-match-discovery",
            ),
        )
        return Specification(
            id = ID,
            name = NAME,
            isDemo = true,
            subspecs = subspecs,
        )
    }

    private val QUESTIONNAIRE_ONBOARDING = """
## ADDED Requirements

### Requirement: Questionnaire-first onboarding
The system SHALL present an onboarding flow that introduces value-based matching, explains similar and opposite discovery modes, and collects questionnaire answers before discovery is enabled.

#### Scenario: User starts onboarding
- **WHEN** a new user opens onboarding
- **THEN** the app shows the concept summary and begins questionnaire progression with visible progress

### Requirement: Minimum-answer completion gate
The system SHALL block onboarding completion until the user provides at least the configured minimum number of valid answers.

#### Scenario: User attempts to continue with too few answers
- **WHEN** the user taps continue while answer count is below the minimum threshold
- **THEN** the app prevents completion and shows guidance to answer more questions

### Requirement: Mini-profile constraints without avatars
The system SHALL allow profile self-description using only up to 5 words and up to 5 emojis, and SHALL NOT allow avatar upload or avatar display.

#### Scenario: User exceeds profile limits
- **WHEN** the user enters more than 5 words or more than 5 emojis
- **THEN** the app blocks submission and indicates the specific limit violation
""".trimIndent()

    private val ANSWER_REVIEW = """
## ADDED Requirements

### Requirement: Thematic answer review
The system SHALL provide a pre-discovery review screen that summarizes selected answers grouped by themes (relationships, lifestyle, values, communication, future plans).

#### Scenario: User opens answer review
- **WHEN** onboarding answers are completed
- **THEN** the app displays grouped summaries of the user's selected positions by theme

### Requirement: Editable answers before confirmation
The system SHALL allow users to return to questionnaire editing from the review screen and then return to review with updated summaries.

#### Scenario: User edits an answer from review
- **WHEN** the user taps edit answers and changes at least one response
- **THEN** the review screen reflects updated positions after returning

### Requirement: Review includes mini-profile preview and mode preview
The system SHALL display the user's 5-word and 5-emoji profile entries and SHALL provide a preview control for both discovery modes before confirmation.

#### Scenario: User previews matching mode options
- **WHEN** the user toggles between similar and opposite previews on review
- **THEN** the app updates the explanatory text to reflect the selected mode behavior
""".trimIndent()

    private val DUAL_MODE_DISCOVERY = """
## ADDED Requirements

### Requirement: Dual-mode candidate discovery
The system SHALL provide two selectable discovery modes: similar for closest-answer candidates and opposite for most contrasting-answer candidates.

#### Scenario: User switches discovery mode
- **WHEN** the user changes mode from similar to opposite (or opposite to similar)
- **THEN** the feed refreshes candidate ranking according to the selected mode

### Requirement: Minimal profile card composition
The system SHALL render each candidate card using only 5 words, 5 emojis, compatibility indicator, and concise reason text, without showing photos.

#### Scenario: Candidate card is displayed
- **WHEN** a candidate appears in discovery
- **THEN** the card shows constrained profile fields and rationale text with no avatar or photo elements

### Requirement: Candidate interaction actions
The system SHALL support like/interest, skip, and compatibility-detail actions on each candidate card.

#### Scenario: User likes a candidate
- **WHEN** the user performs like/interest on a card
- **THEN** the system records the reaction and advances the feed to the next candidate
""".trimIndent()
}
