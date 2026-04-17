package com.alki.salalads.ui.theme

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
import salalads.composeapp.generated.resources.Res
import salalads.composeapp.generated.resources.advent_font

/**
 * Цветовая схема SampleTheme
 * Smart Minimalism + New Year + Cartoon
 */
private val SampleColorScheme = lightColorScheme(
    primary = SampleColors.ChristmasRed,
    onPrimary = SampleColors.SnowWhite,
    primaryContainer = SampleColors.SoftRed,
    onPrimaryContainer = SampleColors.ChristmasRed,

    secondary = SampleColors.ChristmasGreen,
    onSecondary = SampleColors.SnowWhite,
    secondaryContainer = SampleColors.SoftGreen,
    onSecondaryContainer = SampleColors.ChristmasGreen,

    tertiary = SampleColors.ChristmasGold,
    onTertiary = SampleColors.OnBackground,
    tertiaryContainer = SampleColors.SoftGold,
    onTertiaryContainer = SampleColors.OnBackground,

    background = SampleColors.Background,
    onBackground = SampleColors.OnBackground,

    surface = SampleColors.Surface,
    onSurface = SampleColors.OnSurface,
    surfaceVariant = SampleColors.SurfaceVariant,
    onSurfaceVariant = SampleColors.OnSurfaceVariant,

    error = SampleColors.Error,
    onError = SampleColors.SnowWhite,

    outline = SampleColors.CardBorder,
    outlineVariant = SampleColors.CardBorder
)

@Composable
fun getMyFontFamily(): FontFamily {
    val regular = Font(Res.font.advent_font, FontWeight.Normal)
    val bold = Font(Res.font.advent_font, FontWeight.Bold)

    return FontFamily(regular, bold)
}
/**
 * Типографика SampleTypography
 * Шрифт: Winky Sans (используем системный sans-serif как fallback)
 * Стиль: мультяшный, дружелюбный
 */
private fun SampleTypography(fontFamily: FontFamily) = Typography(
    // Большие заголовки
    displayLarge = TextStyle(
        fontFamily = fontFamily, // Winky Sans fallback
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Заголовки
    headlineLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Названия
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
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

    // Основной текст
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Подписи
    labelLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Формы SampleThemeShapes
 * Мультяшный стиль с скругленными углами
 */
private val SampleThemeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Тема SampleTheme
 */
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