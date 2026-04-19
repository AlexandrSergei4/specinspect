package com.alki.specinspect.features.review

object ReviewEngine {

    fun latestDecisionByCard(history: List<SwipeHistoryEntry>): Map<String, SwipeHistoryEntry> =
        history
            .groupBy { it.cardId }
            .mapValues { (_, entries) -> entries.maxBy { it.reviewedAtEpochMillis } }

    fun cardsForMode(
        library: ImportedSpecLibrary,
        history: List<SwipeHistoryEntry>,
        mode: ReviewMode
    ): List<RequirementCard> {
        val latest = latestDecisionByCard(history)
        return library.allCards.filter { card ->
            when (mode) {
                ReviewMode.Unreviewed -> card.cardId !in latest
                ReviewMode.ApprovedOnly -> latest[card.cardId]?.decision == ReviewDecision.Approved
                ReviewMode.RejectedOnly -> latest[card.cardId]?.decision == ReviewDecision.Rejected
            }
        }
    }

    fun stats(library: ImportedSpecLibrary, history: List<SwipeHistoryEntry>): ReviewStats {
        val latest = latestDecisionByCard(history)
        val totalCards = library.allCards.size
        val reviewedCards = latest.size
        val approvedCards = latest.values.count { it.decision == ReviewDecision.Approved }
        val rejectedCards = latest.values.count { it.decision == ReviewDecision.Rejected }

        val perSpec = library.specifications.map { specification ->
            val specEntries = specification.requirements.mapNotNull { latest[it.cardId] }
            val specRejected = specEntries.count { it.decision == ReviewDecision.Rejected }
            SpecificationReviewStats(
                specId = specification.specId,
                displayName = specification.displayName,
                totalCards = specification.requirements.size,
                reviewedCards = specEntries.size,
                rejectedCards = specRejected,
                rejectionRate = percentage(specRejected, specification.requirements.size)
            )
        }.sortedByDescending { it.rejectionRate }

        return ReviewStats(
            totalCards = totalCards,
            reviewedCards = reviewedCards,
            approvedCards = approvedCards,
            rejectedCards = rejectedCards,
            completionRate = percentage(reviewedCards, totalCards),
            rejectionRate = percentage(rejectedCards, totalCards),
            specificationStats = perSpec
        )
    }

    private fun percentage(part: Int, total: Int): Float {
        if (total == 0) return 0f
        return part.toFloat() / total.toFloat()
    }
}
