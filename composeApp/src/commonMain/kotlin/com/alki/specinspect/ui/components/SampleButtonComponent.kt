package com.alki.specinspect.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.theme.SampleColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Основная кнопка SampleButtonComponent
 */
@Composable
fun SampleButtonComponent(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = SampleColors.Clay
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = SampleColors.Paper,
            disabledContainerColor = SampleColors.Border,
            disabledContentColor = SampleColors.InkSoft
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Вторичная кнопка SampleSecondaryButton
 */
@Composable
fun SampleSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SampleColors.Clay
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Текстовая кнопка SampleTextButton
 */
@Composable
fun SampleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) SampleColors.Clay else SampleColors.InkSoft
        )
    }
}

// ===== Previews =====

@Preview
@Composable
private fun SampleButtonPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SampleButtonComponent(
            text = "Основная кнопка",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        SampleButtonComponent(
            text = "Зеленая кнопка",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            color = SampleColors.Moss
        )
        Spacer(modifier = Modifier.height(12.dp))
        SampleButtonComponent(
            text = "Отключена",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview
@Composable
private fun SampleSecondaryButtonPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SampleSecondaryButton(
            text = "Вторичная кнопка",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        SampleSecondaryButton(
            text = "Отключена",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )
    }
}

@Preview
@Composable
private fun SampleextButtonPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        SampleTextButton(
            text = "Текстовая кнопка",
            onClick = {}
        )
        Spacer(modifier = Modifier.height(12.dp))
        SampleTextButton(
            text = "Отключена",
            onClick = {},
            enabled = false
        )
    }
}
