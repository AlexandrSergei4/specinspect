package com.alki.specinspect.data.specification

import com.alki.specinspect.data.importer.ImportedSpecFile
import com.alki.specinspect.data.models.GitSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImportedSpecificationFactoryTest {

    @Test
    fun createsSpecificationFromOpenSpecFiles() {
        val specification = ImportedSpecificationFactory.create(
            name = "Demo Spec",
            files = listOf(
                ImportedSpecFile(
                    name = "dashboard",
                    path = "openspec/specs/dashboard/spec.md",
                    content = openSpec(
                        requirementName = "Dashboard",
                        whenText = "loads data",
                        thenText = "shows widgets",
                    ),
                ),
            ),
        )

        assertEquals("Demo Spec", specification.name)
        assertEquals(1, specification.subspecs.size)
        assertEquals("dashboard", specification.subspecs.single().name)
        assertEquals(1, specification.subspecs.single().requirements.single().scenarios.size)
        assertEquals(
            "openspec/specs/dashboard/spec.md",
            specification.subspecs.single().requirements.single().scenarios.single().source?.filePath,
        )
        assertEquals(6, specification.subspecs.single().requirements.single().scenarios.single().source?.line)
    }

    @Test
    fun createsSpecificationFromSpecKitFilesUsingAcceptanceScenariosOnly() {
        val specification = ImportedSpecificationFactory.create(
            name = "Spec Kit",
            files = listOf(
                ImportedSpecFile(
                    name = "auth-flow",
                    path = ".specify/specs/auth-flow/spec.md",
                    content = specKit(),
                ),
            ),
            gitSource = GitSource(
                repository = "octo-org/specinspect",
                branch = "main",
            ),
        )

        val subspec = specification.subspecs.single()
        val requirement = subspec.requirements.single()
        val firstScenario = requirement.scenarios.first()

        assertEquals("Authentication Flow", subspec.name)
        assertEquals("Sign in with email", requirement.title)
        assertEquals(2, requirement.scenarios.size)
        assertNotNull(specification.gitSource)
        assertTrue(requirement.description.contains("Functional Requirements:"))
        assertTrue(requirement.description.contains("Edge Cases:"))
        assertEquals("Given the user is on the sign-in screen\nWhen they submit valid credentials", firstScenario.whenText)
        assertEquals("the dashboard is shown", firstScenario.thenText)
        assertEquals(".specify/specs/auth-flow/spec.md", firstScenario.source?.filePath)
        assertEquals(10, firstScenario.source?.line)
    }

    @Test
    fun failsWhenFileIsNotRecognized() {
        assertFailsWith<IllegalStateException> {
            ImportedSpecificationFactory.create(
                name = "Broken",
                files = listOf(
                    ImportedSpecFile(
                        name = "broken",
                        content = "# Just notes",
                    ),
                ),
            )
        }
    }

    private fun openSpec(
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

    private fun specKit(): String = """
        # Feature Specification: Authentication Flow

        ## User Scenarios & Testing

        ### User Story 1 - Sign in with email (Priority: P1)
        A returning customer wants to enter the product quickly.
        **Why this priority**: Sign-in is the main entry point to the app.
        **Independent Test**: The user can sign in and land on the dashboard.
        **Acceptance Scenarios**:
        1. **Given** the user is on the sign-in screen, **When** they submit valid credentials, **Then** the dashboard is shown
        2. **Given** the user enters an invalid password, **When** they submit the form, **Then** an inline error message is shown
        ---
        ### Edge Cases
        - The account is locked after too many failed attempts

        ## Requirements

        ### Functional Requirements
        - **FR-001**: System MUST allow the user to sign in with email and password
        - **FR-002**: System MUST show an error when credentials are invalid
    """.trimIndent()
}
