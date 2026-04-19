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
import specinspect.composeapp.generated.resources.advent_font
import specinspect.composeapp.generated.resources.neucha_regular
import specinspect.composeapp.generated.resources.winky_sans

private val SampleColorScheme = lightColorScheme(
    primary = SpecInspectColors.Ink,
    onPrimary = SpecInspectColors.Background,
    primaryContainer = SpecInspectColors.AccentThen,
    onPrimaryContainer = SpecInspectColors.Ink,
    secondary = SpecInspectColors.Correct,
    onSecondary = SpecInspectColors.Background,
    secondaryContainer = SpecInspectColors.Correct.copy(alpha = 0.2f),
    onSecondaryContainer = SpecInspectColors.Ink,
    tertiary = SpecInspectColors.AccentThen,
    onTertiary = SpecInspectColors.Ink,
    tertiaryContainer = SpecInspectColors.AccentThen.copy(alpha = 0.35f),
    onTertiaryContainer = SpecInspectColors.Ink,
    background = SpecInspectColors.Background,
    onBackground = SpecInspectColors.Ink,
    surface = SpecInspectColors.Surface,
    onSurface = SpecInspectColors.Ink,
    surfaceVariant = SpecInspectColors.Background,
    onSurfaceVariant = SpecInspectColors.Muted,
    error = SpecInspectColors.Incorrect,
    onError = SpecInspectColors.Background,
    outline = SpecInspectColors.Muted.copy(alpha = 0.35f),
    outlineVariant = SpecInspectColors.Muted.copy(alpha = 0.2f)
)

@Composable
fun getMyFontFamily(): FontFamily {
    val display = Font(Res.font.winky_sans, FontWeight.Normal)
    val body = Font(Res.font.neucha_regular, FontWeight.Normal)
    val accent = Font(Res.font.advent_font, FontWeight.Bold)
    return FontFamily(display, body, accent)
}

private fun SampleTypography(fontFamily: FontFamily) = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 52.sp,
        lineHeight = 58.sp,
        letterSpacing = (-0.4).sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 46.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    )
)

private val SampleThemeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(38.dp)
)

@Composable
fun SampleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SampleColorScheme,
        typography = SampleTypography(getMyFontFamily()),
        shapes = SampleThemeShapes,
        content = content
    )
}
