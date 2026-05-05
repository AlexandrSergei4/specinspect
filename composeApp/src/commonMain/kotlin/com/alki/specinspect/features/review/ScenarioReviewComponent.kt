package com.alki.specinspect.features.review

import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioStep
import com.alki.specinspect.data.models.displaySteps
import com.alki.specinspect.data.models.gitHubUrlFor
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Область ревью — сужает выборку сценариев.
 */
@Serializable
sealed interface ReviewScope {
    @Serializable
    data class Spec(val specId: String) : ReviewScope

    @Serializable
    data class Subspec(val specId: String, val subspecId: String) : ReviewScope

    @Serializable
    data class Requirement(val specId: String, val subspecId: String, val requirementId: String) : ReviewScope
}

interface ScenarioReviewComponent {
    val state: StateFlow<ScenarioReviewState>
    fun onBack()
    fun onSwipe(status: ReviewStatus)
    fun onUndo()
    fun onOpenSource(url: String)
}

data class ReviewCardState(
    val id: String,
    val subspecId: String,
    val requirementId: String,
    val title: String,
    val whenText: String,
    val thenText: String,
    val steps: List<ScenarioStep> = emptyList(),
    val requirementText: String,
    val sourcePath: String? = null,
    val sourceLine: Int? = null,
    val sourceUrl: String? = null,
)

private data class ReviewUndoEntry(
    val scenarioId: String,
    val subspecId: String,
    val requirementId: String,
    val previousStatus: ReviewStatus,
)

private data class ReviewSource(
    val scenario: Scenario,
    val subspecId: String,
    val requirementId: String,
    val requirementText: String,
    val sourceUrl: String? = null,
)

data class ScenarioReviewState(
    val title: String = "",
    val isDemoSpec: Boolean = false,
    val cards: List<ReviewCardState> = emptyList(),
    val currentIndex: Int = 0,
    val total: Int = 0,
    val canUndo: Boolean = false,
    val finished: Boolean = false,
)

class DefaultScenarioReviewComponent(
    componentContext: ComponentContext,
    private val scope: ReviewScope,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
    private val onBackCallback: () -> Unit,
) : ScenarioReviewComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(ScenarioReviewState())
    override val state: StateFlow<ScenarioReviewState> = _state.asStateFlow()

    private val undoStack = ArrayDeque<ReviewUndoEntry>()

    init {
        loadCards()
    }

    private fun loadCards() {
        val spec = specRepo.getById(scopeSpecId()) ?: run { onBackCallback(); return }
        val statusOf = reviewRepo.snapshotStatusOf()

        // собираем все сценарии в области
        val pool: List<ReviewSource> = when (scope) {
            is ReviewScope.Spec ->
                spec.subspecs.flatMap { sub ->
                    sub.requirements.flatMap { req ->
                        req.scenarios.map {
                            ReviewSource(
                                scenario = it,
                                subspecId = sub.id,
                                requirementId = req.id,
                                requirementText = req.description,
                                sourceUrl = spec.gitHubUrlFor(it),
                            )
                        }
                    }
                }
            is ReviewScope.Subspec ->
                spec.subspecs.firstOrNull { it.id == scope.subspecId }
                    ?.requirements?.flatMap { req ->
                        req.scenarios.map {
                            ReviewSource(
                                scenario = it,
                                subspecId = scope.subspecId,
                                requirementId = req.id,
                                requirementText = req.description,
                                sourceUrl = spec.gitHubUrlFor(it),
                            )
                        }
                    } ?: emptyList()
            is ReviewScope.Requirement -> {
                val req = spec.subspecs.firstOrNull { it.id == scope.subspecId }
                    ?.requirements?.firstOrNull { it.id == scope.requirementId }
                req?.scenarios?.map {
                    ReviewSource(
                        scenario = it,
                        subspecId = scope.subspecId,
                        requirementId = scope.requirementId,
                        requirementText = req.description,
                        sourceUrl = spec.gitHubUrlFor(it),
                    )
                } ?: emptyList()
            }
        }
        // оставляем только непросмотренные и перемешиваем
        val unreviewed = pool.filter { statusOf(it.scenario) == ReviewStatus.UNREVIEWED }.shuffled()

        if (unreviewed.isEmpty()) {
            onBackCallback()
            return
        }

        _state.value = ScenarioReviewState(
            title = spec.name,
            isDemoSpec = spec.isDemo,
            cards = unreviewed.map { source ->
                ReviewCardState(
                    id = source.scenario.id,
                    subspecId = source.subspecId,
                    requirementId = source.requirementId,
                    title = source.scenario.title,
                    whenText = source.scenario.whenText,
                    thenText = source.scenario.thenText,
                    steps = source.scenario.displaySteps(),
                    requirementText = source.requirementText,
                    sourcePath = source.scenario.source?.filePath,
                    sourceLine = source.scenario.source?.line,
                    sourceUrl = source.sourceUrl,
                )
            },
            currentIndex = 0,
            total = unreviewed.size,
            canUndo = false,
            finished = false,
        )
    }

    private fun scopeSpecId(): String = when (scope) {
        is ReviewScope.Spec -> scope.specId
        is ReviewScope.Subspec -> scope.specId
        is ReviewScope.Requirement -> scope.specId
    }

    override fun onBack() = onBackCallback()

    override fun onSwipe(status: ReviewStatus) {
        val s = _state.value
        if (s.finished) return
        val card = s.cards.getOrNull(s.currentIndex) ?: return
        val previous = reviewRepo.statusOf(card.id)
        reviewRepo.setStatus(scopeSpecId(), card.subspecId, card.requirementId, card.id, status)
        undoStack.addLast(
            ReviewUndoEntry(
                scenarioId = card.id,
                subspecId = card.subspecId,
                requirementId = card.requirementId,
                previousStatus = previous,
            )
        )
        val nextIndex = s.currentIndex + 1
        if (nextIndex >= s.cards.size) {
            _state.value = s.copy(currentIndex = nextIndex, canUndo = true, finished = true)
            onBackCallback()
        } else {
            _state.value = s.copy(currentIndex = nextIndex, canUndo = true)
        }
    }

    override fun onUndo() {
        val s = _state.value
        if (undoStack.isEmpty()) return
        val entry = undoStack.removeLast()
        reviewRepo.setStatus(
            scopeSpecId(),
            entry.subspecId,
            entry.requirementId,
            entry.scenarioId,
            entry.previousStatus,
            trackReviewTime = false,
        )
        val newIndex = (s.currentIndex - 1).coerceAtLeast(0)
        _state.value = s.copy(currentIndex = newIndex, canUndo = undoStack.isNotEmpty(), finished = false)
    }

    override fun onOpenSource(url: String) {
        UrlOpener.openUrl(url)
    }
}
