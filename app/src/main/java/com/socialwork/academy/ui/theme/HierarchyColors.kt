package com.socialwork.academy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Three colours for the three levels of the notes:
 * chapter (strong), heading (soft teal) and sub-heading (soft amber).
 * Text colours are chosen for readable contrast in light and dark mode.
 */
data class HierarchyColors(
    val chapter: Color,
    val onChapter: Color,
    val heading: Color,
    val onHeading: Color,
    val sub: Color,
    val onSub: Color
) {
    companion object {
        val Light = HierarchyColors(
            chapter = Color(0xFF3F51B5), onChapter = Color(0xFFFFFFFF),
            heading = Color(0xFFD5EEE9), onHeading = Color(0xFF0B3B36),
            sub = Color(0xFFFFEDBF), onSub = Color(0xFF4A3500)
        )
        val Dark = HierarchyColors(
            chapter = Color(0xFF3A4799), onChapter = Color(0xFFFFFFFF),
            heading = Color(0xFF1E4A44), onHeading = Color(0xFFD0F0EA),
            sub = Color(0xFF5A4410), onSub = Color(0xFFFFE9B0)
        )
    }
}

@Composable
fun hierarchyColors(): HierarchyColors =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) HierarchyColors.Dark else HierarchyColors.Light
