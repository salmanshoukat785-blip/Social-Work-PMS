package com.socialwork.academy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = NavyDark,
    secondary = Purple,
    onSecondary = Color.White,
    secondaryContainer = PurpleLight,
    onSecondaryContainer = Color(0xFF2B0A57),
    background = SurfaceLight,
    onBackground = Color(0xFF1B1B21),
    surface = SurfaceLight,
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE5E3F0),
    onSurfaceVariant = Color(0xFF47464F),
    // Green is used for "correct answer" in quizzes.
    tertiary = Color(0xFF1B6B3A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8F0D0),
    onTertiaryContainer = Color(0xFF0B3D1A)
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = NavyDark,
    primaryContainer = Navy,
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = PurpleLight,
    onSecondary = Color(0xFF3F1478),
    secondaryContainer = Color(0xFF5A2A98),
    onSecondaryContainer = Color(0xFFF0DBFF),
    background = SurfaceDark,
    onBackground = Color(0xFFE4E1EC),
    surface = SurfaceDark,
    onSurface = Color(0xFFE4E1EC),
    surfaceVariant = CardDark,
    onSurfaceVariant = Color(0xFFC8C5D0),
    tertiary = Color(0xFF8FDBA6),
    onTertiary = Color(0xFF0B3D1A),
    tertiaryContainer = Color(0xFF1F5A33),
    onTertiaryContainer = Color(0xFFCFF5D8)
)

@Composable
fun SocialWorkTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
