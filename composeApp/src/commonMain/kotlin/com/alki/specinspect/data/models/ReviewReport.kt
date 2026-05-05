package com.alki.specinspect.data.models

fun Specification.buildReviewReport(
    statusOf: (Scenario) -> ReviewStatus,
    subspecId: String? = null,
    requirementId: String? = null,
    includeCorrect: Boolean = false,
    includeIncorrect: Boolean = true,
    strings: ReviewReportStrings,
    specificationName: String = name,
): String {
    val scopedSubspecs = subspecs
        .filter { sub -> subspecId == null || sub.id == subspecId }
        .map { sub ->
            sub to sub.requirements.filter { req -> requirementId == null || req.id == requirementId }
        }
        .filter { (_, requirements) -> requirements.isNotEmpty() }

    val evaluatedCount = scopedSubspecs.sumOf { (_, requirements) ->
        requirements.sumOf { req ->
            req.scenarios.count { sc -> statusOf(sc).isIncluded(includeCorrect, includeIncorrect) }
        }
    }

    return buildString {
        appendLine(strings.title)
        appendLine(strings.specification(specificationName))
        appendLine(strings.evaluatedCount(evaluatedCount))

        if (evaluatedCount == 0) {
            appendLine()
            appendLine(strings.empty)
            return@buildString
        }

        scopedSubspecs.forEach { (sub, requirements) ->
            val evaluatedRequirements = requirements.filter { req ->
                req.scenarios.any { sc -> statusOf(sc).isIncluded(includeCorrect, includeIncorrect) }
            }
            if (evaluatedRequirements.isEmpty()) return@forEach

            appendLine()
            appendLine(strings.subspec(sub.name))
            evaluatedRequirements.forEach { req ->
                appendLine(strings.requirement(req.title))
                if (req.description.isNotBlank()) {
                    appendLine(strings.description(req.description))
                }
                req.scenarios.forEach { sc ->
                    val status = statusOf(sc)
                    if (status.isIncluded(includeCorrect, includeIncorrect)) {
                        appendLine(strings.scenario(strings.statusMarker(status), strings.statusLabel(status), sc.title))
                        sc.displaySteps().forEach { step ->
                            appendLine(strings.stepText(step))
                        }
                        gitHubUrlFor(sc)?.let { url ->
                            appendLine(strings.source(url))
                        }
                    }
                }
            }
        }
    }.trimEnd()
}

private fun ReviewStatus.isIncluded(includeCorrect: Boolean, includeIncorrect: Boolean): Boolean =
    when (this) {
        ReviewStatus.CORRECT -> includeCorrect
        ReviewStatus.INCORRECT -> includeIncorrect
        ReviewStatus.UNREVIEWED -> false
    }

data class ReviewReportStrings(
    val title: String,
    val specificationFormat: String,
    val evaluatedCountFormat: String,
    val empty: String,
    val subspecFormat: String,
    val requirementFormat: String,
    val descriptionFormat: String,
    val scenarioFormat: String,
    val givenFormat: String = "      GIVEN: %1\$s",
    val whenFormat: String,
    val thenFormat: String,
    val andFormat: String = "      AND: %1\$s",
    val sourceFormat: String,
    val correctMarker: String,
    val incorrectMarker: String,
    val unreviewedMarker: String,
    val correctStatus: String,
    val incorrectStatus: String,
    val unreviewedStatus: String,
) {
    fun specification(value: String): String = specificationFormat.replace("%1\$s", value)
    fun evaluatedCount(value: Int): String = evaluatedCountFormat.replace("%1\$d", value.toString())
    fun subspec(value: String): String = subspecFormat.replace("%1\$s", value)
    fun requirement(value: String): String = requirementFormat.replace("%1\$s", value)
    fun description(value: String): String = descriptionFormat.replace("%1\$s", value)
    fun scenario(marker: String, status: String, title: String): String =
        scenarioFormat
            .replace("%1\$s", marker)
            .replace("%2\$s", status)
            .replace("%3\$s", title)

    fun whenText(value: String): String = whenFormat.replace("%1\$s", value)
    fun thenText(value: String): String = thenFormat.replace("%1\$s", value)
    fun givenText(value: String): String = givenFormat.replace("%1\$s", value)
    fun andText(value: String): String = andFormat.replace("%1\$s", value)
    fun stepText(step: ScenarioStep): String = when (step.keyword) {
        ScenarioStepKeyword.GIVEN -> givenText(step.text)
        ScenarioStepKeyword.WHEN -> whenText(step.text)
        ScenarioStepKeyword.THEN -> thenText(step.text)
        ScenarioStepKeyword.AND -> andText(step.text)
    }
    fun source(value: String): String = sourceFormat.replace("%1\$s", value)

    fun statusLabel(status: ReviewStatus): String = when (status) {
        ReviewStatus.CORRECT -> correctStatus
        ReviewStatus.INCORRECT -> incorrectStatus
        ReviewStatus.UNREVIEWED -> unreviewedStatus
    }

    fun statusMarker(status: ReviewStatus): String = when (status) {
        ReviewStatus.CORRECT -> correctMarker
        ReviewStatus.INCORRECT -> incorrectMarker
        ReviewStatus.UNREVIEWED -> unreviewedMarker
    }
}
