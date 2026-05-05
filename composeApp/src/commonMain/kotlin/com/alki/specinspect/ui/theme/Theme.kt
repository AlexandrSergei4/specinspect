package com.alki.specinspect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import specinspect.composeapp.generated.resources.Res
import specinspect.composeapp.generated.resources.inter_font

private fun specInspectColorScheme(palette: AppColorPalette, isDark: Boolean) = if (isDark) {
    darkColorScheme(
        primary = palette.text,
        onPrimary = palette.background,
        primaryContainer = palette.surface2,
        onPrimaryContainer = palette.text,

        secondary = palette.teal,
        onSecondary = palette.background,
        secondaryContainer = palette.tealSurface,
        onSecondaryContainer = palette.teal,

        tertiary = palette.yellow,
        onTertiary = palette.background,
        tertiaryContainer = palette.yellowSurface,
        onTertiaryContainer = palette.text,

        background = palette.background,
        onBackground = palette.text,

        surface = palette.surface,
        onSurface = palette.text,
        surfaceVariant = palette.surface2,
        onSurfaceVariant = palette.muted,

        error = palette.coral,
        onError = palette.background,
        errorContainer = palette.coralSurface,
        onErrorContainer = palette.coral,

        outline = palette.cardBorder,
        outlineVariant = palette.cardBorderStrong,
    )
} else {
    lightColorScheme(
        primary = palette.text,
        onPrimary = palette.background,
        primaryContainer = palette.text,
        onPrimaryContainer = palette.background,

        secondary = palette.teal,
        onSecondary = palette.surface,
        secondaryContainer = palette.tealSurface,
        onSecondaryContainer = palette.teal,

        tertiary = palette.yellow,
        onTertiary = palette.text,
        tertiaryContainer = palette.yellowSurface,
        onTertiaryContainer = palette.text,

        background = palette.background,
        onBackground = palette.text,

        surface = palette.surface,
        onSurface = palette.text,
        surfaceVariant = palette.background,
        onSurfaceVariant = palette.muted,

        error = palette.coral,
        onError = palette.surface,
        errorContainer = palette.coralSurface,
        onErrorContainer = palette.coral,

        outline = palette.cardBorder,
        outlineVariant = palette.cardBorderStrong,
    )
}

@Composable
private fun appFontFamily(): FontFamily {
    val regular = Font(Res.font.inter_font, FontWeight.Normal)
    val medium = Font(Res.font.inter_font, FontWeight.Medium)
    val bold = Font(Res.font.inter_font, FontWeight.Bold)
    return FontFamily(regular, medium, bold)
}

private fun appTypography(fontFamily: FontFamily) = Typography(
    displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 40.sp),
    displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 30.sp, lineHeight = 36.sp),
    displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),

    headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),

    titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),

    bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SampleTheme(
    themeMode: AppThemeMode = AppThemeMode.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val palette = if (isDark) darkAppColorPalette else lightAppColorPalette
    AppColors.currentPalette = palette

    MaterialTheme(
        colorScheme = specInspectColorScheme(palette, isDark),
        typography = appTypography(appFontFamily()),
        shapes = AppShapes,
        content = content
    )
}
