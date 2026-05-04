package com.alki.specinspect.data.models

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class ReviewReportTest {

    @Test
    fun reportIncludesOnlyEvaluatedScenariosGroupedBySubspecAndRequirement() {
        val evaluated = scenario(id = "evaluated", title = "Evaluated scenario")
        val unreviewed = scenario(id = "unreviewed", title = "Unreviewed scenario")
        val specification = Specification(
            id = "spec",
            name = "Demo spec",
            isDemo = true,
            subspecs = listOf(
                Subspec(
                    id = "sub",
                    name = "dashboard",
                    requirements = listOf(
                        Requirement(
                            id = "req",
                            title = "Default dashboard",
                            description = "Dashboard opens first.",
                            scenarios = listOf(evaluated, unreviewed),
                        ),
                    ),
                ),
            ),
        )

        val report = specification.buildReviewReport(statusOf = { sc ->
            if (sc.id == evaluated.id) ReviewStatus.CORRECT else ReviewStatus.UNREVIEWED
        })

        assertContains(report, "Подспека: dashboard")
        assertContains(report, "Требование: Default dashboard")
        assertContains(report, "✅ Корректный: Evaluated scenario")
        assertFalse(report.contains("Unreviewed scenario"))
    }

    @Test
    fun reportIncludesOnlySelectedStatuses() {
        val correct = scenario(id = "correct", title = "Correct scenario")
        val incorrect = scenario(id = "incorrect", title = "Incorrect scenario")
        val specification = Specification(
            id = "spec",
            name = "Demo spec",
            isDemo = true,
            subspecs = listOf(
                Subspec(
                    id = "sub",
                    name = "dashboard",
                    requirements = listOf(
                        Requirement(
                            id = "req",
                            title = "Default dashboard",
                            description = "",
                            scenarios = listOf(correct, incorrect),
                        ),
                    ),
                ),
            ),
        )

        val report = specification.buildReviewReport(
            statusOf = { sc ->
                if (sc.id == correct.id) ReviewStatus.CORRECT else ReviewStatus.INCORRECT
            },
            includeCorrect = false,
            includeIncorrect = true,
        )

        assertContains(report, "❌ Некорректный: Incorrect scenario")
        assertFalse(report.contains("Correct scenario"))
    }

    private fun scenario(id: String, title: String) = Scenario(
        id = id,
        title = title,
        whenText = "user opens the app",
        thenText = "dashboard is shown",
    )
}
