package com.alki.specinspect.features.review.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alki.specinspect.features.review.ImportedSpecLibrary
import com.alki.specinspect.features.review.LibrarySourceType
import com.alki.specinspect.features.review.PersistedReviewState
import com.alki.specinspect.features.review.RequirementCard
import com.alki.specinspect.features.review.RequirementScenario
import com.alki.specinspect.features.review.ReviewDecision
import com.alki.specinspect.features.review.ReviewEngine
import com.alki.specinspect.features.review.ReviewMode
import com.alki.specinspect.features.review.ReviewStats
import com.alki.specinspect.features.review.data.ReviewStateStore
import com.alki.specinspect.features.review.platform.PlatformFolderImporter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class ReviewScreen {
    Home,
    Deck,
    Stats
}

private object ReviewFigmaAssets {
    const val AppIcon = "https://www.figma.com/api/mcp/asset/9968d940-36bd-4db0-b732-127a2c5e2bc8"
    const val PlayIcon = "https://www.figma.com/api/mcp/asset/92a18885-0b06-452e-985b-6dd47fb733ea"
    const val UploadIcon = "https://www.figma.com/api/mcp/asset/46033a69-ecd2-4a80-8eec-d5da96be9e3f"
    const val RejectIcon = "https://www.figma.com/api/mcp/asset/4ee39fa0-7842-4e75-95fd-752969207cf3"
    const val ResetIcon = "https://www.figma.com/api/mcp/asset/f0b62a3f-74c1-478c-a383-388d0fa82141"
    const val ApproveIcon = "https://www.figma.com/api/mcp/asset/1a192bab-b46f-441e-928c-d0a5b4aa660e"
    const val StatsHeaderIcon = "https://www.figma.com/api/mcp/asset/eaa81305-c26d-47b5-8be0-0b7b6b4ead3b"
    const val StatsApproveIcon = "https://www.figma.com/api/mcp/asset/506bdd1e-ee6b-4273-b5ce-db15b4755af9"
    const val StatsRejectIcon = "https://www.figma.com/api/mcp/asset/f98a9b8b-9cb6-48a1-8924-09cefe382f98"
    const val CompleteIcon = "https://www.figma.com/api/mcp/asset/53f1f16d-1acf-421f-87ce-d5fcd7da48f7"
}

private object ReviewColors {
    val PageTop = Color(0xFFF9FAFB)
    val PageBottom = Color(0xFFFFFFFF)
    val Ink = Color(0xFF101828)
    val Muted = Color(0xFF6A7282)
    val Subtle = Color(0xFF99A1AF)
    val DarkAction = Color(0xFF101828)
    val Border = Color(0xFFE5E7EB)
    val CardBorder = Color(0xFFF3F4F6)
    val Blue = Color(0xFF155DFC)
    val BlueSoft = Color(0xFFEFF6FF)
    val Green = Color(0xFF00A63E)
    val GreenSoft = Color(0xFFF0FDF4)
    val Red = Color(0xFFE7000B)
    val RedSoft = Color(0xFFFEF2F2)
    val ProgressTrack = Color(0xFFF3F4F6)
}

private val AppBackground = Brush.linearGradient(
    colors = listOf(ReviewColors.PageTop, ReviewColors.PageBottom)
)

