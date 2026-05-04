package com.alki.specinspect.features.spec

import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.ReviewReportStrings
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.data.models.aggregatedStatus
import com.alki.specinspect.data.models.buildReviewReport
import com.alki.specinspect.data.models.gitHubUrlFor
import com.alki.specinspect.data.models.requirementStats
import com.alki.specinspect.data.models.scenarioStats
import com.alki.specinspect.data.models.stats
import com.alki.specinspect.data.models.subspecStats
import com.alki.specinspect.data.models.totalScenarios
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

interface SpecDetailComponent {
    val state: StateFlow<SpecDetailState>
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
    fun onListModeChange(mode: SpecListMode)
    fun onOpenSubspec(subspecId: String)
    fun onOpenRequirement(subspecId: String, requirementId: String)
    fun onSetScenarioStatus(subspecId: String, requirementId: String, scenarioId: String, status: ReviewStatus)
    fun onOpenSource(url: String)
}

enum class SpecListMode { SUBSPECS, REQUIREMENTS, SCENARIOS }

data class SubspecCardState(
    val id: String,
    val name: String,
    val lastReviewedAt: Long?,
    val requirementCount: Int,
    val scenarioCount: Int,
    val requirementStats: ReviewStats,
    val scenarioStats: ReviewStats,
    val aggregatedStatus: ReviewStatus,
)

data class SpecRequirementCardState(
    val id: String,
    val subspecId: String,
    val subspecName: String,
    val title: String,
    val description: String,
    val lastReviewedAt: Long?,
    val scenarioCount: Int,
    val scenarioStats: ReviewStats,
    val aggregatedStatus: ReviewStatus,
)

data class SpecScenarioCardState(
    val id: String,
    val subspecId: String,
    val requirementId: String,
    val index: Int,
    val title: String,
    val whenText: String,
    val thenText: String,
    val lastReviewedAt: Long?,
    val status: ReviewStatus,
    val sourceUrl: String?,
    val contextSubspecName: String,
    val contextRequirementTitle: String,
)

data class SpecDetailState(
    val specName: String = "",
    val isDemoSpec: Boolean = false,
    val subspecCount: Int = 0,
    val subspecStats: ReviewStats = ReviewStats(0, 0, 0),
    val scenarioStats: ReviewStats = ReviewStats(0, 0, 0),
    val filter: StatsFilter = StatsFilter.ALL,
    val listMode: SpecListMode = SpecListMode.SUBSPECS,
    val subspecs: List<SubspecCardState> = emptyList(),
    val visibleSubspecs: List<SubspecCardState> = emptyList(),
    val requirements: List<SpecRequirementCardState> = emptyList(),
    val visibleRequirements: List<SpecRequirementCardState> = emptyList(),
    val scenarios: List<SpecScenarioCardState> = emptyList(),
    val visibleScenarios: List<SpecScenarioCardState> = emptyList(),
    val hasUnreviewedScenarios: Boolean = false,
)

