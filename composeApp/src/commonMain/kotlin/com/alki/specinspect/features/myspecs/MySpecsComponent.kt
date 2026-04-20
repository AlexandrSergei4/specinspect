package com.alki.specinspect.features.myspecs

import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.totalRequirements
import com.alki.specinspect.data.repository.SpecificationRepository
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MySpecsComponent {
    val state: StateFlow<MySpecsState>
    fun onAdd()
    fun onOpen(specId: String)
    fun onDelete(specId: String)
    fun onBack()
}

data class SpecListItem(
    val id: String,
    val name: String,
    val isDemo: Boolean,
    val subspecCount: Int,
    val requirementCount: Int,
)

data class MySpecsState(
    val items: List<SpecListItem> = emptyList(),
)

class DefaultMySpecsComponent(
    componentContext: ComponentContext,
    private val repo: SpecificationRepository,
    private val onAddCallback: () -> Unit,
    private val onOpenCallback: (String) -> Unit,
    private val onBackCallback: () -> Unit,
) : MySpecsComponent, ComponentContext by componentContext {

    private val _state = MutableStateFlow(MySpecsState())
    override val state: StateFlow<MySpecsState> = _state.asStateFlow()

    init {
        refresh()
        lifecycle.doOnResume { refresh() }
    }

    private fun refresh() {
        _state.value = MySpecsState(
            items = repo.visibleSpecifications().map { it.toListItem() }
        )
    }

    override fun onAdd() = onAddCallback()
    override fun onOpen(specId: String) = onOpenCallback(specId)
    override fun onDelete(specId: String) {
        repo.remove(specId)
        refresh()
    }
    override fun onBack() = onBackCallback()
}

private fun Specification.toListItem(): SpecListItem = SpecListItem(
    id = id,
    name = name,
    isDemo = isDemo,
    subspecCount = subspecs.size,
    requirementCount = totalRequirements(),
)
