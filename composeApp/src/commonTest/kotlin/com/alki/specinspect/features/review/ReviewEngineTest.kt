package com.alki.specinspect.features.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReviewEngineTest {

    private val parser = OpenSpecParser()

    private val rawLibrary = RawImportedLibrary(
        sourceName = "Specs",
        sourceType = LibrarySourceType.Custom,
        specifications = listOf(
            RawImportedSpecification(
                folderName = "dashboard",
                markdown = """
                    ## ADDED Requirements

                    ### Requirement: Dashboard home
                    The system SHALL open dashboard first.

                    #### Scenario: App starts
                    - **WHEN** the app starts
                    - **THEN** dashboard is visible

                    ### Requirement: Empty state
                    The system SHALL show a placeholder.

                    #### Scenario: No pets
                    - **WHEN** no pets exist
                    - **THEN** an empty state is visible
                """.trimIndent()
            ),
            RawImportedSpecification(
                folderName = "events-feed",
                markdown = """
                    ## ADDED Requirements

                    ### Requirement: Last event section
                    The system SHALL show the last event.

                    #### Scenario: Event exists
                    - **WHEN** one event exists
                    - **THEN** the last event card is shown
                """.trimIndent()
            )
        )
    )

    @Test
    fun parserGroupsScenariosUnderOneRequirementCard() {
        val library = parser.parseLibrary(rawLibrary)

        assertEquals(3, library.allCards.size)
        assertEquals("Dashboard home", library.allCards.first().requirementTitle)
        assertEquals(1, library.allCards.first().scenarios.size)
    }

    @Test
    fun stableIdRemainsSameForUnchangedRequirement() {
        val first = parser.parseLibrary(rawLibrary).allCards.first().cardId
        val second = parser.parseLibrary(rawLibrary).allCards.first().cardId

        assertEquals(first, second)
    }

    @Test
    fun replayFilteringUsesLatestDecision() {
        val library = parser.parseLibrary(rawLibrary)
        val firstCard = library.allCards.first()
        val secondCard = library.allCards[1]

        val history = listOf(
            SwipeHistoryEntry(firstCard.cardId, ReviewDecision.Rejected, 1, "a"),
            SwipeHistoryEntry(secondCard.cardId, ReviewDecision.Approved, 2, "a"),
            SwipeHistoryEntry(firstCard.cardId, ReviewDecision.Approved, 3, "b")
        )

        val rejected = ReviewEngine.cardsForMode(library, history, ReviewMode.RejectedOnly)
        val approved = ReviewEngine.cardsForMode(library, history, ReviewMode.ApprovedOnly)

        assertTrue(rejected.none { it.cardId == firstCard.cardId })
        assertEquals(setOf(firstCard.cardId, secondCard.cardId), approved.map { it.cardId }.toSet())
    }

    @Test
    fun statsSortsSpecificationsByHighestMismatch() {
        val library = parser.parseLibrary(rawLibrary)
        val history = listOf(
            SwipeHistoryEntry(library.allCards[0].cardId, ReviewDecision.Rejected, 1, "a"),
            SwipeHistoryEntry(library.allCards[1].cardId, ReviewDecision.Rejected, 2, "a"),
            SwipeHistoryEntry(library.allCards[2].cardId, ReviewDecision.Approved, 3, "a")
        )

        val stats = ReviewEngine.stats(library, history)

        assertEquals(3, stats.totalCards)
        assertEquals("Dashboard", stats.specificationStats.first().displayName)
        assertEquals(1f, stats.specificationStats.first().rejectionRate)
    }
}