@Composable
fun SpecReviewApp(
    modifier: Modifier = Modifier
) {
    val store = remember { ReviewStateStore() }
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<PersistedReviewState?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var screen by remember { mutableStateOf(ReviewScreen.Home) }

    LaunchedEffect(Unit) {
        val restored = store.load()
        val initialState = if (restored?.activeLibrary != null) {
            restored
        } else {
            store.importLibrary(PersistedReviewState(), loadDemoLibrary())
        }
        state = initialState
        screen = if (initialState.swipeHistory.isNotEmpty()) ReviewScreen.Deck else ReviewScreen.Home
    }

    LaunchedEffect(state) {
        state?.let { store.save(it) }
    }

    val currentState = state
    if (currentState == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ReviewColors.DarkAction)
        }
        return
    }

    val library = currentState.activeLibrary ?: return
    val cards = ReviewEngine.cardsForMode(library, currentState.swipeHistory, currentState.currentMode)
    val stats = ReviewEngine.stats(library, currentState.swipeHistory)
    val reviewSessionId = remember(currentState.currentMode, currentState.swipeHistory.size) {
        "${currentState.currentMode.name.lowercase()}-${currentState.swipeHistory.size}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        when (screen) {
            ReviewScreen.Home -> {
                HomeScreen(
                    message = message,
                    onStartDemo = {
                        scope.launch {
                            state = store.importLibrary(currentState, loadDemoLibrary())
                            screen = ReviewScreen.Deck
                            message = null
                        }
                    },
                    onImport = {
                        scope.launch {
                            val imported = PlatformFolderImporter.pickLibrary()
                            if (imported == null) {
                                message = if (PlatformFolderImporter.isSupported) {
                                    "Импорт отменен."
                                } else {
                                    "Выбор папки пока доступен только на Android."
                                }
                            } else {
                                state = store.importLibrary(currentState, imported)
                                screen = ReviewScreen.Deck
                                message = "Загружено спецификаций: ${imported.specifications.size}"
                            }
                        }
                    },
                    canImport = PlatformFolderImporter.isSupported
                )
            }

            ReviewScreen.Deck -> {
                if (cards.isNotEmpty()) {
                    DeckScreen(
                        library = library,
                        currentMode = currentState.currentMode,
                        card = cards.first(),
                        cardPosition = stats.reviewedCards + 1,
                        totalCards = stats.totalCards,
                        onOpenStats = { screen = ReviewScreen.Stats },
                        onApprove = {
                            state = store.recordDecision(
                                state = currentState,
                                cardId = cards.first().cardId,
                                decision = ReviewDecision.Approved,
                                reviewSessionId = reviewSessionId
                            )
                        },
                        onReject = {
                            state = store.recordDecision(
                                state = currentState,
                                cardId = cards.first().cardId,
                                decision = ReviewDecision.Rejected,
                                reviewSessionId = reviewSessionId
                            )
                        }
                    )
                } else if (stats.rejectedCards == 0 && currentState.currentMode == ReviewMode.Unreviewed) {
                    CompletedScreen(
                        onShowStats = { screen = ReviewScreen.Stats }
                    )
                } else {
                    StatsScreen(
                        stats = stats,
                        onBack = { screen = ReviewScreen.Deck },
                        onReplayAll = {
                            state = currentState.copy(currentMode = ReviewMode.Unreviewed)
                            screen = ReviewScreen.Deck
                        },
                        onReplayRejected = {
                            state = currentState.copy(currentMode = ReviewMode.RejectedOnly)
                            screen = ReviewScreen.Deck
                        },
                        onReplayApproved = {
                            state = currentState.copy(currentMode = ReviewMode.ApprovedOnly)
                            screen = ReviewScreen.Deck
                        }
                    )
                }
            }

            ReviewScreen.Stats -> {
                StatsScreen(
                    stats = stats,
                    onBack = { screen = if (cards.isNotEmpty()) ReviewScreen.Deck else ReviewScreen.Home },
                    onReplayAll = {
                        state = currentState.copy(currentMode = ReviewMode.Unreviewed)
                        screen = ReviewScreen.Deck
                    },
                    onReplayRejected = {
                        state = currentState.copy(currentMode = ReviewMode.RejectedOnly)
                        screen = ReviewScreen.Deck
                    },
                    onReplayApproved = {
                        state = currentState.copy(currentMode = ReviewMode.ApprovedOnly)
                        screen = ReviewScreen.Deck
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    message: String?,
    onStartDemo: () -> Unit,
    onImport: () -> Unit,
    canImport: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        GradientIconBadge(
            icon = "\uD83D\uDCC4",
            modifier = Modifier.size(80.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppText(
                text = "OpenSpec Review",
                size = 30.sp,
                lineHeight = 36.sp,
                weight = FontWeight.Medium,
                color = ReviewColors.Ink,
                align = TextAlign.Center
            )
            AppText(
                text = "Ревью спецификаций в формате карточек.\nСвайпайте вправо для корректных требований и\nвлево для некорректных.",
                size = 14.sp,
                lineHeight = 23.sp,
                color = ReviewColors.Muted,
                align = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryActionButton(
                text = "Начать с демо-спецификацией",
                icon = "▶",
                onClick = onStartDemo
            )
            SecondaryActionButton(
                text = "Загрузить свои спецификации",
                icon = "⤴",
                onClick = onImport,
                enabled = canImport
            )
        }

        if (message != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ReviewColors.Border)
            ) {
                AppText(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    size = 14.sp,
                    lineHeight = 20.sp,
                    color = ReviewColors.Muted
                )
            }
        }

        InfoCard(
            title = "Как это работает?",
            accent = ReviewColors.Ink,
            background = Color.White,
            border = ReviewColors.Border,
            lines = listOf(
                "Просматривайте требования и сценарии",
                "Свайпайте вправо если требование корректное",
                "Свайпайте влево если есть проблемы",
                "Изучайте статистику по спецификациям"
            )
        )

        SpecFormatCard()
    }
}

@Composable
private fun DeckScreen(
    library: ImportedSpecLibrary,
    currentMode: ReviewMode,
    card: RequirementCard,
    cardPosition: Int,
    totalCards: Int,
    onOpenStats: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        SwipeReviewCard(
            currentMode = currentMode,
            card = card,
            cardPosition = cardPosition,
            totalCards = totalCards,
            onApprove = onApprove,
            onReject = onReject
        )
        ReviewActionBar(
            onReject = onReject,
            onStats = onOpenStats,
            onApprove = onApprove
        )
    }
}

@Composable
private fun SwipeReviewCard(
    currentMode: ReviewMode,
    card: RequirementCard,
    cardPosition: Int,
    totalCards: Int,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val offsetX = remember(card.cardId, currentMode) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        val dragWidth = maxWidth.value
        val threshold = dragWidth * 0.18f
        val rotation = (offsetX.value / dragWidth) * 10f

        GhostSwipeCard(
            card = card,
            modifier = Modifier
                .fillMaxWidth()
                .height(668.dp)
                .offset(x = 74.dp, y = (-8).dp)
                .graphicsLayer {
                    rotationZ = 10.5f
                    alpha = 0.6f
                }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(668.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = rotation
                }
                .pointerInput(card.cardId, currentMode) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                        },
                        onDragEnd = {
                            when {
                                offsetX.value > threshold -> {
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = dragWidth * 1.2f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                        onApprove()
                                    }
                                }

                                offsetX.value < -threshold -> {
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = -dragWidth * 1.2f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                        )
                                        onReject()
                                    }
                                }

                                else -> scope.launch {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
                                }
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(0.65.dp, ReviewColors.CardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 22.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(122.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ReviewColors.PageTop, Color.White)
                            )
                        )
                        .border(
                            BorderStroke(0.65.dp, ReviewColors.CardBorder),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppText(
                            text = card.specificationName.uppercase(),
                            size = 12.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.6.sp,
                            color = ReviewColors.Subtle
                        )
                        AppText(
                            text = card.requirementTitle,
                            size = 18.sp,
                            lineHeight = 28.sp,
                            weight = FontWeight.Medium,
                            color = ReviewColors.Ink
                        )
                        val positionText = when (currentMode) {
                            ReviewMode.Unreviewed -> "$cardPosition / $totalCards"
                            ReviewMode.ApprovedOnly -> "Корректные"
                            ReviewMode.RejectedOnly -> "Некорректные"
                        }
                        AppText(
                            text = positionText,
                            size = 12.sp,
                            lineHeight = 16.sp,
                            color = ReviewColors.Subtle
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .height(330.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    card.scenarios.forEach { scenario ->
                        ScenarioPanel(
                            label = "WHEN",
                            text = scenario.whenText,
                            background = ReviewColors.BlueSoft,
                            accent = ReviewColors.Blue
                        )
                        ScenarioPanel(
                            label = "THEN",
                            text = scenario.thenText,
                            background = ReviewColors.GreenSoft,
                            accent = ReviewColors.Green
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White, ReviewColors.PageTop)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    AppText(
                        text = "REQUIREMENT",
                        size = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.6.sp,
                        color = ReviewColors.Subtle
                    )
                    Spacer(Modifier.height(8.dp))
                    AppText(
                        text = card.requirementDescription,
                        size = 14.sp,
                        lineHeight = 23.sp,
                        color = Color(0xFF364153)
                    )
                }
            }
        }
    }
}

