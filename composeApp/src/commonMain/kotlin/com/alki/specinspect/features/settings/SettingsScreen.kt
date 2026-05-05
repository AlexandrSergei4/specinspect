package com.alki.specinspect.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alki.specinspect.ui.components.AppTopBar
import com.alki.specinspect.ui.components.WhiteCard
import com.alki.specinspect.ui.theme.AppColors
import com.alki.specinspect.ui.theme.AppThemeMode
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.Mail
import compose.icons.tablericons.Moon
import compose.icons.tablericons.Shield
import compose.icons.tablericons.Sun
import org.jetbrains.compose.resources.stringResource
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.settings_auto_description
import specinspect.composeapp.generated.resources.settings_info_contact_developer
import specinspect.composeapp.generated.resources.settings_info_privacy_policy
import specinspect.composeapp.generated.resources.settings_info_title
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
                    Spacer(Modifier.height(16.dp))
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_auto),
                        icon = TablerIcons.DeviceDesktop,
                        mode = AppThemeMode.System,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                    Spacer(Modifier.height(8.dp))
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_light),
                        icon = TablerIcons.Sun,
                        mode = AppThemeMode.Light,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                    Spacer(Modifier.height(8.dp))
                    ThemeModeOption(
                        text = stringResource(Res.string.settings_theme_dark),
                        icon = TablerIcons.Moon,
                        mode = AppThemeMode.Dark,
                        selectedMode = themeMode,
                        onSelected = component::onThemeModeSelected,
                    )
                }
            }
            item {
                WhiteCard {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = AppColors.Dark)) {
                                append(stringResource(Res.string.settings_theme_auto))
                                append(":")
                            }
                            append(" ")
                            withStyle(SpanStyle(color = AppColors.GreyViolet)) {
                                append(stringResource(Res.string.settings_auto_description))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            item {
                WhiteCard(padding = PaddingValues(0.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_info_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.Dark,
                        modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 16.dp),
                    )
                    SettingsLinkRow(
                        text = stringResource(Res.string.settings_info_contact_developer),
                        icon = TablerIcons.Mail,
                        onClick = component::onContactDeveloper,
                    )
                    SettingsLinkRow(
                        text = stringResource(Res.string.settings_info_privacy_policy),
                        icon = TablerIcons.Shield,
                        onClick = component::onOpenPrivacyPolicy,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    text: String,
    icon: ImageVector,
    mode: AppThemeMode,
    selectedMode: AppThemeMode,
    onSelected: (AppThemeMode) -> Unit,
) {
    val selected = mode == selectedMode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AppColors.PrimaryAction else AppColors.GreyVioletSurface)
            .clickable { onSelected(mode) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) AppColors.OnPrimaryAction else AppColors.GreyViolet,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) AppColors.OnPrimaryAction else AppColors.Dark,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AppColors.Yellow),
            )
        }
    }
}

@Composable
private fun SettingsLinkRow(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AppColors.GreyVioletSurface),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.GreyVioletSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Dark,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Dark,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = TablerIcons.ExternalLink,
                contentDescription = null,
                tint = AppColors.GreyViolet,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
