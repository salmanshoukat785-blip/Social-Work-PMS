package com.socialwork.academy.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1B6B3A)

private fun scheme(c: PaletteColors, dark: Boolean): ColorScheme {
    val onSurface = if (dark) Color(0xFFE4E1EC) else Color(0xFF1B1B21)
    // Green is used for "correct answer" in quizzes, whatever the theme.
    return if (dark) darkColorScheme(
        primary = c.primary, onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer, onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.secondary, onSecondary = c.onSecondary,
        secondaryContainer = c.secondaryContainer, onSecondaryContainer = c.onSecondaryContainer,
        background = c.background, onBackground = onSurface,
        surface = c.background, onSurface = onSurface,
        surfaceVariant = c.surfaceVariant, onSurfaceVariant = c.onSurfaceVariant,
        tertiary = Color(0xFF8FDBA6), onTertiary = Color(0xFF0B3D1A),
        tertiaryContainer = Color(0xFF1F5A33), onTertiaryContainer = Color(0xFFCFF5D8)
    ) else lightColorScheme(
        primary = c.primary, onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer, onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.secondary, onSecondary = c.onSecondary,
        secondaryContainer = c.secondaryContainer, onSecondaryContainer = c.onSecondaryContainer,
        background = c.background, onBackground = onSurface,
        surface = c.background, onSurface = onSurface,
        surfaceVariant = c.surfaceVariant, onSurfaceVariant = c.onSurfaceVariant,
        tertiary = Green, onTertiary = Color.White,
        tertiaryContainer = Color(0xFFC8F0D0), onTertiaryContainer = Color(0xFF0B3D1A)
    )
}

@Composable
fun SocialWorkTheme(
    darkTheme: Boolean,
    palette: ThemePalette = ThemePalette.Emerald,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = scheme(palette.colors(darkTheme), darkTheme),
        typography = AppTypography
    ) {
        CompositionLocalProvider(LocalHierarchyColors provides palette.levels(darkTheme), content = content)
    }
}
