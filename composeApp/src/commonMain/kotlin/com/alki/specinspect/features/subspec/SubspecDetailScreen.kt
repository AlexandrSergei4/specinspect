package com.alki.specinspect.features.subspec

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.PrimaryButton
import com.alki.specinspect.ui.components.ReviewProgressBar
import com.alki.specinspect.ui.components.ReviewTimestampText
import com.alki.specinspect.ui.components.SectionHeader
import com.alki.specinspect.ui.components.StatsBlocks
import com.alki.specinspect.ui.components.StatsRow
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.PlayerPlay

@Composable
fun SubspecDetailScreen(component: SubspecDetailComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                AppTopBar(
                    title = state.subspecName,
                    subtitle = state.specName,
                    onBack = component::onBack,
                )
            }
            item {
                WhiteCard {
                    Text(
                        "Статистика требований",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.Dark,
                    )
                    Spacer(Modifier.height(16.dp))
                    StatsBlocks(
                        stats = state.requirementStats,
                        selected = state.filter,
                        onFilter = component::onFilter,
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Начать ревью",
                        onClick = component::onStartReview,
                        leadingIcon = TablerIcons.PlayerPlay,
                        height = 48,
                        enabled = state.hasUnreviewedScenarios,
                    )
                }
            }
            item {
                SectionHeader("Требования (${state.visibleRequirements.size})")
            }
            items(state.visibleRequirements, key = { it.id }) { card ->
                RequirementCard(
                    card = card,
                    onClick = { component.onOpenRequirement(card.id) },
                )
            }
        }
    }
}

@Composable
private fun RequirementCard(card: RequirementCardState, onClick: () -> Unit) {
    WhiteCard(onClick = onClick, padding = PaddingValues(20.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    card.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.Dark,
                )
                if (card.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        card.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.GreyViolet,
                        maxLines = 3,
                    )
                }
                ReviewTimestampText(card.lastReviewedAt, modifier = Modifier.padding(top = 8.dp))
            }
            Icon(
                TablerIcons.ChevronRight,
                contentDescription = null,
                tint = AppColors.GreyViolet,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        StatsRow(stats = card.scenarioStats)
        Spacer(Modifier.height(12.dp))
        ReviewProgressBar(stats = card.scenarioStats)
        Spacer(Modifier.height(8.dp))
        Text(
            "${card.scenarioCount} сценариев • ${(card.scenarioStats.reviewedFraction * 100).toInt()}% просмотрено",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.GreyViolet,
        )
    }
}
