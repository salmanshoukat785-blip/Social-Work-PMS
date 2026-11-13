package com.islamicstudies.academy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavyContainer,
    onPrimaryContainer = Navy,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = Color(0xFF00201D),
    tertiary = Gold,
    onTertiary = Color(0xFF241A00),
    background = WarmWhite,
    onBackground = Color(0xFF1B1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = SandVariant,
    onSurfaceVariant = Color(0xFF4A4639)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Navy,
    primaryContainer = Color(0xFF1D3557),
    onPrimaryContainer = NavyContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF00201D),
    secondaryContainer = Color(0xFF123C38),
    onSecondaryContainer = EmeraldContainer,
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF241A00),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF1E2C40),
    onSurfaceVariant = Color(0xFFCBC6B8)
)

@Composable
fun AcademyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AcademyTypography,
        content = content
    )
}
