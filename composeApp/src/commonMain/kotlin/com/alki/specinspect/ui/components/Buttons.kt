package com.alki.specinspect.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.theme.AppColors
import com.alki.specinspect.ui.theme.SampleTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.add_spec_loading
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    height: Int = 56,
    enabled: Boolean = true,
    isShimmering: Boolean = false,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) AppColors.PrimaryAction else AppColors.GreyViolet)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isShimmering) {
            val shimmerTransition = rememberInfiniteTransition(label = "primaryButtonShimmer")
            val shimmerProgress by shimmerTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "primaryButtonShimmerProgress",
            )
            val density = LocalDensity.current
            val buttonWidthPx = with(density) { maxWidth.toPx() }
            val minShimmerWidthPx = with(density) { 24.dp.toPx() }
            val maxShimmerWidthPx = with(density) { 132.dp.toPx() }
            val bellFactor = sin(shimmerProgress * PI).toFloat().coerceIn(0f, 1f)
            val currentShimmerWidthPx =
                minShimmerWidthPx + ((maxShimmerWidthPx - minShimmerWidthPx) * bellFactor)
            val currentShimmerWidthDp = with(density) { currentShimmerWidthPx.toDp() }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(currentShimmerWidthDp)
                    .graphicsLayer {
                        translationX = (-buttonWidthPx/2) + (buttonWidthPx * shimmerProgress) + currentShimmerWidthPx/2
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AppColors.White.copy(alpha = 0.08f + (0.06f * bellFactor)),
                                AppColors.White.copy(alpha = 0.18f + (0.18f * bellFactor)),
                                AppColors.White.copy(alpha = 0.08f + (0.06f * bellFactor)),
                                Color.Transparent,
                            ),
                        )
                    ),
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = AppColors.OnPrimaryAction, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium, color = AppColors.OnPrimaryAction)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    height: Int = 56,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.White)
            .border(BorderStroke(2.dp, AppColors.CardBorderStrong), RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = AppColors.Dark, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium, color = AppColors.Dark)
        }
    }
}

@Composable
fun DashedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.White)
            .border(BorderStroke(2.dp, AppColors.CardBorderStrong), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = AppColors.Dark, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium, color = AppColors.Dark)
        }
    }
}

@Composable
fun IconChip(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = AppColors.PrimaryAction,
    iconTint: Color = AppColors.OnPrimaryAction,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = iconTint, modifier = Modifier.size(18.dp))
    }
}

@Preview
@Composable
private fun PrimaryButtonShimmerPreview() {
    SampleTheme {
        Box(
            modifier = Modifier
                .width(320.dp)
                .background(AppColors.Light)
                .padding(16.dp),
        ) {
            PrimaryButton(
                text = stringResource(Res.string.add_spec_loading),
                onClick = {},
                enabled = false,
                isShimmering = true,
            )
        }
    }
}
