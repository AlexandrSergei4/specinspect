package com.alki.specinspect.features.review

import kotlinx.serialization.Serializable

@Serializable
data class ImportedSpecLibrary(
    val sourceName: String,
    val sourceType: LibrarySourceType,
    val importedAtEpochMillis: Long,
    val specifications: List<ImportedSpecification>
) {
    val allCards: List<RequirementCard>
        get() = specifications.flatMap { it.requirements }

    val allScenarios: List<RequirementScenario>
        get() = specifications.flatMap { it.scenarios }
}

@Serializable
enum class LibrarySourceType {
    Demo,
    Custom
}

@Serializable
data class ImportedSpecification(
    val specId: String,
    val folderName: String,
    val displayName: String,
    val subspecs: List<ImportedSubspec>
) {
    val requirements: List<ImportedRequirement>
        get() = subspecs.flatMap { it.requirements }

    val scenarios: List<ImportedScenario>
        get() = requirements.flatMap { it.scenarios }
}

@Serializable
data class ImportedSubspec(
    val subspecId: String,
    val specId: String,
    val folderName: String,
    val displayName: String,
    val requirements: List<ImportedRequirement>
)

@Serializable
data class ImportedRequirement(
    val requirementId: String,
    val cardId: String,
    val specId: String,
    val subspecId: String,
    val specificationName: String,
    val subspecName: String,
    val requirementTitle: String,
    val requirementDescription: String,
    val scenarios: List<ImportedScenario>
)

@Serializable
data class ImportedScenario(
    val scenarioId: String,
    val requirementId: String,
    val title: String,
    val whenText: String,
    val thenText: String
)

typealias RequirementCard = ImportedRequirement
typealias RequirementScenario = ImportedScenario

@Serializable
data class SwipeHistoryEntry(
    val cardId: String,
    val decision: ReviewDecision,
    val reviewedAtEpochMillis: Long,
    val reviewSessionId: String
)

@Serializable
enum class ReviewDecision {
    Approved,
    Rejected
}

@Serializable
enum class ReviewMode {
    Unreviewed,
    ApprovedOnly,
    RejectedOnly
}

@Serializable
data class PersistedReviewState(
    val activeLibrary: ImportedSpecLibrary? = null,
    val libraries: List<ImportedSpecLibrary> = emptyList(),
    val swipeHistory: List<SwipeHistoryEntry> = emptyList(),
    val scenarioDecisions: Map<String, ReviewDecision> = emptyMap(),
    val currentMode: ReviewMode = ReviewMode.Unreviewed,
    val showOnboardingOnLaunch: Boolean = true,
    val isDemoHiddenInLibrary: Boolean = false
)

data class ReviewStats(
    val totalCards: Int,
    val reviewedCards: Int,
    val approvedCards: Int,
    val rejectedCards: Int,
    val completionRate: Float,
    val rejectionRate: Float,
    val specificationStats: List<SpecificationReviewStats>
)

data class SpecificationReviewStats(
    val specId: String,
    val displayName: String,
    val totalCards: Int,
    val reviewedCards: Int,
    val rejectedCards: Int,
    val rejectionRate: Float
)

enum class ReviewStatus {
    Correct,
    Incorrect,
    Unreviewed
}

data class ScopeReviewStats(
    val totalScenarios: Int,
    val correctScenarios: Int,
    val incorrectScenarios: Int,
    val unreviewedScenarios: Int
)

data class RequirementHierarchyStats(
    val requirementId: String,
    val requirementTitle: String,
    val stats: ScopeReviewStats
)

data class SubspecHierarchyStats(
    val subspecId: String,
    val displayName: String,
    val requirementCount: Int,
    val stats: ScopeReviewStats,
    val requirements: List<RequirementHierarchyStats>
)

data class SpecificationHierarchyStats(
    val specId: String,
    val displayName: String,
    val subspecCount: Int,
    val stats: ScopeReviewStats,
    val subspecs: List<SubspecHierarchyStats>
)

sealed interface ScenarioScope {
    data class Specification(val specId: String) : ScenarioScope
    data class Subspec(val specId: String, val subspecId: String) : ScenarioScope
    data class Requirement(
        val specId: String,
        val subspecId: String,
        val requirementId: String
    ) : ScenarioScope
}

data class RawImportedSpecification(
    val folderName: String,
    val markdown: String
)

data class RawImportedLibrary(
    val sourceName: String,
    val sourceType: LibrarySourceType,
    val specifications: List<RawImportedSpecification>
)
