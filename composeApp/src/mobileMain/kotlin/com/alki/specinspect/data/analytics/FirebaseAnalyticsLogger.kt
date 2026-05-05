package com.alki.specinspect.data.analytics

import co.touchlab.kermit.Logger
import com.alki.specinspect.data.models.ReviewStatus
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

class FirebaseAnalyticsLogger : AnalyticsLogger {

    override fun logRepositoryImported(
        repositoryType: AnalyticsRepositoryType,
        specificationType: AnalyticsSpecificationType,
        subspecCount: Int,
        requirementCount: Int,
        scenarioCount: Int,
    ) {
        logEvent(EVENT_REPOSITORY_IMPORTED) {
            param(PARAM_REPOSITORY_TYPE, repositoryType.value)
            param(PARAM_SPECIFICATION_TYPE, specificationType.value)
            param(PARAM_SUBSPEC_COUNT, subspecCount)
            param(PARAM_REQUIREMENT_COUNT, requirementCount)
            param(PARAM_SCENARIO_COUNT, scenarioCount)
        }
    }

    override fun logReviewCardReviewed(
        status: ReviewStatus,
        reviewedCardsCount: Int,
        sessionReviewedCardsCount: Int,
        totalCardsInSession: Int,
        reviewScope: AnalyticsScope,
    ) {
        logEvent(EVENT_REVIEW_CARD_REVIEWED) {
            param(PARAM_REVIEW_STATUS, status.name.lowercase())
            param(PARAM_REVIEWED_CARDS_COUNT, reviewedCardsCount)
            param(PARAM_SESSION_REVIEWED_CARDS_COUNT, sessionReviewedCardsCount)
            param(PARAM_TOTAL_CARDS_IN_SESSION, totalCardsInSession)
            param(PARAM_SCOPE, reviewScope.value)
        }
    }

    override fun logShareUsed(scope: AnalyticsScope) {
        logEvent(EVENT_SHARE_USED) {
            param(PARAM_SCOPE, scope.value)
        }
    }

    override fun logGitHubSourceOpened(scope: AnalyticsScope) {
        logEvent(EVENT_GITHUB_SOURCE_OPENED) {
            param(PARAM_SCOPE, scope.value)
        }
    }

    private fun logEvent(
        name: String,
        parameters: dev.gitlive.firebase.analytics.FirebaseAnalyticsParameters.() -> Unit,
    ) {
        runCatching {
            Firebase.analytics.logEvent(name, parameters)
        }.onFailure { error ->
            Logger.w(error) { "Failed to log Firebase Analytics event: $name" }
        }
    }
}

private const val EVENT_REPOSITORY_IMPORTED = "repository_imported"
private const val EVENT_REVIEW_CARD_REVIEWED = "review_card_reviewed"
private const val EVENT_SHARE_USED = "share_used"
private const val EVENT_GITHUB_SOURCE_OPENED = "github_source_opened"

private const val PARAM_REPOSITORY_TYPE = "repository_type"
private const val PARAM_SPECIFICATION_TYPE = "specification_type"
private const val PARAM_SUBSPEC_COUNT = "subspec_count"
private const val PARAM_REQUIREMENT_COUNT = "requirement_count"
private const val PARAM_SCENARIO_COUNT = "scenario_count"
private const val PARAM_REVIEW_STATUS = "review_status"
private const val PARAM_REVIEWED_CARDS_COUNT = "reviewed_cards_count"
private const val PARAM_SESSION_REVIEWED_CARDS_COUNT = "session_reviewed_cards_count"
private const val PARAM_TOTAL_CARDS_IN_SESSION = "total_cards_in_session"
private const val PARAM_SCOPE = "scope"
