package com.alki.specinspect.features.requirement

import com.alki.specinspect.data.models.ReviewStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class RequirementDetailComponentTest {

    @Test
    fun keepsExistingScenarioOrderWhenStatusesChange() {
        val currentOrder = listOf(
            scenarioCard(id = "b", index = 2, reviewedAt = 200L),
            scenarioCard(id = "a", index = 1, reviewedAt = 100L),
            scenarioCard(id = "c", index = 3, reviewedAt = null),
        )
        val rebuiltCards = listOf(
            scenarioCard(id = "a", index = 1, reviewedAt = 300L, status = ReviewStatus.CORRECT),
            scenarioCard(id = "b", index = 2, reviewedAt = 400L, status = ReviewStatus.INCORRECT),
            scenarioCard(id = "c", index = 3, reviewedAt = null),
        )

        val reordered = rebuiltCards.keepCurrentOrder(currentOrder)

        assertEquals(listOf("b", "a", "c"), reordered.map { it.id })
        assertEquals(ReviewStatus.CORRECT, reordered[1].status)
        assertEquals(300L, reordered[1].lastReviewedAt)
    }

    @Test
    fun appendsNewScenarioUsingOriginalIndexWhenItWasNotInPreviousOrder() {
        val currentOrder = listOf(
            scenarioCard(id = "b", index = 2, reviewedAt = 200L),
            scenarioCard(id = "a", index = 1, reviewedAt = 100L),
        )
        val rebuiltCards = listOf(
            scenarioCard(id = "a", index = 1, reviewedAt = 300L),
            scenarioCard(id = "b", index = 2, reviewedAt = 400L),
            scenarioCard(id = "c", index = 3, reviewedAt = null),
        )

        val reordered = rebuiltCards.keepCurrentOrder(currentOrder)

        assertEquals(listOf("b", "a", "c"), reordered.map { it.id })
    }

    private fun scenarioCard(
        id: String,
        index: Int,
        reviewedAt: Long?,
        status: ReviewStatus = ReviewStatus.UNREVIEWED,
    ) = ScenarioCardState(
        id = id,
        index = index,
        title = "Scenario $id",
        whenText = "when",
        thenText = "then",
        lastReviewedAt = reviewedAt,
        status = status,
    )
}
