package com.pakistanlaws.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/**
 * Selectable colour themes. Each one is a deep base colour plus a brass/gold-style accent.
 * Part 3 connects this to the Settings screen.
 */
enum class AppPalette(val label: String, val primary: Color, val accent: Color) {
    EMERALD("Emerald", Color(0xFF0B6B4D), Color(0xFFC9A227)),
    NAVY("Navy", Color(0xFF1E3A8A), Color(0xFFE0A526)),
    BURGUNDY("Burgundy", Color(0xFF7B1E3A), Color(0xFFD4AF37)),
    SLATE("Slate", Color(0xFF334155), Color(0xFF0EA5A4))
}

private fun lightScheme(p: AppPalette): ColorScheme = lightColorScheme(
    primary = p.primary,
    onPrimary = Color.White,
    primaryContainer = lerp(p.primary, Color.White, 0.85f),
    onPrimaryContainer = lerp(p.primary, Color.Black, 0.55f),
    secondary = lerp(p.accent, Color.Black, 0.25f),
    onSecondary = Color.White,
    secondaryContainer = lerp(p.accent, Color.White, 0.8f),
    onSecondaryContainer = Color(0xFF3A2E00),
    background = Color(0xFFF7F9F8),
    onBackground = Color(0xFF16201C),
    surface = Color.White,
    onSurface = Color(0xFF16201C),
    surfaceVariant = Color(0xFFE6ECE9),
    onSurfaceVariant = Color(0xFF45524C),
    outline = Color(0xFF7A8782)
)

private fun darkScheme(p: AppPalette): ColorScheme = darkColorScheme(
    primary = lerp(p.primary, Color.White, 0.4f),
    onPrimary = Color(0xFF0A0F0D),
    primaryContainer = lerp(p.primary, Color.Black, 0.45f),
    onPrimaryContainer = Color(0xFFE8F3EE),
    secondary = p.accent,
    onSecondary = Color(0xFF1A1400),
    secondaryContainer = lerp(p.accent, Color.Black, 0.65f),
    onSecondaryContainer = lerp(p.accent, Color.White, 0.7f),
    background = Color(0xFF0E1412),
    onBackground = Color(0xFFE6EBE8),
    surface = Color(0xFF131A17),
    onSurface = Color(0xFFE6EBE8),
    surfaceVariant = Color(0xFF1E2824),
    onSurfaceVariant = Color(0xFFB9C4BF),
    outline = Color(0xFF55625C)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun PakistanLawsTheme(
    palette: AppPalette = AppPalette.EMERALD,
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme(palette) else lightScheme(palette),
        typography = appTypography(fontScale),
        shapes = AppShapes,
        content = content
    )
}
