package com.alki.specinspect.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Палитра SpecInspect — Soft Consumer Minimalism
 */
object AppColors {
    val Dark = Color(0xFF272838)         // основной фон кнопок, текст
    val Yellow = Color(0xFFF3DE8A)       // акценты, секция THEN
    val Coral = Color(0xFFEB9486)        // некорректные карточки, кнопка "влево"
    val GreyViolet = Color(0xFF7E7F9A)   // вторичный текст, границы
    val Light = Color(0xFFF9F8F8)        // основной фон
    val Teal = Color(0xFF77A0A9)         // корректные карточки, кнопка "вправо", WHEN

    val White = Color(0xFFFFFFFF)
    val CardBorder = Color(0x337E7F9A)   // GreyViolet @20%
    val CardBorderStrong = Color(0x4D7E7F9A) // @30%
    val CardShadow = Color(0x1A000000)

    val TealSurface = Color(0x1A77A0A9)  // 10%
    val TealBorder = Color(0x4D77A0A9)   // 30%

    val CoralSurface = Color(0x1AEB9486) // 10%
    val CoralBorder = Color(0x4DEB9486)  // 30%

    val YellowSurface = Color(0x33F3DE8A) // 20%
    val YellowBorder = Color(0x80F3DE8A)  // 50%

    val GreyVioletSurface = Color(0x1A7E7F9A) // 10%
    val GreyVioletBorder = Color(0x4D7E7F9A)  // 30%
}
