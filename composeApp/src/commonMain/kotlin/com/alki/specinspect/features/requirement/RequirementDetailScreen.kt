package com.alki.specinspect.features.requirement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.IconChip
import com.alki.specinspect.ui.components.PrimaryButton
import com.alki.specinspect.ui.components.ReviewTimestampText
import com.alki.specinspect.ui.components.SectionHeader
import com.alki.specinspect.ui.components.StatsBlocks
import com.alki.specinspect.ui.components.ThenBlock
import com.alki.specinspect.ui.components.TintedCard
import com.alki.specinspect.ui.components.WhenBlock
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.Check
import compose.icons.tablericons.PlayerPlay
import compose.icons.tablericons.X

@Composable
fun RequirementDetailScreen(component: RequirementDetailComponent) {
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
                    title = "Requirement",
                    subtitle = state.breadcrumb,
                    onBack = component::onBack,
                )
            }
            item {
                WhiteCard {
                    Text(state.title, style = MaterialTheme.typography.headlineMedium, color = AppColors.Dark)
                    if (state.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.GreyViolet,
                        )
                    }
                }
            }
            item {
                WhiteCard {
                    Text("Статистика сценариев", style = MaterialTheme.typography.titleSmall, color = AppColors.Dark)
                    Spacer(Modifier.height(16.dp))
                    StatsBlocks(
                        stats = state.stats,
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
                SectionHeader("Сценарии (${state.visibleScenarios.size})")
            }
            items(state.visibleScenarios, key = { it.id }) { sc ->
                ScenarioCard(
                    state = sc,
                    onOpenSource = component::onOpenSource,
                    onSetStatus = { st -> component.onSetStatus(sc.id, st) },
                )
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    state: ScenarioCardState,
    onOpenSource: (String) -> Unit,
    onSetStatus: (ReviewStatus) -> Unit,
) {
    TintedCard(
        background = AppColors.TealSurface,
        border = AppColors.TealBorder,
        padding = PaddingValues(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "СЦЕНАРИЙ ${state.index}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.GreyViolet,
                )
                Text(
                    state.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Dark,
                )
            }
            if (state.sourceUrl != null) {
                Spacer(Modifier.width(12.dp))
                IconChip(
                    icon = TablerIcons.BrandGithub,
                    onClick = { onOpenSource(state.sourceUrl) },
                    background = AppColors.White,
                    iconTint = AppColors.Dark,
                    contentDescription = "Открыть источник на GitHub",
                )
            }
        }
        ReviewTimestampText(state.lastReviewedAt, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(12.dp))
        WhenBlock(state.whenText)
        Spacer(Modifier.height(12.dp))
        ThenBlock(state.thenText)
        Spacer(Modifier.height(16.dp))
        StatusToggle(state.status, onSetStatus)
    }
}

@Composable
private fun StatusToggle(
    status: ReviewStatus,
    onSetStatus: (ReviewStatus) -> Unit,
) {
    val incorrectSelected = status == ReviewStatus.INCORRECT
    val correctSelected = status == ReviewStatus.CORRECT
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.White)
            .padding(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ToggleSegment(
                text = "Некорректный",
                icon = TablerIcons.X,
                selected = incorrectSelected,
                color = AppColors.Coral,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSetStatus(if (incorrectSelected) ReviewStatus.UNREVIEWED else ReviewStatus.INCORRECT)
                },
            )
            Spacer(Modifier.width(8.dp))
            ToggleSegment(
                text = "Корректный",
                icon = TablerIcons.Check,
                selected = correctSelected,
                color = AppColors.Teal,
                modifier = Modifier.weight(1f),
                onClick = {
                    onSetStatus(if (correctSelected) ReviewStatus.UNREVIEWED else ReviewStatus.CORRECT)
                },
            )
        }
    }
}

@Composable
private fun ToggleSegment(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (selected) color else Color.Transparent
    val fg = if (selected) AppColors.White else AppColors.GreyViolet
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}
