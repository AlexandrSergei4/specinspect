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
import com.alki.specinspect.ui.components.ScenarioReviewCard
import com.alki.specinspect.ui.components.SectionDropdown
import com.alki.specinspect.ui.components.SectionDropdownOption
import com.alki.specinspect.ui.components.StatsBlocks
import com.alki.specinspect.ui.components.StatsCardHeader
import com.alki.specinspect.ui.components.StatsRow
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import com.alki.specinspect.localization.reviewReportStrings
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.PlayerPlay
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_start_review
import specinspect.composeapp.generated.resources.count_scenarios_reviewed
import specinspect.composeapp.generated.resources.demo_specification_name
import specinspect.composeapp.generated.resources.label_scenario
import specinspect.composeapp.generated.resources.section_dropdown_option_format
import specinspect.composeapp.generated.resources.section_requirements
import specinspect.composeapp.generated.resources.section_scenarios
import specinspect.composeapp.generated.resources.share_report_title
import specinspect.composeapp.generated.resources.stats_overview

@Composable
fun SubspecDetailScreen(component: SubspecDetailComponent) {
    val state by component.state.collectAsState()
    val specDisplayName = if (state.isDemoSpec) {
        stringResource(Res.string.demo_specification_name)
    } else {
        state.specName
    }
    val reportStrings = reviewReportStrings()
    val shareReportTitle = stringResource(Res.string.share_report_title)

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
                    subtitle = specDisplayName,
                    onBack = component::onBack,
                )
            }
            item {
                WhiteCard {
                    StatsCardHeader(
                        text = stringResource(Res.string.stats_overview),
                        onShare = { includeCorrect, includeIncorrect ->
                            component.onShareReport(
                                includeCorrect = includeCorrect,
                                includeIncorrect = includeIncorrect,
                                shareTitle = shareReportTitle,
                                reportStrings = reportStrings,
                                specificationName = specDisplayName,
                            )
                        },
                    )
                    Spacer(Modifier.height(16.dp))
                    StatsBlocks(
                        stats = state.scenarioStats,
                        selected = state.filter,
                        onFilter = component::onFilter,
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(Res.string.action_start_review),
                        onClick = component::onStartReview,
                        leadingIcon = TablerIcons.PlayerPlay,
                        height = 48,
                        enabled = state.hasUnreviewedScenarios,
                    )
                }
            }
            item {
                SectionDropdown(
                    selected = state.listMode,
                    options = listOf(
                        SectionDropdownOption(
                            SubspecListMode.REQUIREMENTS,
                            stringResource(
                                Res.string.section_dropdown_option_format,
                                stringResource(Res.string.section_requirements),
                                state.visibleRequirements.size,
                            ),
                        ),
                        SectionDropdownOption(
                            SubspecListMode.SCENARIOS,
                            stringResource(
                                Res.string.section_dropdown_option_format,
                                stringResource(Res.string.section_scenarios),
                                state.visibleScenarios.size,
                            ),
                        ),
                    ),
                    onSelected = component::onListModeChange,
                )
            }
            when (state.listMode) {
                SubspecListMode.REQUIREMENTS -> {
                    items(state.visibleRequirements, key = { it.id }) { card ->
                        RequirementCard(
                            card = card,
                            onClick = { component.onOpenRequirement(card.id) },
                        )
                    }
                }
                SubspecListMode.SCENARIOS -> {
                    items(state.visibleScenarios, key = { it.id }) { sc ->
                        ScenarioReviewCard(
                            label = stringResource(Res.string.label_scenario, sc.index),
                            title = sc.title,
                            whenText = sc.whenText,
                            thenText = sc.thenText,
                            lastReviewedAt = sc.lastReviewedAt,
                            status = sc.status,
                            sourceUrl = sc.sourceUrl,
                            context = sc.contextLabel,
                            onOpenSource = component::onOpenSource,
                            onSetStatus = { status ->
                                component.onSetScenarioStatus(sc.requirementId, sc.id, status)
                            },
                        )
                    }
                }
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
            stringResource(
                Res.string.count_scenarios_reviewed,
                card.scenarioCount,
                (card.scenarioStats.reviewedFraction * 100).toInt(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.GreyViolet,
        )
    }
}
