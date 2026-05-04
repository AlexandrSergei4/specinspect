package com.alki.specinspect.features.requirement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.PrimaryButton
import com.alki.specinspect.ui.components.ScenarioReviewCard
import com.alki.specinspect.ui.components.SectionHeader
import com.alki.specinspect.ui.components.StatsBlocks
import com.alki.specinspect.ui.components.StatsCardHeader
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import com.alki.specinspect.localization.reviewReportStrings
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_start_review
import specinspect.composeapp.generated.resources.breadcrumb_format
import specinspect.composeapp.generated.resources.demo_specification_name
import specinspect.composeapp.generated.resources.label_scenario
import specinspect.composeapp.generated.resources.requirement_title
import specinspect.composeapp.generated.resources.section_scenarios_count
import specinspect.composeapp.generated.resources.share_report_title
import specinspect.composeapp.generated.resources.stats_scenarios

@Composable
fun RequirementDetailScreen(component: RequirementDetailComponent) {
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
                    title = stringResource(Res.string.requirement_title),
                    subtitle = stringResource(Res.string.breadcrumb_format, specDisplayName, state.subspecName),
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
                    StatsCardHeader(
                        text = stringResource(Res.string.stats_scenarios),
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
                        stats = state.stats,
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
                SectionHeader(stringResource(Res.string.section_scenarios_count, state.visibleScenarios.size))
            }
            items(state.visibleScenarios, key = { it.id }) { sc ->
                ScenarioReviewCard(
                    label = stringResource(Res.string.label_scenario, sc.index),
                    title = sc.title,
                    whenText = sc.whenText,
                    thenText = sc.thenText,
                    lastReviewedAt = sc.lastReviewedAt,
                    status = sc.status,
                    sourceUrl = sc.sourceUrl,
                    onOpenSource = component::onOpenSource,
                    onSetStatus = { st -> component.onSetStatus(sc.id, st) },
                )
            }
        }
    }
}
