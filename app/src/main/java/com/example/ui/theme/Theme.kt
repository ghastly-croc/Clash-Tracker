package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClashDarkColorScheme = darkColorScheme(
    primary = ClashGold,
    onPrimary = Color(0xFF111522),
    primaryContainer = Color(0xFF3B2F13),
    onPrimaryContainer = ClashGoldLight,

    secondary = ClashIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF262B54),
    onSecondaryContainer = Color(0xFFE0E7FF),

    tertiary = ClashGreen,
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),

    background = ClashDarkBg,
    onBackground = ClashTextPrimary,

    surface = ClashDarkSurface,
    onSurface = ClashTextPrimary,
    surfaceVariant = ClashDarkSurfaceVariant,
    onSurfaceVariant = ClashTextSecondary,

    outline = ClashBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClashDarkColorScheme,
        typography = Typography,
        content = content
    )
}
