package com.alki.specinspect.features.review.data

import com.alki.specinspect.features.review.ImportedSpecLibrary
import com.alki.specinspect.features.review.PersistedReviewState
import com.alki.specinspect.features.review.RawImportedLibrary
import com.alki.specinspect.features.review.ReviewDecision
import com.alki.specinspect.features.review.ReviewMode
import com.alki.specinspect.features.review.SwipeHistoryEntry
import com.alki.specinspect.features.review.OpenSpecParser
import com.alki.specinspect.features.review.platform.PlatformStorage
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class ReviewStateStore(
    private val parser: OpenSpecParser = OpenSpecParser(),
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) {
    suspend fun load(): PersistedReviewState? =
        PlatformStorage.readState()?.let { stored ->
            runCatching { json.decodeFromString<PersistedReviewState>(stored) }.getOrNull()
        }

    suspend fun save(state: PersistedReviewState) {
        PlatformStorage.writeState(json.encodeToString(state))
    }

    suspend fun importLibrary(
        currentState: PersistedReviewState,
        rawLibrary: RawImportedLibrary
    ): PersistedReviewState {
        val parsed = parser.parseLibrary(rawLibrary)
        val updatedLibraries = currentState.libraries + parsed
        return currentState.copy(
            activeLibrary = parsed,
            libraries = updatedLibraries,
            currentMode = ReviewMode.Unreviewed
        )
    }

    fun recordDecision(
        state: PersistedReviewState,
        cardId: String,
        decision: ReviewDecision,
        reviewSessionId: String
    ): PersistedReviewState {
        val updated = state.swipeHistory + SwipeHistoryEntry(
            cardId = cardId,
            decision = decision,
            reviewedAtEpochMillis = Clock.System.now().toEpochMilliseconds(),
            reviewSessionId = reviewSessionId
        )
        val relatedScenarioIds = state.activeLibrary
            ?.allCards
            ?.firstOrNull { it.cardId == cardId }
            ?.scenarios
            .orEmpty()
            .map { it.scenarioId }

        val updatedScenarioDecisions = buildMap {
            putAll(state.scenarioDecisions)
            relatedScenarioIds.forEach { scenarioId ->
                put(scenarioId, decision)
            }
        }

        return state.copy(
            swipeHistory = updated,
            scenarioDecisions = updatedScenarioDecisions
        )
    }
}
