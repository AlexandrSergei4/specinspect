package com.alki.specinspect.data.repository

import com.alki.specinspect.data.demo.DemoSpecification
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpecificationRepositoryTest {

    @Test
    fun restoresPersistedSpecificationsAfterRestart() {
        val storage = InMemorySpecificationPersistentStorage()
        val specification = Specification(
            id = "user-spec-1",
            name = "Persisted",
            isDemo = false,
            subspecs = emptyList(),
        )
        val repository = SpecificationRepository(storage)

        repository.add(specification)
        repository.remove(DemoSpecification.ID)

        val restoredRepository = SpecificationRepository(storage)

        assertEquals(
            listOf(DemoSpecification.ID, specification.id),
            restoredRepository.specifications.value.map { it.id },
        )
        assertEquals(listOf(specification.id), restoredRepository.visibleSpecifications().map { it.id })
        assertTrue(restoredRepository.demoHiddenInList.value)
    }

    @Test
    fun persistsDeletedUserSpecifications() {
        val storage = InMemorySpecificationPersistentStorage()
        val specification = Specification(
            id = "user-spec-1",
            name = "Persisted",
            isDemo = false,
            subspecs = emptyList(),
        )
        val repository = SpecificationRepository(storage)

        repository.add(specification)
        repository.remove(specification.id)

        val restoredRepository = SpecificationRepository(storage)

        assertNull(restoredRepository.getById(specification.id))
    }
}

private class InMemorySpecificationPersistentStorage : SpecificationPersistentStorage {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun clear() {
        value = null
    }
}
