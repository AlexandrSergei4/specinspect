package com.alki.specinspect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alki.specinspect.data.models.ReviewStats
import com.alki.specinspect.data.models.StatsFilter
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.X

private data class StatStyle(
    val accent: Color,
    val surface: Color,
    val border: Color,
)

private val correctStyle = StatStyle(AppColors.Teal, AppColors.TealSurface, AppColors.TealBorder)
private val incorrectStyle = StatStyle(AppColors.Coral, AppColors.CoralSurface, AppColors.CoralBorder)
private val unreviewedStyle = StatStyle(AppColors.GreyViolet, AppColors.GreyVioletSurface, AppColors.GreyVioletBorder)

/**
 * Три цветные карточки статистики (корректные / некорректные / неоценённые)
 * с поддержкой клика для фильтрации.
 */
@Composable
fun StatsBlocks(
    stats: ReviewStats,
    selected: StatsFilter,
    onFilter: (StatsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatBlock(
            count = stats.correct,
            label = "Корректные",
            style = correctStyle,
            isSelected = selected == StatsFilter.CORRECT,
            onClick = { onFilter(if (selected == StatsFilter.CORRECT) StatsFilter.ALL else StatsFilter.CORRECT) },
            icon = StatIcon.Check,
            modifier = Modifier.weight(1f),
        )
        StatBlock(
            count = stats.incorrect,
            label = "Некорректные",
            style = incorrectStyle,
            isSelected = selected == StatsFilter.INCORRECT,
            onClick = { onFilter(if (selected == StatsFilter.INCORRECT) StatsFilter.ALL else StatsFilter.INCORRECT) },
            icon = StatIcon.X,
            modifier = Modifier.weight(1f),
        )
        StatBlock(
            count = stats.unreviewed,
            label = "Неоценённые",
            style = unreviewedStyle,
            isSelected = selected == StatsFilter.UNREVIEWED,
            onClick = { onFilter(if (selected == StatsFilter.UNREVIEWED) StatsFilter.ALL else StatsFilter.UNREVIEWED) },
            icon = StatIcon.Circle,
            modifier = Modifier.weight(1f),
        )
    }
}

private enum class StatIcon { Check, X, Circle }

@Composable
private fun StatBlock(
    count: Int,
    label: String,
    style: StatStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: StatIcon,
    modifier: Modifier = Modifier,
) {
    val borderWidth = if (isSelected) 2.dp else 1.dp
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(style.surface)
            .border(BorderStroke(borderWidth, style.accent.copy(alpha = if (isSelected) 0.6f else 0.3f)), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = when (icon) {
                StatIcon.Check -> TablerIcons.Check
                StatIcon.X -> TablerIcons.X
                StatIcon.Circle -> TablerIcons.Circle
            },
            contentDescription = null,
            tint = style.accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Dark,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.GreyViolet,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Компактный «inline» вариант статистики — три иконки + цифры в один ряд (для карточек списков).
 */
@Composable
fun StatsRow(
    stats: ReviewStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        StatChip(stats.correct, AppColors.Teal, StatIcon.Check)
        StatChip(stats.incorrect, AppColors.Coral, StatIcon.X)
        StatChip(stats.unreviewed, AppColors.GreyViolet, StatIcon.Circle)
    }
}

@Composable
private fun StatChip(value: Int, tint: Color, icon: StatIcon) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (icon) {
                StatIcon.Check -> TablerIcons.Check
                StatIcon.X -> TablerIcons.X
                StatIcon.Circle -> TablerIcons.Circle
            },
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = AppColors.Dark,
        )
    }
}

/**
 * Сегментированный прогресс-бар: teal (correct) / coral (incorrect) / серый фон (unreviewed).
 */
@Composable
fun ReviewProgressBar(
    stats: ReviewStats,
    modifier: Modifier = Modifier,
) {
    val total = stats.total.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(AppColors.GreyVioletSurface),
    ) {
        if (stats.correct > 0) {
            Box(
                modifier = Modifier
                    .weight(stats.correct.toFloat() / total)
                    .fillMaxWidth()
                    .background(AppColors.Teal),
            )
        }
        if (stats.incorrect > 0) {
            Box(
                modifier = Modifier
                    .weight(stats.incorrect.toFloat() / total)
                    .fillMaxWidth()
                    .background(AppColors.Coral),
            )
        }
        if (stats.unreviewed > 0) {
            Box(
                modifier = Modifier
                    .weight(stats.unreviewed.toFloat() / total)
                    .fillMaxWidth(),
            )
        }
    }
}