@Composable
private fun GhostSwipeCard(
    card: RequirementCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.65.dp, ReviewColors.CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(121.dp)
                    .background(Brush.linearGradient(colors = listOf(ReviewColors.PageTop, Color.White)))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText(
                        text = card.specificationName.uppercase(),
                        size = 12.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.6.sp,
                        color = ReviewColors.Subtle
                    )
                    AppText(
                        text = card.requirementTitle,
                        size = 18.sp,
                        lineHeight = 28.sp,
                        weight = FontWeight.Medium,
                        color = ReviewColors.Ink
                    )
                    AppText(
                        text = "2 / 8",
                        size = 12.sp,
                        lineHeight = 16.sp,
                        color = ReviewColors.Subtle
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(412.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors = listOf(Color.White, ReviewColors.PageTop)))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                AppText(
                    text = "REQUIREMENT",
                    size = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.6.sp,
                    color = ReviewColors.Subtle
                )
                Spacer(Modifier.height(8.dp))
                AppText(
                    text = card.requirementDescription,
                    size = 14.sp,
                    lineHeight = 23.sp,
                    color = Color(0xFF364153)
                )
            }
        }
    }
}

@Composable
private fun ReviewActionBar(
    onReject: () -> Unit,
    onStats: () -> Unit,
    onApprove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleActionButton(
            background = Color(0xFFFB2C36),
            icon = "✕",
            size = 64.dp,
            shadow = true,
            onClick = onReject
        )
        Spacer(Modifier.width(24.dp))
        CircleActionButton(
            background = ReviewColors.Border,
            icon = "↺",
            size = 48.dp,
            shadow = false,
            onClick = onStats
        )
        Spacer(Modifier.width(24.dp))
        CircleActionButton(
            background = Color(0xFF00C950),
            icon = "✓",
            size = 64.dp,
            shadow = true,
            onClick = onApprove
        )
    }
}

