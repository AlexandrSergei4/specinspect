package com.alki.specinspect.features.subspec

import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.data.models.Subspec
import com.alki.specinspect.data.models.aggregatedStatus
import com.alki.specinspect.data.models.requirementStats
import com.alki.specinspect.data.models.scenarioStats
import com.alki.specinspect.data.models.stats
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
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

interface SubspecDetailComponent {
    val state: StateFlow<SubspecDetailState>
    fun onBack()
    fun onStartReview()
    fun onFilter(filter: StatsFilter)
    fun onOpenRequirement(requirementId: String)
}

data class RequirementCardState(
    val id: String,
    val title: String,
    val description: String,
    val lastReviewedAt: Long?,
    val scenarioCount: Int,
    val scenarioStats: ReviewStats,
    val aggregatedStatus: ReviewStatus,
)

data class SubspecDetailState(
    val specName: String = "",
    val subspecName: String = "",
    val requirementStats: ReviewStats = ReviewStats(0, 0, 0),
    val filter: StatsFilter = StatsFilter.ALL,
    val requirements: List<RequirementCardState> = emptyList(),
    val visibleRequirements: List<RequirementCardState> = emptyList(),
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
        val current = _state.value
        _state.value = SubspecDetailState(
            specName = spec.name,
            subspecName = sub.name,
            requirementStats = sub.requirementStats(statusOf),
            filter = current.filter,
            requirements = cards,
            visibleRequirements = filterCards(cards, current.filter),
            hasUnreviewedScenarios = sub.scenarioStats(statusOf).unreviewed > 0,
        )
    }

    private fun filterCards(cards: List<RequirementCardState>, filter: StatsFilter): List<RequirementCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            StatsFilter.CORRECT -> cards.filter { it.aggregatedStatus == ReviewStatus.CORRECT }
            StatsFilter.INCORRECT -> cards.filter { it.aggregatedStatus == ReviewStatus.INCORRECT }
            StatsFilter.UNREVIEWED -> cards.filter { it.aggregatedStatus == ReviewStatus.UNREVIEWED }
        }

    override fun onBack() = onBackCallback()
    override fun onStartReview() = onStartReviewCallback(specId, subspecId)
    override fun onFilter(filter: StatsFilter) {
        _state.value = _state.value.copy(
            filter = filter,
            visibleRequirements = filterCards(_state.value.requirements, filter),
        )
    }
    override fun onOpenRequirement(requirementId: String) =
        onOpenRequirementCallback(specId, subspecId, requirementId)
}
