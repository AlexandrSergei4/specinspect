package com.alki.specinspect.data.repository

import com.alki.specinspect.data.demo.DemoSpecification
import com.alki.specinspect.data.models.GitSource
import com.alki.specinspect.data.models.Requirement
import com.alki.specinspect.data.models.Scenario
import com.alki.specinspect.data.models.ScenarioSource
import com.alki.specinspect.data.models.Specification
import com.alki.specinspect.data.models.Subspec
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    @Test
    fun restoresScenarioSourceMetadataAfterRestart() {
        val storage = InMemorySpecificationPersistentStorage()
        val specification = Specification(
            id = "user-spec-2",
            name = "Imported",
            isDemo = false,
            subspecs = listOf(
                Subspec(
                    id = "sub-1",
                    name = "Authentication Flow",
                    requirements = listOf(
                        Requirement(
                            id = "req-1",
                            title = "Sign in with email",
                            description = "Requirement description",
                            scenarios = listOf(
                                Scenario(
                                    id = "sc-1",
                                    title = "Use Case 1",
                                    whenText = "When the user submits valid credentials",
                                    thenText = "the dashboard is shown",
                                    source = ScenarioSource(
                                        filePath = ".specify/specs/auth-flow/spec.md",
                                        line = 10,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            gitSource = GitSource(
                repository = "octo-org/specinspect",
                branch = "main",
            ),
        )
        val repository = SpecificationRepository(storage)

        repository.add(specification)

        val restoredRepository = SpecificationRepository(storage)
        val restoredSpec = restoredRepository.getById(specification.id)
        val restoredScenario = restoredSpec
            ?.subspecs
            ?.single()
            ?.requirements
            ?.single()
            ?.scenarios
            ?.single()

        assertNotNull(restoredSpec)
        assertEquals("octo-org/specinspect", restoredSpec.gitSource?.repository)
        assertEquals("main", restoredSpec.gitSource?.branch)
        assertEquals(".specify/specs/auth-flow/spec.md", restoredScenario?.source?.filePath)
        assertEquals(10, restoredScenario?.source?.line)
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