@Composable
private fun StatsScreen(
    stats: ReviewStats,
    onBack: () -> Unit,
    onReplayAll: () -> Unit,
    onReplayRejected: () -> Unit,
    onReplayApproved: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                color = Color.Transparent,
                onClick = onBack
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppText(text = "←", size = 20.sp, lineHeight = 24.sp, color = Color(0xFF475467))
                }
            }
            AppText(
                text = "Статистика",
                size = 24.sp,
                lineHeight = 32.sp,
                weight = FontWeight.Medium,
                color = ReviewColors.Ink
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(0.65.dp, ReviewColors.CardBorder),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmojiIcon(icon = "\uD83D\uDCCA", size = 18.sp, tint = Color(0xFF4B5565))
                    AppText(
                        text = "Общий прогресс",
                        size = 20.sp,
                        lineHeight = 30.sp,
                        weight = FontWeight.Medium,
                        color = ReviewColors.Ink
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppText("Просмотрено", 14.sp, 20.sp, color = Color(0xFF4A5565))
                    AppText("${stats.reviewedCards} / ${stats.totalCards}", 14.sp, 20.sp, weight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ReviewColors.ProgressTrack)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stats.completionRate)
                            .height(8.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2B7FFF), Color(0xFF155DFC))
                                )
                            )
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatMetricCard(
                        title = "КОРРЕКТНЫЕ",
                        value = stats.approvedCards.toString(),
                        percent = "${(if (stats.reviewedCards == 0) 0 else (stats.approvedCards * 100 / stats.reviewedCards))}%",
                        background = ReviewColors.GreenSoft,
                        accent = ReviewColors.Green,
                        icon = "✓",
                        modifier = Modifier.fillMaxWidth(0.48f)
                    )
                    StatMetricCard(
                        title = "НЕКОРРЕКТНЫЕ",
                        value = stats.rejectedCards.toString(),
                        percent = "${(stats.rejectionRate * 100).roundToInt()}%",
                        background = ReviewColors.RedSoft,
                        accent = ReviewColors.Red,
                        icon = "✕",
                        modifier = Modifier.fillMaxWidth(0.48f)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(0.65.dp, ReviewColors.CardBorder),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppText(
                    text = "По спецификациям",
                    size = 20.sp,
                    lineHeight = 30.sp,
                    weight = FontWeight.Medium,
                    color = ReviewColors.Ink
                )
                stats.specificationStats.forEach { spec ->
                    val approvedFraction =
                        if (spec.totalCards == 0) 0f else (spec.reviewedCards - spec.rejectedCards).toFloat() / spec.totalCards
                    val rejectedFraction =
                        if (spec.totalCards == 0) 0f else spec.rejectedCards.toFloat() / spec.totalCards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AppText(
                                text = spec.displayName.lowercase(),
                                size = 14.sp,
                                lineHeight = 20.sp,
                                weight = FontWeight.Medium
                            )
                            AppText(
                                text = "${spec.reviewedCards} / ${spec.totalCards}",
                                size = 12.sp,
                                lineHeight = 16.sp,
                                color = ReviewColors.Muted
                            )
                        }
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(ReviewColors.ProgressTrack)
                        ) {
                            if (approvedFraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .width(maxWidth * approvedFraction)
                                        .height(8.dp)
                                        .background(Color(0xFF00C950))
                                )
                            }
                            if (rejectedFraction > 0f) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = maxWidth * approvedFraction)
                                        .width(maxWidth * rejectedFraction)
                                        .height(8.dp)
                                        .background(Color(0xFFFB2C36))
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AppText(
                                text = "✓ ${spec.reviewedCards - spec.rejectedCards} (${(approvedFraction * 100).roundToInt()}%)",
                                size = 12.sp,
                                lineHeight = 16.sp,
                                color = ReviewColors.Green
                            )
                            AppText(
                                text = "✕ ${spec.rejectedCards}",
                                size = 12.sp,
                                lineHeight = 16.sp,
                                color = ReviewColors.Red
                            )
                        }
                    }
                }
            }
        }

        PrimaryActionButton(
            text = "Пройти ревью заново",
            onClick = onReplayAll
        )
        SoftTintActionButton(
            text = "Просмотреть некорректные (${stats.rejectedCards})",
            background = ReviewColors.RedSoft,
            textColor = Color(0xFFC10007),
            onClick = onReplayRejected
        )
        SoftTintActionButton(
            text = "Просмотреть корректные (${stats.approvedCards})",
            background = ReviewColors.GreenSoft,
            textColor = Color(0xFF008236),
            onClick = onReplayApproved
        )
    }
}

