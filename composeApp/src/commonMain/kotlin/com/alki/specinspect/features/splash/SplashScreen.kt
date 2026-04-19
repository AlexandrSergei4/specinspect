package com.alki.specinspect.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alki.specinspect.ui.theme.SampleColors
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
            .background(
                Brush.linearGradient(
                    colors = listOf(SampleColors.Mist, SampleColors.Paper)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SampleColors.Clay, SampleColors.Moss)
                        )
                    )
                    .size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83D\uDCC4",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "OpenSpec Review",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = SampleColors.Ink
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Swipe through requirements with calm focus",
                style = MaterialTheme.typography.bodyMedium,
                color = SampleColors.InkSoft
            )

            Spacer(modifier = Modifier.height(28.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = SampleColors.Clay,
                strokeWidth = 3.dp
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .clip(RoundedCornerShape(999.dp)),
            color = SampleColors.Fog
        ) {
            Text(
                text = "Open specs. Make sharper calls.",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = SampleColors.InkSoft
            )
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
