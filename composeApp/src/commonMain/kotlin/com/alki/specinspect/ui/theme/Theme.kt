package com.alki.specinspect.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
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

private val SpecInspectColorScheme = lightColorScheme(
    primary = AppColors.Dark,
    onPrimary = AppColors.Light,
    primaryContainer = AppColors.Dark,
    onPrimaryContainer = AppColors.Light,

    secondary = AppColors.Teal,
    onSecondary = AppColors.White,
    secondaryContainer = AppColors.TealSurface,
    onSecondaryContainer = AppColors.Teal,

    tertiary = AppColors.Yellow,
    onTertiary = AppColors.Dark,
    tertiaryContainer = AppColors.YellowSurface,
    onTertiaryContainer = AppColors.Dark,

    background = AppColors.Light,
    onBackground = AppColors.Dark,

    surface = AppColors.White,
    onSurface = AppColors.Dark,
    surfaceVariant = AppColors.Light,
    onSurfaceVariant = AppColors.GreyViolet,

    error = AppColors.Coral,
    onError = AppColors.White,
    errorContainer = AppColors.CoralSurface,
    onErrorContainer = AppColors.Coral,

    outline = AppColors.CardBorder,
    outlineVariant = AppColors.CardBorder
)

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
fun SampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpecInspectColorScheme,
        typography = appTypography(appFontFamily()),
        shapes = AppShapes,
        content = content
    )
}