@Composable
private fun CompletedScreen(
    onShowStats: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFFDCFCE7)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    EmojiIcon(icon = "✓", size = 32.sp, tint = Color(0xFF16A34A))
                }
            }
            AppText(
                text = "Все карточки просмотрены",
                size = 20.sp,
                lineHeight = 28.sp,
                weight = FontWeight.Medium,
                color = ReviewColors.Ink,
                align = TextAlign.Center
            )
            AppText(
                text = "Нет некорректных карточек",
                size = 14.sp,
                lineHeight = 20.sp,
                color = ReviewColors.Muted,
                align = TextAlign.Center
            )
            Button(
                onClick = onShowStats,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReviewColors.DarkAction,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 12.dp)
            ) {
                AppText(
                    text = "Посмотреть статистику",
                    size = 14.sp,
                    lineHeight = 20.sp,
                    weight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    icon: String? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ReviewColors.DarkAction,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (icon != null) {
            EmojiIcon(icon = icon, size = 18.sp, tint = Color.White)
            Spacer(Modifier.width(12.dp))
        }
        AppText(
            text = text,
            size = 16.sp,
            lineHeight = 24.sp,
            weight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.95.dp, ReviewColors.Border),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = ReviewColors.Ink,
            disabledContainerColor = Color.White,
            disabledContentColor = ReviewColors.Subtle
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        EmojiIcon(icon = icon, size = 18.sp, tint = if (enabled) ReviewColors.Ink else ReviewColors.Subtle)
        Spacer(Modifier.width(12.dp))
        AppText(
            text = text,
            size = 16.sp,
            lineHeight = 24.sp,
            weight = FontWeight.Medium,
            color = if (enabled) ReviewColors.Ink else ReviewColors.Subtle
        )
    }
}

