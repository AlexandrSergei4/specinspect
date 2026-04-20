package com.alki.specinspect.data.models

import kotlinx.serialization.Serializable

/**
 * Статус ревью сценария
 */
enum class ReviewStatus {
    UNREVIEWED,
    CORRECT,
    INCORRECT
}

/**
 * Сценарий внутри Requirement
 */
@Serializable
data class Scenario(
    val id: String,
    val title: String,
    val whenText: String,
    val thenText: String,
)

/**
 * Requirement внутри Subspec
 */
@Serializable
data class Requirement(
    val id: String,
    val title: String,
    val description: String,
    val scenarios: List<Scenario>,
)

/**
 * Subspec — отдельная папка внутри specs/
 */
@Serializable
data class Subspec(
    val id: String,
    val name: String,
    val requirements: List<Requirement>,
)

/**
 * Спецификация целиком
 */
@Serializable
data class Specification(
    val id: String,
    val name: String,
    val isDemo: Boolean,
    val subspecs: List<Subspec>,
)

/**
 * Агрегированная статистика по любой сущности
 */
data class ReviewStats(
    val correct: Int,
    val incorrect: Int,
    val unreviewed: Int,
) {
    val total: Int get() = correct + incorrect + unreviewed
    val reviewed: Int get() = correct + incorrect
    val reviewedFraction: Float get() = if (total == 0) 0f else reviewed.toFloat() / total
}

/**
 * Фильтр по статусу — используется на экранах spec/subspec/requirement
 */
enum class StatsFilter { ALL, CORRECT, INCORRECT, UNREVIEWED }

// ===== расширения для подсчёта статистики =====

fun List<Scenario>.statsBy(statusOf: (Scenario) -> ReviewStatus): ReviewStats {
    var c = 0; var i = 0; var u = 0
    forEach {
        when (statusOf(it)) {
            ReviewStatus.CORRECT -> c++
            ReviewStatus.INCORRECT -> i++
            ReviewStatus.UNREVIEWED -> u++
        }
    }
    return ReviewStats(correct = c, incorrect = i, unreviewed = u)
}

/**
 * Статистика Requirement по его сценариям
 */
fun Requirement.stats(statusOf: (Scenario) -> ReviewStatus): ReviewStats =
    scenarios.statsBy(statusOf)

/**
 * Статистика Requirement, выраженная в Requirement-единицах
 * (один Requirement = CORRECT если все сценарии CORRECT,
 *                     INCORRECT если хотя бы один INCORRECT,
 *                     иначе UNREVIEWED)
 */
fun Requirement.aggregatedStatus(statusOf: (Scenario) -> ReviewStatus): ReviewStatus {
    if (scenarios.isEmpty()) return ReviewStatus.UNREVIEWED
    val statuses = scenarios.map(statusOf)
    return when {
        statuses.any { it == ReviewStatus.INCORRECT } -> ReviewStatus.INCORRECT
        statuses.all { it == ReviewStatus.CORRECT } -> ReviewStatus.CORRECT
        else -> ReviewStatus.UNREVIEWED
    }
}

fun Subspec.aggregatedStatus(statusOf: (Scenario) -> ReviewStatus): ReviewStatus {
    if (requirements.isEmpty()) return ReviewStatus.UNREVIEWED
    val statuses = requirements.map { it.aggregatedStatus(statusOf) }
    return when {
        statuses.any { it == ReviewStatus.INCORRECT } -> ReviewStatus.INCORRECT
        statuses.all { it == ReviewStatus.CORRECT } -> ReviewStatus.CORRECT
        else -> ReviewStatus.UNREVIEWED
    }
}

fun Subspec.requirementStats(statusOf: (Scenario) -> ReviewStatus): ReviewStats {
    var c = 0; var i = 0; var u = 0
    requirements.forEach {
        when (it.aggregatedStatus(statusOf)) {
            ReviewStatus.CORRECT -> c++
            ReviewStatus.INCORRECT -> i++
            ReviewStatus.UNREVIEWED -> u++
        }
    }
    return ReviewStats(correct = c, incorrect = i, unreviewed = u)
}

fun Subspec.scenarioStats(statusOf: (Scenario) -> ReviewStatus): ReviewStats =
    requirements.flatMap { it.scenarios }.statsBy(statusOf)

fun Specification.subspecStats(statusOf: (Scenario) -> ReviewStatus): ReviewStats {
    var c = 0; var i = 0; var u = 0
    subspecs.forEach {
        when (it.aggregatedStatus(statusOf)) {
            ReviewStatus.CORRECT -> c++
            ReviewStatus.INCORRECT -> i++
            ReviewStatus.UNREVIEWED -> u++
        }
    }
    return ReviewStats(correct = c, incorrect = i, unreviewed = u)
}

fun Specification.scenarioStats(statusOf: (Scenario) -> ReviewStatus): ReviewStats =
    subspecs.flatMap { it.requirements }.flatMap { it.scenarios }.statsBy(statusOf)

fun Specification.totalRequirements(): Int = subspecs.sumOf { it.requirements.size }
fun Specification.totalScenarios(): Int = subspecs.sumOf { sub -> sub.requirements.sumOf { it.scenarios.size } }
fun Subspec.totalScenarios(): Int = requirements.sumOf { it.scenarios.size }
