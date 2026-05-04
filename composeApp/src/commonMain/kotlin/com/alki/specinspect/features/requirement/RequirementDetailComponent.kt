package com.alki.specinspect.features.requirement

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.data.models.Subspec
import com.alki.specinspect.data.models.buildReviewReport
import com.alki.specinspect.data.models.gitHubUrlFor
import com.alki.specinspect.data.models.stats
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.util.ImageSharing
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface RequirementDetailComponent {
    val state: StateFlow<RequirementDetailState>
    fun onBack()
    fun onStartReview()
    fun onShareReport(includeCorrect: Boolean, includeIncorrect: Boolean)
    fun onFilter(filter: StatsFilter)
    fun onSetStatus(scenarioId: String, status: ReviewStatus)
    fun onOpenSource(url: String)
}

data class ScenarioCardState(
    val id: String,
    val index: Int,
    val title: String,
    val whenText: String,
    val thenText: String,
    val lastReviewedAt: Long?,
    val status: ReviewStatus,
    val sourceUrl: String? = null,
)

data class RequirementDetailState(
    val breadcrumb: String = "",
    val title: String = "",
    val description: String = "",
    val stats: ReviewStats = ReviewStats(0, 0, 0),
    val filter: StatsFilter = StatsFilter.ALL,
    val scenarios: List<ScenarioCardState> = emptyList(),
    val visibleScenarios: List<ScenarioCardState> = emptyList(),
    val hasUnreviewedScenarios: Boolean = false,
)

class DefaultRequirementDetailComponent(
    componentContext: ComponentContext,
    private val specId: String,
    private val subspecId: String,
    private val requirementId: String,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
    private val onBackCallback: () -> Unit,
    private val onStartReviewCallback: (specId: String, subspecId: String, requirementId: String) -> Unit,
) : RequirementDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(RequirementDetailState())
    override val state: StateFlow<RequirementDetailState> = _state.asStateFlow()

    init {
        combine(specRepo.specifications, reviewRepo.statuses, reviewRepo.reviewVersion) { _, _, _ -> Unit }
            .onEach { rebuild() }
            .launchIn(scope)
        rebuild()
        lifecycle.doOnResume { rebuild() }
    }

    private fun rebuild() {
        val spec: Specification = specRepo.getById(specId) ?: return
        val sub: Subspec = spec.subspecs.firstOrNull { it.id == subspecId } ?: return
        val req: Requirement = sub.requirements.firstOrNull { it.id == requirementId } ?: return
        val statusOf = reviewRepo.snapshotStatusOf()
        val rebuiltCards = req.scenarios.mapIndexed { idx, sc: Scenario ->
            ScenarioCardState(
                id = sc.id,
                index = idx + 1,
                title = sc.title,
                whenText = sc.whenText,
                thenText = sc.thenText,
                lastReviewedAt = reviewRepo.scenarioReviewedAt(sc.id),
                status = statusOf(sc),
                sourceUrl = spec.gitHubUrlFor(sc),
            )
        }
        val current = _state.value
        val cards = if (current.scenarios.isEmpty()) {
            rebuiltCards.sortedByDescending { it.lastReviewedAt ?: Long.MIN_VALUE }
        } else {
            rebuiltCards.keepCurrentOrder(current.scenarios)
        }
        _state.value = RequirementDetailState(
            breadcrumb = "${spec.name} / ${sub.name}",
            title = req.title,
            description = req.description,
            stats = req.stats(statusOf),
            filter = current.filter,
            scenarios = cards,
            visibleScenarios = filterCards(cards, current.filter),
            hasUnreviewedScenarios = cards.any { it.status == ReviewStatus.UNREVIEWED },
        )
    }

    private fun filterCards(cards: List<ScenarioCardState>, filter: StatsFilter): List<ScenarioCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            StatsFilter.CORRECT -> cards.filter { it.status == ReviewStatus.CORRECT }
            StatsFilter.INCORRECT -> cards.filter { it.status == ReviewStatus.INCORRECT }
            StatsFilter.UNREVIEWED -> cards.filter { it.status == ReviewStatus.UNREVIEWED }
        }

    override fun onBack() = onBackCallback()
    override fun onStartReview() = onStartReviewCallback(specId, subspecId, requirementId)
    override fun onShareReport(includeCorrect: Boolean, includeIncorrect: Boolean) {
        val spec = specRepo.getById(specId) ?: return
        val report = spec.buildReviewReport(
            statusOf = reviewRepo.snapshotStatusOf(),
            subspecId = subspecId,
            requirementId = requirementId,
            includeCorrect = includeCorrect,
            includeIncorrect = includeIncorrect,
        )
        scope.launch {
            ImageSharing.shareText(report, "Поделиться отчётом")
        }
    }
    override fun onFilter(filter: StatsFilter) {
        _state.value = _state.value.copy(
            filter = filter,
            visibleScenarios = filterCards(_state.value.scenarios, filter),
        )
    }
    override fun onSetStatus(scenarioId: String, status: ReviewStatus) {
        reviewRepo.setStatus(specId, subspecId, requirementId, scenarioId, status)
    }

    override fun onOpenSource(url: String) {
        UrlOpener.openUrl(url)
    }
}

internal fun List<ScenarioCardState>.keepCurrentOrder(
    currentOrder: List<ScenarioCardState>,
): List<ScenarioCardState> {
    if (currentOrder.isEmpty()) return this

    val positions = currentOrder.mapIndexed { index, card -> card.id to index }.toMap()
    return sortedWith(
        compareBy<ScenarioCardState> { positions[it.id] ?: Int.MAX_VALUE }
            .thenBy { it.index },
    )
}
