package com.alki.specinspect.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val storageValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: System
    }
}

internal data class AppColorPalette(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val muted: Color,
    val teal: Color,
    val coral: Color,
    val yellow: Color,
    val primaryAction: Color,
    val onPrimaryAction: Color,
    val cardBorder: Color,
    val cardBorderStrong: Color,
    val cardShadow: Color,
    val tealSurface: Color,
    val tealBorder: Color,
    val coralSurface: Color,
    val coralBorder: Color,
    val yellowSurface: Color,
    val yellowBorder: Color,
    val mutedSurface: Color,
    val mutedBorder: Color,
)

/**
 * Палитра SpecInspect — Soft Consumer Minimalism
 */
object AppColors {
    internal var currentPalette: AppColorPalette = lightAppColorPalette

    val Dark: Color get() = currentPalette.text
    val Yellow: Color get() = currentPalette.yellow
    val Coral: Color get() = currentPalette.coral
    val GreyViolet: Color get() = currentPalette.muted
    val Light: Color get() = currentPalette.background
    val Teal: Color get() = currentPalette.teal

    val White: Color get() = currentPalette.surface
    val Surface2: Color get() = currentPalette.surface2
    val PrimaryAction: Color get() = currentPalette.primaryAction
    val OnPrimaryAction: Color get() = currentPalette.onPrimaryAction
    val CardBorder: Color get() = currentPalette.cardBorder
    val CardBorderStrong: Color get() = currentPalette.cardBorderStrong
    val CardShadow: Color get() = currentPalette.cardShadow

    val TealSurface: Color get() = currentPalette.tealSurface
    val TealBorder: Color get() = currentPalette.tealBorder

    val CoralSurface: Color get() = currentPalette.coralSurface
    val CoralBorder: Color get() = currentPalette.coralBorder

    val YellowSurface: Color get() = currentPalette.yellowSurface
    val YellowBorder: Color get() = currentPalette.yellowBorder

    val GreyVioletSurface: Color get() = currentPalette.mutedSurface
    val GreyVioletBorder: Color get() = currentPalette.mutedBorder
}

internal val lightAppColorPalette = AppColorPalette(
    background = Color(0xFFF9F8F8),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF0EFF4),
    text = Color(0xFF272838),
    muted = Color(0xFF7E7F9A),
    teal = Color(0xFF77A0A9),
    coral = Color(0xFFEB9486),
    yellow = Color(0xFFF3DE8A),
    primaryAction = Color(0xFF272838),
    onPrimaryAction = Color(0xFFF9F8F8),
    cardBorder = Color(0x337E7F9A),
    cardBorderStrong = Color(0x4D7E7F9A),
    cardShadow = Color(0x1A000000),
    tealSurface = Color(0x1A77A0A9),
    tealBorder = Color(0x4D77A0A9),
    coralSurface = Color(0x1AEB9486),
    coralBorder = Color(0x4DEB9486),
    yellowSurface = Color(0x33F3DE8A),
    yellowBorder = Color(0x80F3DE8A),
    mutedSurface = Color(0x1A7E7F9A),
    mutedBorder = Color(0x4D7E7F9A),
)

internal val darkAppColorPalette = AppColorPalette(
    background = Color(0xFF15161E),
    surface = Color(0xFF1F2030),
    surface2 = Color(0xFF2A2C3F),
    text = Color(0xFFF2F2F4),
    muted = Color(0xFF8E8FAA),
    teal = Color(0xFF8FBEC8),
    coral = Color(0xFFF0A595),
    yellow = Color(0xFFF5DF95),
    primaryAction = Color(0xFF2A2C3F),
    onPrimaryAction = Color(0xFFF2F2F4),
    cardBorder = Color(0x338E8FAA),
    cardBorderStrong = Color(0x4D8E8FAA),
    cardShadow = Color(0x33000000),
    tealSurface = Color(0x268FBEC8),
    tealBorder = Color(0x668FBEC8),
    coralSurface = Color(0x26F0A595),
    coralBorder = Color(0x66F0A595),
    yellowSurface = Color(0x33F5DF95),
    yellowBorder = Color(0x80F5DF95),
    mutedSurface = Color(0x268E8FAA),
    mutedBorder = Color(0x668E8FAA),
)
