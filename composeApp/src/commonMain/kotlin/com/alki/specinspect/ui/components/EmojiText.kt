package com.alki.specinspect.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Платформенный компонент для отображения emoji
 * На мобильных платформах - системный шрифт
 * На WASM - специальный emoji шрифт (Noto Emoji)
 */
@Composable
expect fun EmojiText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp
)

/**
 * Платформенный компонент для отображения emoji
 * На мобильных платформах - системный шрифт
 * На WASM - специальный emoji шрифт (Noto Emoji)
 */
@Composable
fun EmojiWithText(
    emojiBefore: String? = null,
    text: String,
    emojiAfter: String? = null,
    modifier: Modifier = Modifier,
    style: TextStyle,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        emojiBefore?.let { EmojiText(emojiBefore, modifier, style.fontSize) }
        Text(
            text,
            fontSize = style.fontSize,
            style = style,
            fontWeight = fontWeight,
            color = color
        )
        emojiAfter?.let { EmojiText(emojiAfter, modifier, style.fontSize) }
    }
}
