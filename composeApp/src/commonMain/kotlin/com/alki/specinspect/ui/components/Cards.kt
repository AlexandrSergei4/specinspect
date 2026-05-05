package com.alki.specinspect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alki.specinspect.data.models.ScenarioStep
import com.alki.specinspect.data.models.ScenarioStepKeyword
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.Share
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_cancel
import specinspect.composeapp.generated.resources.action_share
import specinspect.composeapp.generated.resources.label_and
import specinspect.composeapp.generated.resources.label_given
import specinspect.composeapp.generated.resources.label_then
import specinspect.composeapp.generated.resources.label_when
import specinspect.composeapp.generated.resources.report_include_correct
import specinspect.composeapp.generated.resources.report_include_incorrect
import specinspect.composeapp.generated.resources.report_options_title

/**
 * Базовая белая карточка с тонкой границей и мягкой тенью.
 */
@Composable
fun WhiteCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit,
) {
    var m = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(AppColors.White)
        .border(BorderStroke(1.dp, AppColors.CardBorder), RoundedCornerShape(16.dp))
    if (onClick != null) m = m.clickable { onClick() }
    Box(modifier = m) {
        Column(modifier = Modifier.padding(padding)) { content() }
    }
}

/**
 * Цветная заливная карточка (используется для блоков WHEN/THEN и подсказок).
 */
@Composable
fun TintedCard(
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(14.dp)),
    ) {
        Column(modifier = Modifier.padding(padding)) { content() }
    }
}

/**
 * Уппер-кейс заголовок секции серо-фиолетового цвета.
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AppColors.GreyViolet,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun StatsCardHeader(
    text: String,
    onShare: (includeCorrect: Boolean, includeIncorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var includeIncorrect by remember { mutableStateOf(true) }
    var includeCorrect by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = AppColors.Dark,
            modifier = Modifier.weight(1f),
        )
        IconChip(
            icon = TablerIcons.Share,
            onClick = {
                includeIncorrect = true
                includeCorrect = false
                showReportDialog = true
            },
            background = AppColors.GreyVioletSurface,
            iconTint = AppColors.GreyViolet,
            contentDescription = stringResource(Res.string.action_share),
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = stringResource(Res.string.report_options_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Dark,
                )
            },
            text = {
                Column {
                    ReportStatusOptionRow(
                        text = stringResource(Res.string.report_include_incorrect),
                        checked = includeIncorrect,
                        onCheckedChange = { includeIncorrect = it },
                    )
                    ReportStatusOptionRow(
                        text = stringResource(Res.string.report_include_correct),
                        checked = includeCorrect,
                        onCheckedChange = { includeCorrect = it },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = includeIncorrect || includeCorrect,
                    onClick = {
                        showReportDialog = false
                        onShare(includeCorrect, includeIncorrect)
                    },
                ) {
                    Text(stringResource(Res.string.action_share))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun ReportStatusOptionRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Dark,
        )
    }
}

data class SectionDropdownOption<T>(
    val value: T,
    val text: String,
)

@Composable
fun <T> SectionDropdown(
    selected: T,
    options: List<SectionDropdownOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.value == selected } ?: options.firstOrNull()

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = options.size > 1) { expanded = true }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedOption?.text.orEmpty().uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = AppColors.GreyViolet,
            )
            Icon(
                imageVector = TablerIcons.ChevronDown,
                contentDescription = null,
                tint = AppColors.GreyViolet,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Dark,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(option.value)
                    },
                )
            }
        }
    }
}

@Composable
fun YellowPillBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.Yellow)
            .padding(horizontal = 10.dp, vertical = 2.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = AppColors.Dark)
    }
}

@Composable
fun WhenBlock(text: String) {
    ScenarioStepBlock(ScenarioStep(ScenarioStepKeyword.WHEN, text))
}

@Composable
fun ThenBlock(text: String) {
    ScenarioStepBlock(ScenarioStep(ScenarioStepKeyword.THEN, text))
}

@Composable
fun ScenarioStepBlocks(
    steps: List<ScenarioStep>,
    fallbackWhenText: String,
    fallbackThenText: String,
) {
    val visibleSteps = steps.ifEmpty {
        buildList {
            if (fallbackWhenText.isNotBlank()) add(ScenarioStep(ScenarioStepKeyword.WHEN, fallbackWhenText))
            if (fallbackThenText.isNotBlank()) add(ScenarioStep(ScenarioStepKeyword.THEN, fallbackThenText))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        visibleSteps.forEach { step ->
            ScenarioStepBlock(step)
        }
    }
}

@Composable
private fun ScenarioStepBlock(step: ScenarioStep) {
    val background = when (step.keyword) {
        ScenarioStepKeyword.GIVEN -> AppColors.GreyVioletSurface
        ScenarioStepKeyword.WHEN -> AppColors.TealSurface
        ScenarioStepKeyword.THEN -> AppColors.YellowSurface
        ScenarioStepKeyword.AND -> Color(0x1A7E7F9A)
    }
    val border = when (step.keyword) {
        ScenarioStepKeyword.GIVEN -> AppColors.GreyVioletBorder
        ScenarioStepKeyword.WHEN -> AppColors.TealBorder
        ScenarioStepKeyword.THEN -> AppColors.YellowBorder
        ScenarioStepKeyword.AND -> AppColors.CoralBorder
    }
    val label = when (step.keyword) {
        ScenarioStepKeyword.GIVEN -> stringResource(Res.string.label_given)
        ScenarioStepKeyword.WHEN -> stringResource(Res.string.label_when)
        ScenarioStepKeyword.THEN -> stringResource(Res.string.label_then)
        ScenarioStepKeyword.AND -> stringResource(Res.string.label_and)
    }
    val labelColor = when (step.keyword) {
        ScenarioStepKeyword.GIVEN -> AppColors.GreyViolet
        ScenarioStepKeyword.WHEN -> AppColors.Teal
        ScenarioStepKeyword.THEN -> AppColors.Dark
        ScenarioStepKeyword.AND -> AppColors.Coral
    }

    TintedCard(
        background = background,
        border = border,
        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = labelColor,
        )
        Text(
            step.text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Dark,
        )
    }
}
