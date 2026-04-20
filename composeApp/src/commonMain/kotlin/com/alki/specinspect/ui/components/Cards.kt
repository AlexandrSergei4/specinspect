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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.theme.AppColors

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
