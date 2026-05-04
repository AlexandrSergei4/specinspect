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
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.Share

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
            contentDescription = "Поделиться",
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Что включить в отчёт",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Dark,
                )
            },
            text = {
                Column {
                    ReportStatusOptionRow(
                        text = "Некорректные",
                        checked = includeIncorrect,
                        onCheckedChange = { includeIncorrect = it },
                    )
                    ReportStatusOptionRow(
                        text = "Корректные",
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
                    Text("Поделиться")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Отмена")
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
    val label: String,
    val count: Int,
) {
    val text: String get() = "$label ($count)"
}

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
    TintedCard(
        background = AppColors.TealSurface,
        border = AppColors.TealBorder,
        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            "WHEN",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Teal,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Dark,
        )
    }
}

@Composable
fun ThenBlock(text: String) {
    TintedCard(
        background = AppColors.YellowSurface,
        border = AppColors.YellowBorder,
        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            "THEN",
            style = MaterialTheme.typography.labelMedium,
            color = AppColors.Dark,
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Dark,
        )
    }
}
