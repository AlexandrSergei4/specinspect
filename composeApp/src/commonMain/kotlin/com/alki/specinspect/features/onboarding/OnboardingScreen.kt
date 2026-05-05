package com.alki.specinspect.features.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.alki.specinspect.ui.components.SettingsIconButton
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import compose.icons.TablerIcons
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Folder
import compose.icons.tablericons.PlayerPlay
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.action_settings
import specinspect.composeapp.generated.resources.app_name
import specinspect.composeapp.generated.resources.bullet_symbol
import specinspect.composeapp.generated.resources.onboarding_bullet_review_requirements
import specinspect.composeapp.generated.resources.onboarding_bullet_stats
import specinspect.composeapp.generated.resources.onboarding_bullet_swipe_correct
import specinspect.composeapp.generated.resources.onboarding_bullet_swipe_incorrect
import specinspect.composeapp.generated.resources.onboarding_description
import specinspect.composeapp.generated.resources.onboarding_how_it_works
import specinspect.composeapp.generated.resources.onboarding_open_my_specs
import specinspect.composeapp.generated.resources.onboarding_start_demo

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
                    .background(AppColors.PrimaryAction),
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
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                color = AppColors.Dark,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.onboarding_description),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.GreyViolet,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            PrimaryButton(
                text = stringResource(Res.string.onboarding_start_demo),
                onClick = component::onStartDemo,
                leadingIcon = TablerIcons.PlayerPlay,
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(Res.string.onboarding_open_my_specs),
                onClick = component::onOpenMySpecifications,
                leadingIcon = TablerIcons.Folder,
            )
            Spacer(Modifier.height(32.dp))

            WhiteCard {
                Text(
                    text = stringResource(Res.string.onboarding_how_it_works),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppColors.Dark,
                )
                Spacer(Modifier.height(12.dp))
                BulletLine(stringResource(Res.string.onboarding_bullet_review_requirements))
                BulletLine(stringResource(Res.string.onboarding_bullet_swipe_correct))
                BulletLine(stringResource(Res.string.onboarding_bullet_swipe_incorrect))
                BulletLine(stringResource(Res.string.onboarding_bullet_stats))
            }
        }

        SettingsIconButton(
            onClick = component::onOpenSettings,
            contentDescription = stringResource(Res.string.action_settings),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
        )
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            stringResource(Res.string.bullet_symbol),
            color = AppColors.GreyViolet.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = AppColors.GreyViolet)
    }
}
