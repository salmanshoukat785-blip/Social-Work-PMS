package com.socialwork.academy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Generous line heights so Urdu (taller glyphs) is not clipped.
// Urdu is drawn with the phone's own Urdu-capable font; the reading font-size control is in Settings.
private val Base = Typography()

val AppTypography = Typography(
    headlineMedium = Base.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    titleLarge = Base.titleLarge.copy(fontWeight = FontWeight.SemiBold, lineHeight = 34.sp),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 24.sp),
    labelLarge = Base.labelLarge.copy(lineHeight = 22.sp),
    labelMedium = Base.labelMedium.copy(lineHeight = 20.sp)
)
