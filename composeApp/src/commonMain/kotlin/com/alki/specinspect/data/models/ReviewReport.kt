package com.alki.specinspect.data.models

fun Specification.buildReviewReport(
    statusOf: (Scenario) -> ReviewStatus,
    subspecId: String? = null,
    requirementId: String? = null,
    includeCorrect: Boolean = false,
    includeIncorrect: Boolean = true,
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
        appendLine("Отчёт ревью")
        appendLine("Спецификация: $name")
        appendLine("Оценённых сценариев: $evaluatedCount")

        if (evaluatedCount == 0) {
            appendLine()
            appendLine("Оценённых сценариев пока нет.")
            return@buildString
        }

        scopedSubspecs.forEach { (sub, requirements) ->
            val evaluatedRequirements = requirements.filter { req ->
                req.scenarios.any { sc -> statusOf(sc).isIncluded(includeCorrect, includeIncorrect) }
            }
            if (evaluatedRequirements.isEmpty()) return@forEach

            appendLine()
            appendLine("Подспека: ${sub.name}")
            evaluatedRequirements.forEach { req ->
                appendLine("  Требование: ${req.title}")
                if (req.description.isNotBlank()) {
                    appendLine("  Описание: ${req.description}")
                }
                req.scenarios.forEach { sc ->
                    val status = statusOf(sc)
                    if (status.isIncluded(includeCorrect, includeIncorrect)) {
                        appendLine("    - ${status.reportMarker} ${status.reportLabel}: ${sc.title}")
                        appendLine("      WHEN: ${sc.whenText}")
                        appendLine("      THEN: ${sc.thenText}")
                        gitHubUrlFor(sc)?.let { url ->
                            appendLine("      Источник: $url")
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

private val ReviewStatus.reportMarker: String
    get() = when (this) {
        ReviewStatus.CORRECT -> "✅"
        ReviewStatus.INCORRECT -> "❌"
        ReviewStatus.UNREVIEWED -> ""
    }

private val ReviewStatus.reportLabel: String
    get() = when (this) {
        ReviewStatus.CORRECT -> "Корректный"
        ReviewStatus.INCORRECT -> "Некорректный"
        ReviewStatus.UNREVIEWED -> "Неоценённый"
    }