@Composable
private fun SoftTintActionButton(
    text: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        AppText(
            text = text,
            size = 16.sp,
            lineHeight = 24.sp,
            weight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ScenarioPanel(
    label: String,
    text: String,
    background: Color,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppText(
                text = label,
                size = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.6.sp,
                weight = FontWeight.Medium,
                color = accent
            )
            AppText(
                text = text,
                size = 14.sp,
                lineHeight = 23.sp,
                color = Color(0xFF1E2939)
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    background: Color,
    icon: String,
    size: androidx.compose.ui.unit.Dp,
    shadow: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = background,
        shadowElevation = if (shadow) 12.dp else 0.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            EmojiIcon(
                icon = icon,
                size = if (size > 50.dp) 28.sp else 18.sp,
                tint = if (background == ReviewColors.Border) Color(0xFF6B7280) else Color.White
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    accent: Color,
    background: Color,
    border: Color,
    lines: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = background,
        border = BorderStroke(1.dp, border),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppText(
                text = title,
                size = 14.sp,
                lineHeight = 20.sp,
                weight = FontWeight.Medium,
                color = accent
            )
            lines.forEach { line ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText("•", 12.sp, 16.sp, color = ReviewColors.Subtle)
                    AppText(
                        text = line,
                        size = 12.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF4A5565)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecFormatCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ReviewColors.BlueSoft,
        border = BorderStroke(0.65.dp, Color(0xFFDBEAFE))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppText(
                text = "Формат спецификаций",
                size = 14.sp,
                lineHeight = 20.sp,
                weight = FontWeight.Medium,
                color = Color(0xFF1C398E)
            )
            AppText(
                text = "Выберите папку со спецификациями. Каждая спецификация должна быть в отдельной папке с файлом spec.md",
                size = 12.sp,
                lineHeight = 16.sp,
                color = ReviewColors.Blue
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.5f)
            ) {
                AppText(
                    text = "specs/\n├─ dashboard/\n│  └─ spec.md\n├─ events-feed/\n│  └─ spec.md",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    size = 12.sp,
                    lineHeight = 16.sp,
                    family = FontFamily.Monospace,
                    color = Color(0xFF193CB8)
                )
            }
        }
    }
}

@Composable
private fun GradientIconBadge(
    icon: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2B7FFF), Color(0xFF155DFC))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        EmojiIcon(icon = icon, size = 34.sp, tint = Color.White)
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    percent: String,
    background: Color,
    accent: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmojiIcon(icon = icon, size = 14.sp, tint = accent)
                AppText(
                    text = title,
                    size = 12.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.6.sp,
                    color = accent
                )
            }
            AppText(
                text = value,
                size = 24.sp,
                lineHeight = 32.sp,
                weight = FontWeight.Medium,
                color = ReviewColors.Ink
            )
            AppText(
                text = percent,
                size = 12.sp,
                lineHeight = 16.sp,
                color = ReviewColors.Muted
            )
        }
    }
}

@Composable
private fun EmojiIcon(
    icon: String,
    size: androidx.compose.ui.unit.TextUnit,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = icon,
        modifier = modifier,
        color = tint,
        fontSize = size,
        lineHeight = size
    )
}

@Composable
private fun FigmaImage(
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun AppText(
    text: String,
    size: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    weight: FontWeight = FontWeight.Normal,
    color: Color = ReviewColors.Ink,
    align: TextAlign? = null,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
    family: FontFamily = FontFamily.SansSerif
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = family,
            fontSize = size,
            lineHeight = lineHeight,
            fontWeight = weight,
            letterSpacing = letterSpacing
        ),
        color = color,
        textAlign = align
    )
}
