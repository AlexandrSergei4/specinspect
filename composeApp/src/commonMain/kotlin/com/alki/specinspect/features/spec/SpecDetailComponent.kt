package com.alki.specinspect.features.spec

import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.data.models.aggregatedStatus
import com.alki.specinspect.data.models.requirementStats
import com.alki.specinspect.data.models.scenarioStats
import com.alki.specinspect.data.models.subspecStats
import com.alki.specinspect.data.models.totalScenarios
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

interface SpecDetailComponent {
    val state: StateFlow<SpecDetailState>
    fun onBack()
    fun onStartReview()
    fun onFilter(filter: StatsFilter)
    fun onOpenSubspec(subspecId: String)
}

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

data class SpecDetailState(
    val specName: String = "",
    val subspecCount: Int = 0,
    val subspecStats: ReviewStats = ReviewStats(0, 0, 0),
    val filter: StatsFilter = StatsFilter.ALL,
    val subspecs: List<SubspecCardState> = emptyList(),
    val visibleSubspecs: List<SubspecCardState> = emptyList(),
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
        val current = _state.value
        val filtered = filterCards(cards, current.filter)
        _state.value = SpecDetailState(
            specName = spec.name,
            subspecCount = spec.subspecs.size,
            subspecStats = spec.subspecStats(statusOf),
            filter = current.filter,
            subspecs = cards,
            visibleSubspecs = filtered,
            hasUnreviewedScenarios = spec.scenarioStats(statusOf).unreviewed > 0,
        )
    }

    private fun filterCards(cards: List<SubspecCardState>, filter: StatsFilter): List<SubspecCardState> =
        when (filter) {
            StatsFilter.ALL -> cards
            StatsFilter.CORRECT -> cards.filter { it.aggregatedStatus == ReviewStatus.CORRECT }
            StatsFilter.INCORRECT -> cards.filter { it.aggregatedStatus == ReviewStatus.INCORRECT }
            StatsFilter.UNREVIEWED -> cards.filter { it.aggregatedStatus == ReviewStatus.UNREVIEWED }
        }

    override fun onBack() = onBackCallback()
    override fun onStartReview() = onStartReviewCallback(specId)
    override fun onFilter(filter: StatsFilter) {
        _state.value = _state.value.copy(
            filter = filter,
            visibleSubspecs = filterCards(_state.value.subspecs, filter),
        )
    }
    override fun onOpenSubspec(subspecId: String) = onOpenSubspecCallback(specId, subspecId)
}
