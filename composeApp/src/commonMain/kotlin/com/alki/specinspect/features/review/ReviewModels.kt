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
    val requirements: List<RequirementCard>
)

@Serializable
data class RequirementCard(
    val cardId: String,
    val specId: String,
    val specificationName: String,
    val requirementTitle: String,
    val requirementDescription: String,
    val scenarios: List<RequirementScenario>
)

@Serializable
data class RequirementScenario(
    val title: String,
    val whenText: String,
    val thenText: String
)

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
    val swipeHistory: List<SwipeHistoryEntry> = emptyList(),
    val currentMode: ReviewMode = ReviewMode.Unreviewed
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

data class RawImportedSpecification(
    val folderName: String,
    val markdown: String
)

data class RawImportedLibrary(
    val sourceName: String,
    val sourceType: LibrarySourceType,
    val specifications: List<RawImportedSpecification>
)
