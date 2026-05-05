package com.alki.specinspect.features.review

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alki.specinspect.data.models.ReviewStatus
import com.alki.specinspect.ui.components.IconChip
import com.alki.specinspect.ui.components.ScenarioStepBlocks
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowBackUp
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.X
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_back
import specinspect.composeapp.generated.resources.action_open_github_source
import specinspect.composeapp.generated.resources.demo_specification_name
import specinspect.composeapp.generated.resources.label_review_requirement
import specinspect.composeapp.generated.resources.review_all_scenarios_done
import specinspect.composeapp.generated.resources.review_progress_format

private const val SWIPE_THRESHOLD_PX = 220f

@Composable
fun ScenarioReviewScreen(component: ScenarioReviewComponent) {
    val state by component.state.collectAsState()
    val card = state.cards.getOrNull(state.currentIndex)
    val specDisplayName = if (state.isDemoSpec) {
        stringResource(Res.string.demo_specification_name)
    } else {
        state.title
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { component.onBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        TablerIcons.ChevronLeft,
                        contentDescription = stringResource(Res.string.action_back),
                        tint = AppColors.Dark,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(
                        Res.string.review_progress_format,
                        (state.currentIndex + 1).coerceAtMost(state.total),
                        state.total,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.GreyViolet,
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (card == null) {
                    Text(
                        stringResource(Res.string.review_all_scenarios_done),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.GreyViolet,
                    )
                } else {
                    SwipeCard(
                        card = card,
                        index = state.currentIndex,
                        total = state.total,
                        specName = specDisplayName,
                        onOpenSource = component::onOpenSource,
                        onSwiped = component::onSwipe,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleAction(
                    background = AppColors.Coral,
                    icon = TablerIcons.X,
                    onClick = { component.onSwipe(ReviewStatus.INCORRECT) },
                    size = 64,
                )
                Spacer(Modifier.size(24.dp))
                CircleAction(
                    background = AppColors.GreyVioletSurface,
                    iconTint = AppColors.GreyViolet,
                    icon = TablerIcons.ArrowBackUp,
                    onClick = component::onUndo,
                    size = 48,
                    enabled = state.canUndo,
                )
                Spacer(Modifier.size(24.dp))
                CircleAction(
                    background = AppColors.Teal,
                    icon = TablerIcons.Check,
                    onClick = { component.onSwipe(ReviewStatus.CORRECT) },
                    size = 64,
                )
            }
        }
    }
}

@Composable
private fun SwipeCard(
    card: ReviewCardState,
    index: Int,
    total: Int,
    specName: String,
    onOpenSource: (String) -> Unit,
    onSwiped: (ReviewStatus) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember(card.id) { Animatable(0f) }
    val scrollState = rememberScrollState()
    val dragState = rememberDraggableState { delta ->
        scope.launch { offsetX.snapTo(offsetX.value + delta) }
    }

    LaunchedEffect(card.id) {
        offsetX.snapTo(0f)
        scrollState.scrollTo(0)
    }

    val rotation = (offsetX.value / 30f).coerceIn(-15f, 15f)
    val swipeProgress = (offsetX.value / SWIPE_THRESHOLD_PX).coerceIn(-1f, 1f)
    val tintColor = when {
        swipeProgress > 0 -> AppColors.Teal.copy(alpha = (swipeProgress * 0.25f).coerceAtMost(0.25f))
        swipeProgress < 0 -> AppColors.Coral.copy(alpha = (-swipeProgress * 0.25f).coerceAtMost(0.25f))
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = rotation
            }
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.White)
            .border(1.dp, AppColors.CardBorder, RoundedCornerShape(24.dp))
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    val x = offsetX.value
                    when {
                        x > SWIPE_THRESHOLD_PX -> scope.launch {
                            offsetX.animateTo(2000f, tween(220))
                            onSwiped(ReviewStatus.CORRECT)
                        }
                        x < -SWIPE_THRESHOLD_PX -> scope.launch {
                            offsetX.animateTo(-2000f, tween(220))
                            onSwiped(ReviewStatus.INCORRECT)
                        }
                        else -> scope.launch { offsetX.animateTo(0f, spring()) }
                    }
                },
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Light)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(specName.uppercase(), style = MaterialTheme.typography.labelSmall, color = AppColors.GreyViolet)
                        Spacer(Modifier.height(8.dp))
                        Text(card.title, style = MaterialTheme.typography.titleLarge, color = AppColors.Dark)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.review_progress_format, index + 1, total),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.GreyViolet,
                        )
                    }
                    if (card.sourceUrl != null) {
                        Spacer(Modifier.size(12.dp))
                        IconChip(
                            icon = TablerIcons.BrandGithub,
                            onClick = { onOpenSource(card.sourceUrl) },
                            background = AppColors.White,
                            iconTint = AppColors.Dark,
                            contentDescription = stringResource(Res.string.action_open_github_source),
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(tintColor)
                    .verticalScroll(scrollState),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ScenarioStepBlocks(
                        steps = card.steps,
                        fallbackWhenText = card.whenText,
                        fallbackThenText = card.thenText,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Light)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                ) {
                    Text(
                        stringResource(Res.string.label_review_requirement),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.GreyViolet,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        card.requirementText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Dark.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleAction(
    background: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    size: Int,
    iconTint: Color = AppColors.White,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}
