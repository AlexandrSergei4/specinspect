package com.alki.salalads.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Цветовая палитра SampleColors
 * Стиль: Smart Minimalism + New Year + Cartoon
 */
object SampleColors {
    // Основные новогодние цвета
    val ChristmasRed = Color(0xFFE53935)        // Яркий красный
    val ChristmasGreen = Color(0xFF43A047)       // Ёлочный зеленый
    val ChristmasGold = Color(0xFFFFD54F)        // Золотой
    val SnowWhite = Color(0xFFFAFAFA)            // Снежно-белый
    val MidnightBlue = Color(0xFF1A237E)         // Полуночный синий

    // Мягкие пастельные оттенки для минимализма
    val SoftRed = Color(0xFFFFCDD2)              // Мягкий красный
    val SoftGreen = Color(0xFFC8E6C9)            // Мягкий зеленый
    val SoftGold = Color(0xFFFFF9C4)             // Мягкий золотой
    val SoftBlue = Color(0xFFE3F2FD)             // Мягкий голубой

    // Cartoon-style яркие акценты
    val CartoonOrange = Color(0xFFFF7043)        // Мультяшный оранжевый
    val CartoonPink = Color(0xFFEC407A)          // Мультяшный розовый
    val CartoonPurple = Color(0xFF7E57C2)        // Мультяшный фиолетовый
    val CartoonTeal = Color(0xFF26A69A)          // Мультяшный бирюзовый

    // Нейтральные тона
    val Background = Color(0xFFFFFBFE)           // Основной фон
    val Surface = Color(0xFFFFFFFF)              // Поверхность
    val SurfaceVariant = Color(0xFFF5F5F5)       // Вариант поверхности
    val OnBackground = Color(0xFF1C1B1F)         // Текст на фоне
    val OnSurface = Color(0xFF1C1B1F)            // Текст на поверхности
    val OnSurfaceVariant = Color(0xFF757575)     // Вторичный текст

    // Градиент новогодний
    val GradientStart = Color(0xFFE53935)
    val GradientMiddle = Color(0xFFFFD54F)
    val GradientEnd = Color(0xFF43A047)

    // Состояния
    val Success = ChristmasGreen
    val Error = ChristmasRed
    val Warning = ChristmasGold
    val Info = MidnightBlue

    // Карточки
    val CardBackground = Color(0xFFFFFFFF)
    val CardBorder = Color(0xFFE0E0E0)
    val CardShadow = Color(0x1A000000)

    // Кнопки
    val PrimaryButton = ChristmasRed
    val SecondaryButton = ChristmasGreen
    val DisabledButton = Color(0xFFBDBDBD)

    // Прогресс
    val ProgressBackground = Color(0xFFE0E0E0)
    val ProgressFilled = ChristmasGreen
}