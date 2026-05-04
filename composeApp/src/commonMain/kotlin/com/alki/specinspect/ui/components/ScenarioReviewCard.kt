package com.alki.specinspect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.Check
import compose.icons.tablericons.X
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_open_github_source
import specinspect.composeapp.generated.resources.status_correct
import specinspect.composeapp.generated.resources.status_incorrect

@Composable
fun ScenarioReviewCard(
    label: String,
    title: String,
    whenText: String,
    thenText: String,
    lastReviewedAt: Long?,
    status: ReviewStatus,
    sourceUrl: String?,
    onOpenSource: (String) -> Unit,
    onSetStatus: (ReviewStatus) -> Unit,
    modifier: Modifier = Modifier,
    context: String? = null,
) {
    TintedCard(
        background = AppColors.TealSurface,
        border = AppColors.TealBorder,
        modifier = modifier,
        padding = PaddingValues(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.GreyViolet,
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Dark,
                )
                if (!context.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        context,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.GreyViolet,
                    )
                }
            }
            if (sourceUrl != null) {
                Spacer(Modifier.width(12.dp))
                IconChip(
                    icon = TablerIcons.BrandGithub,
                    onClick = { onOpenSource(sourceUrl) },
                    background = AppColors.White,
                    iconTint = AppColors.Dark,
                    contentDescription = stringResource(Res.string.action_open_github_source),
                )
            }
        }
        ReviewTimestampText(lastReviewedAt, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(12.dp))
        WhenBlock(whenText)
        Spacer(Modifier.height(12.dp))
        ThenBlock(thenText)
        Spacer(Modifier.height(16.dp))
        StatusToggle(status, onSetStatus)
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
                text = stringResource(Res.string.status_incorrect),
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
                text = stringResource(Res.string.status_correct),
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
    icon: ImageVector,
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
