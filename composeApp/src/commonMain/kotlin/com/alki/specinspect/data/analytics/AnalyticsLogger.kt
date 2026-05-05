package com.alki.specinspect.data.analytics

import com.alki.specinspect.data.models.ReviewStatus

enum class AnalyticsRepositoryType(val value: String) {
    Personal("personal"),
    Public("public"),
}

enum class AnalyticsSpecificationType(val value: String) {
    OpenSpec("openspec"),
    SpecKit("spec_kit"),
    Mixed("mixed"),
}

enum class AnalyticsScope(val value: String) {
    Spec("spec"),
    Subspec("subspec"),
    Requirement("requirement"),
    Review("review"),
}

interface AnalyticsLogger {
    fun logRepositoryImported(
        repositoryType: AnalyticsRepositoryType,
        specificationType: AnalyticsSpecificationType,
        subspecCount: Int,
        requirementCount: Int,
        scenarioCount: Int,
    )

    fun logReviewCardReviewed(
        status: ReviewStatus,
        reviewedCardsCount: Int,
        sessionReviewedCardsCount: Int,
        totalCardsInSession: Int,
        reviewScope: AnalyticsScope,
    )

    fun logShareUsed(scope: AnalyticsScope)

    fun logGitHubSourceOpened(scope: AnalyticsScope)
}

object NoOpAnalyticsLogger : AnalyticsLogger {
    override fun logRepositoryImported(
        repositoryType: AnalyticsRepositoryType,
        specificationType: AnalyticsSpecificationType,
        subspecCount: Int,
        requirementCount: Int,
        scenarioCount: Int,
    ) = Unit

    override fun logReviewCardReviewed(
        status: ReviewStatus,
        reviewedCardsCount: Int,
        sessionReviewedCardsCount: Int,
        totalCardsInSession: Int,
        reviewScope: AnalyticsScope,
    ) = Unit

    override fun logShareUsed(scope: AnalyticsScope) = Unit

    override fun logGitHubSourceOpened(scope: AnalyticsScope) = Unit
}
