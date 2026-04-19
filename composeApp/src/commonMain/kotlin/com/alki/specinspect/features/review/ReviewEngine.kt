package com.alki.specinspect.features.review

object ReviewEngine {

    fun unreviewedScenarioQueue(
        library: ImportedSpecLibrary,
        scope: ScenarioScope,
        scenarioDecisions: Map<String, ReviewDecision>
    ): List<ImportedScenario> =
        scopedScenarios(library, scope).filter { scenario ->
            scenarioDecisions[scenario.scenarioId] == null
        }

    fun scopedScenarios(
        library: ImportedSpecLibrary,
        scope: ScenarioScope
    ): List<ImportedScenario> = when (scope) {
        is ScenarioScope.Specification -> {
            library.specifications
                .firstOrNull { it.specId == scope.specId }
                ?.scenarios
                .orEmpty()
        }
        is ScenarioScope.Subspec -> {
            library.specifications
                .firstOrNull { it.specId == scope.specId }
                ?.subspecs
                ?.firstOrNull { it.subspecId == scope.subspecId }
                ?.requirements
                ?.flatMap { it.scenarios }
                .orEmpty()
        }
        is ScenarioScope.Requirement -> {
            library.specifications
                .firstOrNull { it.specId == scope.specId }
                ?.subspecs
                ?.firstOrNull { it.subspecId == scope.subspecId }
                ?.requirements
                ?.firstOrNull { it.requirementId == scope.requirementId }
                ?.scenarios
                .orEmpty()
        }
    }

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

    fun hierarchyStats(
        library: ImportedSpecLibrary,
        history: List<SwipeHistoryEntry>,
        scenarioDecisions: Map<String, ReviewDecision> = emptyMap()
    ): List<SpecificationHierarchyStats> {
        val latest = latestDecisionByCard(history)
        return library.specifications.map { specification ->
            val subspecStats = specification.subspecs.map { subspec ->
                val requirementStats = subspec.requirements.map { requirement ->
                    RequirementHierarchyStats(
                        requirementId = requirement.requirementId,
                        requirementTitle = requirement.requirementTitle,
                        stats = requirement.scenarios.toScopeStats(
                            latest = latest,
                            cardId = requirement.cardId,
                            scenarioDecisions = scenarioDecisions
                        )
                    )
                }
                SubspecHierarchyStats(
                    subspecId = subspec.subspecId,
                    displayName = subspec.displayName,
                    requirementCount = subspec.requirements.size,
                    stats = aggregateRequirementStats(requirementStats),
                    requirements = requirementStats
                )
            }

            SpecificationHierarchyStats(
                specId = specification.specId,
                displayName = specification.displayName,
                subspecCount = specification.subspecs.size,
                stats = aggregateSubspecStats(subspecStats),
                subspecs = subspecStats
            )
        }
    }

    private fun List<ImportedScenario>.toScopeStats(
        latest: Map<String, SwipeHistoryEntry>,
        cardId: String,
        scenarioDecisions: Map<String, ReviewDecision>
    ): ScopeReviewStats {
        if (isEmpty()) {
            return ScopeReviewStats(
                totalScenarios = 0,
                correctScenarios = 0,
                incorrectScenarios = 0,
                unreviewedScenarios = 0
            )
        }

        val fallbackDecision = latest[cardId]?.decision
        var correct = 0
        var incorrect = 0
        var unreviewed = 0

        forEach { scenario ->
            val decision = scenarioDecisions[scenario.scenarioId] ?: fallbackDecision
            when (decision) {
                ReviewDecision.Approved -> correct++
                ReviewDecision.Rejected -> incorrect++
                null -> unreviewed++
            }
        }

        return ScopeReviewStats(
            totalScenarios = size,
            correctScenarios = correct,
            incorrectScenarios = incorrect,
            unreviewedScenarios = unreviewed
        )
    }

    private fun aggregateRequirementStats(items: List<RequirementHierarchyStats>): ScopeReviewStats {
        val total = items.sumOf { it.stats.totalScenarios }
        return ScopeReviewStats(
            totalScenarios = total,
            correctScenarios = items.sumOf { it.stats.correctScenarios },
            incorrectScenarios = items.sumOf { it.stats.incorrectScenarios },
            unreviewedScenarios = items.sumOf { it.stats.unreviewedScenarios }
        )
    }

    private fun aggregateSubspecStats(items: List<SubspecHierarchyStats>): ScopeReviewStats {
        val total = items.sumOf { it.stats.totalScenarios }
        return ScopeReviewStats(
            totalScenarios = total,
            correctScenarios = items.sumOf { it.stats.correctScenarios },
            incorrectScenarios = items.sumOf { it.stats.incorrectScenarios },
            unreviewedScenarios = items.sumOf { it.stats.unreviewedScenarios }
        )
    }

    private fun percentage(part: Int, total: Int): Float {
        if (total == 0) return 0f
        return part.toFloat() / total.toFloat()
    }
}
