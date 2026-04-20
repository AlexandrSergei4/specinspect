package com.alki.specinspect.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.PrimaryButton
import com.alki.specinspect.ui.components.SecondaryButton
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Folder
import compose.icons.tablericons.PlayerPlay

@Composable
fun OnboardingScreen(component: OnboardingComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Логотип
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.Dark),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TablerIcons.FileText,
                    contentDescription = null,
                    tint = AppColors.Yellow,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(32.dp))

            Text(
                text = "OpenSpec Review",
                style = MaterialTheme.typography.displayMedium,
                color = AppColors.Dark,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Ревью спецификаций в формате карточек. Свайпайте вправо для корректных требований и влево для некорректных.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.GreyViolet,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            PrimaryButton(
                text = "Начать с демо-спецификацией",
                onClick = component::onStartDemo,
                leadingIcon = TablerIcons.PlayerPlay,
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = "Перейти к моим спецификациям",
                onClick = component::onOpenMySpecifications,
                leadingIcon = TablerIcons.Folder,
            )
            Spacer(Modifier.height(32.dp))

            WhiteCard {
                Text(
                    text = "Как это работает?",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppColors.Dark,
                )
                Spacer(Modifier.height(12.dp))
                BulletLine("Просматривайте требования и сценарии")
                BulletLine("Свайпайте вправо если требование корректное")
                BulletLine("Свайпайте влево если есть проблемы")
                BulletLine("Изучайте статистику по спецификациям")
            }
        }
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("•", color = AppColors.GreyViolet.copy(alpha = 0.5f), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = AppColors.GreyViolet)
    }
}
