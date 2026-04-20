package com.alki.specinspect.data.repository

import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.storage.ReviewPersistentStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ReviewRepositoryTest {

    @Test
    fun restoresPersistedReviewStateAfterRestart() {
        val storage = InMemoryReviewPersistentStorage()
        val repository = ReviewRepository(storage)

        repository.setStatus("spec-1", "sub-1", "req-1", "scenario-1", ReviewStatus.CORRECT)

        val scenarioReviewedAt = assertNotNull(repository.scenarioReviewedAt("scenario-1"))
        val requirementReviewedAt = assertNotNull(repository.requirementReviewedAt("spec-1", "sub-1", "req-1"))
        val subspecReviewedAt = assertNotNull(repository.subspecReviewedAt("spec-1", "sub-1"))

        val restoredRepository = ReviewRepository(storage)

        assertEquals(ReviewStatus.CORRECT, restoredRepository.statusOf("scenario-1"))
        assertEquals(scenarioReviewedAt, restoredRepository.scenarioReviewedAt("scenario-1"))
        assertEquals(requirementReviewedAt, restoredRepository.requirementReviewedAt("spec-1", "sub-1", "req-1"))
        assertEquals(subspecReviewedAt, restoredRepository.subspecReviewedAt("spec-1", "sub-1"))
    }

    @Test
    fun keepsLastReviewTimestampWhenUndoRestoresPreviousStatus() {
        val storage = InMemoryReviewPersistentStorage()
        val repository = ReviewRepository(storage)

        repository.setStatus("spec-1", "sub-1", "req-1", "scenario-1", ReviewStatus.CORRECT)

        val reviewedAt = assertNotNull(repository.scenarioReviewedAt("scenario-1"))

        repository.setStatus(
            "spec-1",
            "sub-1",
            "req-1",
            "scenario-1",
            ReviewStatus.UNREVIEWED,
            trackReviewTime = false,
        )

        val restoredRepository = ReviewRepository(storage)

        assertEquals(ReviewStatus.UNREVIEWED, restoredRepository.statusOf("scenario-1"))
        assertEquals(reviewedAt, restoredRepository.scenarioReviewedAt("scenario-1"))
    }
}

private class InMemoryReviewPersistentStorage : ReviewPersistentStorage {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun clear() {
        value = null
    }
}
