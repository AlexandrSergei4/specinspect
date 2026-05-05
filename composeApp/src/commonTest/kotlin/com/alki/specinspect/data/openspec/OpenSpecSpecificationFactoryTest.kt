package com.alki.specinspect.data.openspec

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.models.ScenarioStep
import com.alki.specinspect.data.models.ScenarioStepKeyword
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

    @Test
    fun parsesMultilineScenarioStepsAndAndSections() {
        val subspec = OpenSpecParser.parseSubspec(
            name = "registration",
            content = """
                ## ADDED Requirements

                ### Requirement: Registration flow
                The system SHALL register invited users.

                #### Scenario: Invited user signs up
                - **GIVEN** the visitor has an invitation
                - **WHEN** the visitor starts registration
                  in order to create an account
                  with team access
                - **THEN** the account is created
                  and the dashboard opens
                - **AND** onboarding checklist is visible
                  with the first item selected
                - **AND** email notification is queued
            """.trimIndent(),
        )

        val scenario = subspec.requirements.single().scenarios.single()

        assertEquals(
            "the visitor starts registration\nin order to create an account\nwith team access",
            scenario.whenText,
        )
        assertEquals("the account is created\nand the dashboard opens", scenario.thenText)
        assertEquals(
            listOf(
                ScenarioStep(ScenarioStepKeyword.GIVEN, "the visitor has an invitation"),
                ScenarioStep(
                    ScenarioStepKeyword.WHEN,
                    "the visitor starts registration\nin order to create an account\nwith team access",
                ),
                ScenarioStep(ScenarioStepKeyword.THEN, "the account is created\nand the dashboard opens"),
                ScenarioStep(
                    ScenarioStepKeyword.AND,
                    "onboarding checklist is visible\nwith the first item selected",
                ),
                ScenarioStep(ScenarioStepKeyword.AND, "email notification is queued"),
            ),
            scenario.steps,
        )
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
