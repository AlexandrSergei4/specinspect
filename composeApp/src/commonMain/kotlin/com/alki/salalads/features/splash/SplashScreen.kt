package com.alki.salalads.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alki.salalads.ui.components.EmojiText
import com.alki.salalads.ui.theme.SampleColors
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Splash экран с анимацией загрузки
 */
@Composable
fun SplashScreen(component: SplashComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SampleColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип
            EmojiText(
                text = " \uD83D\uDE80 ",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Название
            Text(
                text = "Template KMP APP",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = SampleColors.ChristmasGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Подзаголовок
            Text(
                text = "Start your journey",
                style = MaterialTheme.typography.bodyLarge,
                color = SampleColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Индикатор загрузки
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = SampleColors.ChristmasRed,
                strokeWidth = 4.dp
            )
        }

        // Новогоднее украшение внизу
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmojiText(
                text = "  \uD83C\uDF0D \uD83C\uDF0E️ \uD83C\uDF0F  ",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ===== Preview =====

@Preview
@Composable
private fun SplashScreenPreview() {
    val mockComponent = object : SplashComponent {
        override val state: StateFlow<SplashState>
            get() = TODO("Not yet implemented")

        override fun refresh() {
            TODO("Not yet implemented")
        }
    }
    SplashScreen(mockComponent)
}