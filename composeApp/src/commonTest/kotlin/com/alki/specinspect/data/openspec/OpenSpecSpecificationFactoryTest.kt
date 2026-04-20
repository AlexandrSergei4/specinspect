package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.importer.ImportedSpecFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OpenSpecSpecificationFactoryTest {

    @Test
    fun createsSpecificationFromImportedFiles() {
        val specification = OpenSpecSpecificationFactory.create(
            name = "Demo Spec",
            files = listOf(
                ImportedSpecFile(
                    name = "dashboard",
                    content = sampleSpec("Dashboard", "Loads data", "show widget"),
                ),
                ImportedSpecFile(
                    name = "events-feed",
                    content = sampleSpec("Events", "Shows updates", "render feed"),
                ),
            ),
        )

        assertEquals("Demo Spec", specification.name)
        assertEquals(2, specification.subspecs.size)
        assertEquals("dashboard", specification.subspecs[0].name)
        assertEquals(1, specification.subspecs[0].requirements.size)
        assertEquals(1, specification.subspecs[1].requirements.first().scenarios.size)
    }

    @Test
    fun failsWhenNoSpecFilesWereImported() {
        assertFailsWith<IllegalStateException> {
            OpenSpecSpecificationFactory.create(name = "Demo Spec", files = emptyList())
        }
    }

    private fun sampleSpec(
        requirementName: String,
        whenText: String,
        thenText: String,
    ): String = """
        ## ADDED Requirements

        ### Requirement: $requirementName
        Description

        #### Scenario: Happy path
        - **WHEN** $whenText
        - **THEN** $thenText
    """.trimIndent()
}
