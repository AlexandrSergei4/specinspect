package com.alki.salalads.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * На мобильных платформах системный шрифт поддерживает emoji
 */
@Composable
actual fun EmojiText(
    text: String,
    modifier: Modifier,
    fontSize: TextUnit
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize
    )
}