class DefaultSpecDetailComponent(
    componentContext: ComponentContext,
    private val specId: String,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
    private val onBackCallback: () -> Unit,
    private val onStartReviewCallback: (String) -> Unit,
    private val onOpenSubspecCallback: (String, String) -> Unit,
    private val onOpenRequirementCallback: (String, String, String) -> Unit,
) : SpecDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SpecDetailState())
    override val state: StateFlow<SpecDetailState> = _state.asStateFlow()

    init {
        // Перерисовка при изменении статусов ревью или списка спецификаций
        combine(specRepo.specifications, reviewRepo.statuses, reviewRepo.reviewVersion) { _, _, _ -> Unit }
            .onEach { rebuild() }
            .launchIn(scope)
        rebuild()
        lifecycle.doOnResume { rebuild() }
    }

    private fun rebuild() {
        val spec: Specification = specRepo.getById(specId) ?: return
        val statusOf = reviewRepo.snapshotStatusOf()
        val cards = spec.subspecs
            .sortedByDescending { reviewRepo.subspecReviewedAt(specId, it.id) ?: Long.MIN_VALUE }
            .map { sub ->
                SubspecCardState(
                    id = sub.id,
                    name = sub.name,
                    lastReviewedAt = reviewRepo.subspecReviewedAt(specId, sub.id),
                    requirementCount = sub.requirements.size,
                    scenarioCount = sub.totalScenarios(),
                    requirementStats = sub.requirementStats(statusOf),
                    scenarioStats = sub.scenarioStats(statusOf),
                    aggregatedStatus = sub.aggregatedStatus(statusOf),
                )
            }
        val requirementCards = spec.subspecs
            .flatMap { sub ->
                sub.requirements.map { req ->
                    SpecRequirementCardState(
                        id = req.id,
                        subspecId = sub.id,
                        subspecName = sub.name,
                        title = req.title,
                        description = req.description,
                        lastReviewedAt = reviewRepo.requirementReviewedAt(specId, sub.id, req.id),
                        scenarioCount = req.scenarios.size,
                        scenarioStats = req.stats(statusOf),
                        aggregatedStatus = req.aggregatedStatus(statusOf),
                    )
                }
            }
            .sortedByDescending { it.lastReviewedAt ?: Long.MIN_VALUE }
        var scenarioIndex = 1
        val scenarioCards = spec.subspecs
            .flatMap { sub ->
                sub.requirements.flatMap { req ->
                    req.scenarios.map { sc: Scenario ->
                        SpecScenarioCardState(
                            id = sc.id,
                            subspecId = sub.id,
                            requirementId = req.id,
                            index = scenarioIndex++,
                            title = sc.title,
                            whenText = sc.whenText,
                            thenText = sc.thenText,
                            lastReviewedAt = reviewRepo.scenarioReviewedAt(sc.id),
                            status = statusOf(sc),
                            sourceUrl = spec.gitHubUrlFor(sc),
                            contextSubspecName = sub.name,
                            contextRequirementTitle = req.title,
                        )
                    }
                }
            }
            .sortedByDescending { it.lastReviewedAt ?: Long.MIN_VALUE }
        val current = _state.value
        val scenarioStats = spec.scenarioStats(statusOf)
        _state.value = SpecDetailState(
            specName = spec.name,
            isDemoSpec = spec.isDemo,
            subspecCount = spec.subspecs.size,
            subspecStats = spec.subspecStats(statusOf),
            scenarioStats = scenarioStats,
            filter = current.filter,
            listMode = current.listMode,
            subspecs = cards,
            visibleSubspecs = filterCards(cards, current.filter),
            requirements = requirementCards,
            visibleRequirements = filterRequirementCards(requirementCards, current.filter),
            scenarios = scenarioCards,
            visibleScenarios = filterScenarioCards(scenarioCards, current.filter),
            hasUnreviewedScenarios = scenarioStats.unreviewed > 0,
        )
    }

    private fun filterCards(cards: List<SubspecCardState>, filter: StatsFilter): List<SubspecCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            else -> cards.filter { it.scenarioStats.matches(filter) }
        }

    private fun filterRequirementCards(
        cards: List<SpecRequirementCardState>,
        filter: StatsFilter,
    ): List<SpecRequirementCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            else -> cards.filter { it.scenarioStats.matches(filter) }
        }

    private fun filterScenarioCards(cards: List<SpecScenarioCardState>, filter: StatsFilter): List<SpecScenarioCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            StatsFilter.CORRECT -> cards.filter { it.status == ReviewStatus.CORRECT }
            StatsFilter.INCORRECT -> cards.filter { it.status == ReviewStatus.INCORRECT }
            StatsFilter.UNREVIEWED -> cards.filter { it.status == ReviewStatus.UNREVIEWED }
        }

    override fun onBack() = onBackCallback()
    override fun onStartReview() = onStartReviewCallback(specId)
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
            visibleSubspecs = filterCards(_state.value.subspecs, filter),
            visibleRequirements = filterRequirementCards(_state.value.requirements, filter),
            visibleScenarios = filterScenarioCards(_state.value.scenarios, filter),
        )
    }
    override fun onListModeChange(mode: SpecListMode) {
        _state.value = _state.value.copy(listMode = mode)
    }
    override fun onOpenSubspec(subspecId: String) = onOpenSubspecCallback(specId, subspecId)
    override fun onOpenRequirement(subspecId: String, requirementId: String) =
        onOpenRequirementCallback(specId, subspecId, requirementId)

    override fun onSetScenarioStatus(
        subspecId: String,
        requirementId: String,
        scenarioId: String,
        status: ReviewStatus,
    ) {
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
