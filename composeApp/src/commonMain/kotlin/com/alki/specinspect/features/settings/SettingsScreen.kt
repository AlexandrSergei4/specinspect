package com.alki.specinspect.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import com.alki.specinspect.ui.theme.AppThemeMode
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.settings_theme_auto
import specinspect.composeapp.generated.resources.settings_theme_dark
import specinspect.composeapp.generated.resources.settings_theme_light
import specinspect.composeapp.generated.resources.settings_theme_title
import specinspect.composeapp.generated.resources.settings_title

@Composable
fun SettingsScreen(component: SettingsComponent) {
    val themeMode by component.themeMode.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Light)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                AppTopBar(
                    title = stringResource(Res.string.settings_title),
                    onBack = component::onBack,
                )
            }
            item {
                WhiteCard {
                    Text(
                        text = stringResource(Res.string.settings_theme_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.Dark,
                    )
                    Spacer(Modifier.height(12.dp))
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_auto),
                        mode = AppThemeMode.System,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_light),
                        mode = AppThemeMode.Light,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_dark),
                        mode = AppThemeMode.Dark,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    text: String,
    mode: AppThemeMode,
    selectedMode: AppThemeMode,
    onSelected: (AppThemeMode) -> Unit,
) {
    val selected = mode == selectedMode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AppColors.Surface2 else AppColors.White)
            .border(BorderStroke(1.dp, if (selected) AppColors.CardBorderStrong else AppColors.CardBorder), RoundedCornerShape(12.dp))
            .clickable { onSelected(mode) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelected(mode) },
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Dark,
            modifier = Modifier.weight(1f),
        )
    }
}
