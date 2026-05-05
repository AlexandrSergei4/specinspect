package com.alki.specinspect.features.subspec

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.ReviewReportStrings
import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioStep
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.data.models.Subspec
import com.alki.specinspect.data.models.aggregatedStatus
import com.alki.specinspect.data.models.buildReviewReport
import com.alki.specinspect.data.models.displaySteps
import com.alki.specinspect.data.models.gitHubUrlFor
import com.alki.specinspect.data.models.requirementStats
import com.alki.specinspect.data.models.scenarioStats
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

interface SubspecDetailComponent {
    val state: StateFlow<SubspecDetailState>
    fun onBack()
    fun onStartReview()
    fun onShareReport(
        includeCorrect: Boolean,
        includeIncorrect: Boolean,
        shareTitle: String,
        reportStrings: ReviewReportStrings,
        specificationName: String,
    )
    fun onFilter(filter: StatsFilter)
    fun onListModeChange(mode: SubspecListMode)
    fun onOpenRequirement(requirementId: String)
    fun onSetScenarioStatus(requirementId: String, scenarioId: String, status: ReviewStatus)
    fun onOpenSource(url: String)
}

enum class SubspecListMode { REQUIREMENTS, SCENARIOS }

data class RequirementCardState(
    val id: String,
    val title: String,
    val description: String,
    val lastReviewedAt: Long?,
    val scenarioCount: Int,
    val scenarioStats: ReviewStats,
    val aggregatedStatus: ReviewStatus,
)

data class SubspecScenarioCardState(
    val id: String,
    val requirementId: String,
    val index: Int,
    val title: String,
    val whenText: String,
    val thenText: String,
    val steps: List<ScenarioStep> = emptyList(),
    val lastReviewedAt: Long?,
    val status: ReviewStatus,
    val sourceUrl: String?,
    val contextLabel: String,
)

data class SubspecDetailState(
    val specName: String = "",
    val isDemoSpec: Boolean = false,
    val subspecName: String = "",
    val requirementStats: ReviewStats = ReviewStats(0, 0, 0),
    val scenarioStats: ReviewStats = ReviewStats(0, 0, 0),
    val filter: StatsFilter = StatsFilter.ALL,
    val listMode: SubspecListMode = SubspecListMode.REQUIREMENTS,
    val requirements: List<RequirementCardState> = emptyList(),
    val visibleRequirements: List<RequirementCardState> = emptyList(),
    val scenarios: List<SubspecScenarioCardState> = emptyList(),
    val visibleScenarios: List<SubspecScenarioCardState> = emptyList(),
    val hasUnreviewedScenarios: Boolean = false,
)

class DefaultSubspecDetailComponent(
    componentContext: ComponentContext,
    private val specId: String,
    private val subspecId: String,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
    private val onBackCallback: () -> Unit,
    private val onStartReviewCallback: (specId: String, subspecId: String) -> Unit,
    private val onOpenRequirementCallback: (specId: String, subspecId: String, requirementId: String) -> Unit,
) : SubspecDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SubspecDetailState())
    override val state: StateFlow<SubspecDetailState> = _state.asStateFlow()

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
        val statusOf = reviewRepo.snapshotStatusOf()
        val cards = sub.requirements
            .sortedByDescending { reviewRepo.requirementReviewedAt(specId, subspecId, it.id) ?: Long.MIN_VALUE }
            .map { req: Requirement ->
                RequirementCardState(
                    id = req.id,
                    title = req.title,
                    description = req.description,
                    lastReviewedAt = reviewRepo.requirementReviewedAt(specId, subspecId, req.id),
                    scenarioCount = req.scenarios.size,
                    scenarioStats = req.stats(statusOf),
                    aggregatedStatus = req.aggregatedStatus(statusOf),
                )
            }
        var scenarioIndex = 1
        val scenarioCards = sub.requirements
            .flatMap { req ->
                req.scenarios.map { sc: Scenario ->
                    SubspecScenarioCardState(
                        id = sc.id,
                        requirementId = req.id,
                        index = scenarioIndex++,
                        title = sc.title,
                        whenText = sc.whenText,
                        thenText = sc.thenText,
                        steps = sc.displaySteps(),
                        lastReviewedAt = reviewRepo.scenarioReviewedAt(sc.id),
                        status = statusOf(sc),
                        sourceUrl = spec.gitHubUrlFor(sc),
                        contextLabel = req.title,
                    )
                }
            }
            .sortedByDescending { it.lastReviewedAt ?: Long.MIN_VALUE }
        val current = _state.value
        val scenarioStats = sub.scenarioStats(statusOf)
        _state.value = SubspecDetailState(
            specName = spec.name,
            isDemoSpec = spec.isDemo,
            subspecName = sub.name,
            requirementStats = sub.requirementStats(statusOf),
            scenarioStats = scenarioStats,
            filter = current.filter,
            listMode = current.listMode,
            requirements = cards,
            visibleRequirements = filterCards(cards, current.filter),
            scenarios = scenarioCards,
            visibleScenarios = filterScenarioCards(scenarioCards, current.filter),
            hasUnreviewedScenarios = scenarioStats.unreviewed > 0,
        )
    }

    private fun filterCards(cards: List<RequirementCardState>, filter: StatsFilter): List<RequirementCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            else -> cards.filter { it.scenarioStats.matches(filter) }
        }

    private fun filterScenarioCards(
        cards: List<SubspecScenarioCardState>,
        filter: StatsFilter,
    ): List<SubspecScenarioCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            StatsFilter.CORRECT -> cards.filter { it.status == ReviewStatus.CORRECT }
            StatsFilter.INCORRECT -> cards.filter { it.status == ReviewStatus.INCORRECT }
            StatsFilter.UNREVIEWED -> cards.filter { it.status == ReviewStatus.UNREVIEWED }
        }

    override fun onBack() = onBackCallback()
    override fun onStartReview() = onStartReviewCallback(specId, subspecId)
    override fun onShareReport(
        includeCorrect: Boolean,
        includeIncorrect: Boolean,
        shareTitle: String,
        reportStrings: ReviewReportStrings,
        specificationName: String,
    ) {
        val spec = specRepo.getById(specId) ?: return
        val report = spec.buildReviewReport(
            statusOf = reviewRepo.snapshotStatusOf(),
            subspecId = subspecId,
            includeCorrect = includeCorrect,
            includeIncorrect = includeIncorrect,
            strings = reportStrings,
            specificationName = specificationName,
        )
        scope.launch {
            ImageSharing.shareText(report, shareTitle)
        }
    }
    override fun onFilter(filter: StatsFilter) {
        _state.value = _state.value.copy(
            filter = filter,
            visibleRequirements = filterCards(_state.value.requirements, filter),
            visibleScenarios = filterScenarioCards(_state.value.scenarios, filter),
        )
    }
    override fun onListModeChange(mode: SubspecListMode) {
        _state.value = _state.value.copy(listMode = mode)
    }
    override fun onOpenRequirement(requirementId: String) =
        onOpenRequirementCallback(specId, subspecId, requirementId)

    override fun onSetScenarioStatus(requirementId: String, scenarioId: String, status: ReviewStatus) {
        reviewRepo.setStatus(specId, subspecId, requirementId, scenarioId, status)
    }

    override fun onOpenSource(url: String) {
        UrlOpener.openUrl(url)
    }
}

private fun ReviewStats.matches(filter: StatsFilter): Boolean =
    when (filter) {
        StatsFilter.ALL -> true
        StatsFilter.CORRECT -> correct > 0
        StatsFilter.INCORRECT -> incorrect > 0
        StatsFilter.UNREVIEWED -> unreviewed > 0
    }
