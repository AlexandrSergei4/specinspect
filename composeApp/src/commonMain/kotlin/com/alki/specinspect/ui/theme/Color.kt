package com.alki.specinspect.ui.theme

import androidx.compose.ui.graphics.Color

object SpecInspectColors {
    val Ink = Color(0xFF272838)
    val AccentThen = Color(0xFFF3DE8A)
    val Incorrect = Color(0xFFEB9486)
    val Muted = Color(0xFF7E7F9A)
    val Background = Color(0xFFF9F8F8)
    val Correct = Color(0xFF77A0A9)
    val Surface = Color(0xFFFFFFFF)
}

// Compatibility alias for existing UI modules while we migrate to SpecInspect naming.
object SampleColors {
    val Clay = SpecInspectColors.AccentThen
    val Moss = SpecInspectColors.Ink
    val DustyRose = SpecInspectColors.Incorrect
    val Butter = SpecInspectColors.AccentThen
    val Mist = SpecInspectColors.Background
    val Paper = SpecInspectColors.Surface
    val Fog = SpecInspectColors.Background
    val Ink = SpecInspectColors.Ink
    val InkSoft = SpecInspectColors.Muted
    val Border = SpecInspectColors.Muted.copy(alpha = 0.35f)
    val Positive = SpecInspectColors.Correct
    val Negative = SpecInspectColors.Incorrect
}
