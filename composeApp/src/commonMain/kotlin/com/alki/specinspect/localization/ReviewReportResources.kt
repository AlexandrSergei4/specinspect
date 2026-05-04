package com.alki.specinspect.localization

import androidx.compose.runtime.Composable
import com.alki.specinspect.data.models.ReviewReportStrings
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.report_description_format
import specinspect.composeapp.generated.resources.report_empty
import specinspect.composeapp.generated.resources.report_evaluated_count_format
import specinspect.composeapp.generated.resources.report_marker_correct
import specinspect.composeapp.generated.resources.report_marker_incorrect
import specinspect.composeapp.generated.resources.report_marker_unreviewed
import specinspect.composeapp.generated.resources.report_requirement_format
import specinspect.composeapp.generated.resources.report_scenario_format
import specinspect.composeapp.generated.resources.report_source_format
import specinspect.composeapp.generated.resources.report_specification_format
import specinspect.composeapp.generated.resources.report_subspec_format
import specinspect.composeapp.generated.resources.report_then_format
import specinspect.composeapp.generated.resources.report_title
import specinspect.composeapp.generated.resources.report_when_format
import specinspect.composeapp.generated.resources.status_correct
import specinspect.composeapp.generated.resources.status_incorrect
import specinspect.composeapp.generated.resources.status_unreviewed

@Composable
fun reviewReportStrings(): ReviewReportStrings = ReviewReportStrings(
    title = stringResource(Res.string.report_title),
    specificationFormat = stringResource(Res.string.report_specification_format),
    evaluatedCountFormat = stringResource(Res.string.report_evaluated_count_format),
    empty = stringResource(Res.string.report_empty),
    subspecFormat = stringResource(Res.string.report_subspec_format),
    requirementFormat = stringResource(Res.string.report_requirement_format),
    descriptionFormat = stringResource(Res.string.report_description_format),
    scenarioFormat = stringResource(Res.string.report_scenario_format),
    whenFormat = stringResource(Res.string.report_when_format),
    thenFormat = stringResource(Res.string.report_then_format),
    sourceFormat = stringResource(Res.string.report_source_format),
    correctMarker = stringResource(Res.string.report_marker_correct),
    incorrectMarker = stringResource(Res.string.report_marker_incorrect),
    unreviewedMarker = stringResource(Res.string.report_marker_unreviewed),
    correctStatus = stringResource(Res.string.status_correct),
    incorrectStatus = stringResource(Res.string.status_incorrect),
    unreviewedStatus = stringResource(Res.string.status_unreviewed),
)
