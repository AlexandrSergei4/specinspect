package com.alki.specinspect.data.repository

import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.storage.NoOpReviewPersistentStorage
import com.alki.specinspect.data.storage.ReviewPersistentStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * In-memory репозиторий статусов ревью сценариев.
 * Хранится Map<scenarioId, ReviewStatus>; отсутствие ключа = UNREVIEWED.
 */
@OptIn(ExperimentalTime::class)
class ReviewRepository(
    private val storage: ReviewPersistentStorage = NoOpReviewPersistentStorage,
) {

    private val persistedState = loadPersistedState()

    private val _statuses = MutableStateFlow<Map<String, ReviewStatus>>(persistedState.statuses)
    val statuses: StateFlow<Map<String, ReviewStatus>> = _statuses.asStateFlow()
    private val _reviewVersion = MutableStateFlow(0L)
    val reviewVersion: StateFlow<Long> = _reviewVersion.asStateFlow()
    private val scenarioReviewedAtMap = persistedState.scenarioReviewedAt.toMutableMap()
    private val requirementReviewedAtMap = persistedState.requirementReviewedAt.toMutableMap()
    private val subspecReviewedAtMap = persistedState.subspecReviewedAt.toMutableMap()

    fun statusOf(scenarioId: String): ReviewStatus =
        _statuses.value[scenarioId] ?: ReviewStatus.UNREVIEWED

    fun statusOf(scenario: Scenario): ReviewStatus = statusOf(scenario.id)

    fun reviewedScenarioCount(): Int = _statuses.value.size

    /**
     * Удобный read-only снимок: функция, которая по сценарию вернёт его статус.
     */
    fun snapshotStatusOf(): (Scenario) -> ReviewStatus {
        val map = _statuses.value
        return { sc -> map[sc.id] ?: ReviewStatus.UNREVIEWED }
    }

    fun scenarioReviewedAt(scenarioId: String): Long? = scenarioReviewedAtMap[scenarioId]

    fun requirementReviewedAt(specId: String, subspecId: String, requirementId: String): Long? =
        requirementReviewedAtMap[requirementKey(specId, subspecId, requirementId)]

    fun subspecReviewedAt(specId: String, subspecId: String): Long? =
        subspecReviewedAtMap[subspecKey(specId, subspecId)]

    fun setStatus(
        specId: String,
        subspecId: String,
        requirementId: String,
        scenarioId: String,
        status: ReviewStatus,
        trackReviewTime: Boolean = status != ReviewStatus.UNREVIEWED,
    ) {
        _statuses.value = _statuses.value.toMutableMap().apply {
            if (status == ReviewStatus.UNREVIEWED) remove(scenarioId) else put(scenarioId, status)
        }
        if (trackReviewTime) {
            val now = Clock.System.now().toEpochMilliseconds()
            scenarioReviewedAtMap[scenarioId] = now
            requirementReviewedAtMap[requirementKey(specId, subspecId, requirementId)] = now
            subspecReviewedAtMap[subspecKey(specId, subspecId)] = now
            _reviewVersion.value += 1
        }
        persistState()
    }

    private fun loadPersistedState(): PersistedReviewState =
        storage.read()
            ?.takeIf { it.isNotBlank() }
            ?.let { payload ->
                runCatching {
                    reviewRepositoryJson.decodeFromString<PersistedReviewState>(payload)
                }.getOrElse {
                    storage.clear()
                    PersistedReviewState()
                }
            }
            ?: PersistedReviewState()

    private fun persistState() {
        val state = PersistedReviewState(
            statuses = _statuses.value,
            scenarioReviewedAt = scenarioReviewedAtMap,
            requirementReviewedAt = requirementReviewedAtMap,
            subspecReviewedAt = subspecReviewedAtMap,
        )
        if (
            state.statuses.isEmpty() &&
            state.scenarioReviewedAt.isEmpty() &&
            state.requirementReviewedAt.isEmpty() &&
            state.subspecReviewedAt.isEmpty()
        ) {
            storage.clear()
            return
        }
        storage.write(reviewRepositoryJson.encodeToString(state))
    }

    private fun requirementKey(specId: String, subspecId: String, requirementId: String): String =
        "$specId::$subspecId::$requirementId"

    private fun subspecKey(specId: String, subspecId: String): String =
        "$specId::$subspecId"
}

@Serializable
private data class PersistedReviewState(
    val statuses: Map<String, ReviewStatus> = emptyMap(),
    val scenarioReviewedAt: Map<String, Long> = emptyMap(),
    val requirementReviewedAt: Map<String, Long> = emptyMap(),
    val subspecReviewedAt: Map<String, Long> = emptyMap(),
)

private val reviewRepositoryJson = Json {
    ignoreUnknownKeys = true
}
