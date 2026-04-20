package com.alki.specinspect.data.repository

import com.alki.specinspect.data.demo.DemoSpecification
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.storage.NoOpSpecificationPersistentStorage
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Репозиторий загруженных пользователем спецификаций + демо.
 * Демо-спецификация всегда доступна на экране онбординга, но может быть
 * скрыта на экране "Мои спецификации" (флаг demoHiddenInList).
 */
class SpecificationRepository(
    private val storage: SpecificationPersistentStorage = NoOpSpecificationPersistentStorage,
) {

    private val persistedState = loadPersistedState()

    private val _specifications = MutableStateFlow<List<Specification>>(
        listOf(DemoSpecification.build()) + persistedState.userSpecifications.filterNot {
            it.isDemo || it.id == DemoSpecification.ID
        }
    )
    val specifications: StateFlow<List<Specification>> = _specifications.asStateFlow()

    private val _demoHiddenInList = MutableStateFlow(persistedState.demoHiddenInList)
    val demoHiddenInList: StateFlow<Boolean> = _demoHiddenInList.asStateFlow()

    /**
     * Демо-спецификация всегда доступна (для онбординга),
     * даже если убрана из списка "Мои спецификации".
     */
    fun getDemo(): Specification = DemoSpecification.build()

    fun getById(id: String): Specification? =
        _specifications.value.firstOrNull { it.id == id }

    fun add(specification: Specification) {
        _specifications.value = _specifications.value + specification
        persistState()
    }

    /**
     * Удаляет спецификацию из списка. Демо-спецификация только скрывается,
     * чтобы оставаться доступной на онбординге.
     */
    fun remove(id: String) {
        if (id == DemoSpecification.ID) {
            _demoHiddenInList.value = true
            persistState()
            return
        }
        _specifications.value = _specifications.value.filterNot { it.id == id }
        persistState()
    }

    /**
     * Список для экрана "Мои спецификации" — учитывает скрытие демо.
     */
    fun visibleSpecifications(): List<Specification> {
        val all = _specifications.value
        return if (_demoHiddenInList.value) all.filterNot { it.id == DemoSpecification.ID } else all
    }

    private fun loadPersistedState(): PersistedSpecificationState =
        storage.read()
            ?.takeIf { it.isNotBlank() }
            ?.let { payload ->
                runCatching {
                    specificationRepositoryJson.decodeFromString<PersistedSpecificationState>(payload)
                }.getOrElse {
                    storage.clear()
                    PersistedSpecificationState()
                }
            }
            ?: PersistedSpecificationState()

    private fun persistState() {
        val state = PersistedSpecificationState(
            userSpecifications = _specifications.value.filterNot { it.isDemo || it.id == DemoSpecification.ID },
            demoHiddenInList = _demoHiddenInList.value,
        )
        if (state.userSpecifications.isEmpty() && !state.demoHiddenInList) {
            storage.clear()
            return
        }
        storage.write(specificationRepositoryJson.encodeToString(state))
    }
}

@Serializable
private data class PersistedSpecificationState(
    val userSpecifications: List<Specification> = emptyList(),
    val demoHiddenInList: Boolean = false,
)

private val specificationRepositoryJson = Json {
    ignoreUnknownKeys = true
}
