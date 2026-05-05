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

        val report = specification.buildReviewReport(
            statusOf = { sc ->
                if (sc.id == evaluated.id) ReviewStatus.CORRECT else ReviewStatus.UNREVIEWED
            },
            includeCorrect = true,
            includeIncorrect = true,
            strings = testReviewReportStrings,
        )

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
            strings = testReviewReportStrings,
        )

        assertContains(report, "❌ Некорректный: Incorrect scenario")
        assertFalse(report.contains("Correct scenario"))
    }

    @Test
    fun reportIncludesOrderedScenarioStepsWhenPresent() {
        val steppedScenario = Scenario(
            id = "stepped",
            title = "Stepped scenario",
            whenText = "user opens the app",
            thenText = "dashboard is shown",
            steps = listOf(
                ScenarioStep(ScenarioStepKeyword.GIVEN, "the user is signed in"),
                ScenarioStep(ScenarioStepKeyword.WHEN, "user opens the app\nfrom a deep link"),
                ScenarioStep(ScenarioStepKeyword.THEN, "dashboard is shown"),
                ScenarioStep(ScenarioStepKeyword.AND, "latest data is refreshed"),
            ),
        )
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
                            scenarios = listOf(steppedScenario),
                        ),
                    ),
                ),
            ),
        )

        val report = specification.buildReviewReport(
            statusOf = { ReviewStatus.INCORRECT },
            includeCorrect = false,
            includeIncorrect = true,
            strings = testReviewReportStrings,
        )

        assertContains(report, "GIVEN: the user is signed in")
        assertContains(report, "WHEN: user opens the app\nfrom a deep link")
        assertContains(report, "AND: latest data is refreshed")
    }

    private fun scenario(id: String, title: String) = Scenario(
        id = id,
        title = title,
        whenText = "user opens the app",
        thenText = "dashboard is shown",
    )

    private val testReviewReportStrings = ReviewReportStrings(
        title = "Отчёт ревью",
        specificationFormat = "Спецификация: %1\$s",
        evaluatedCountFormat = "Оценённых сценариев: %1\$d",
        empty = "Оценённых сценариев пока нет.",
        subspecFormat = "Подспека: %1\$s",
        requirementFormat = "  Требование: %1\$s",
        descriptionFormat = "  Описание: %1\$s",
        scenarioFormat = "    - %1\$s %2\$s: %3\$s",
        whenFormat = "      WHEN: %1\$s",
        thenFormat = "      THEN: %1\$s",
        sourceFormat = "      Источник: %1\$s",
        correctMarker = "✅",
        incorrectMarker = "❌",
        unreviewedMarker = "",
        correctStatus = "Корректный",
        incorrectStatus = "Некорректный",
        unreviewedStatus = "Неоценённый",
    )
}
