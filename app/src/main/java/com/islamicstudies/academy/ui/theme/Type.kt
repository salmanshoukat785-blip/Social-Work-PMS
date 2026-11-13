package com.islamicstudies.academy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

val AcademyTypography = Typography()

/** Urdu text styles: generous line height for Nastaliq/Naskh, direction taken from the content. */
object UrduText {
    val title = TextStyle(
        fontSize = 20.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        textDirection = TextDirection.Content
    )
    val subheading = TextStyle(
        fontSize = 18.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        textDirection = TextDirection.Content
    )
    val body = TextStyle(
        fontSize = 17.sp,
        lineHeight = 30.sp,
        textDirection = TextDirection.Content
    )
}
