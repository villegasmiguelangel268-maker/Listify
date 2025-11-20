package com.example.listify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🌞 Light Theme Colors
private val LightColors = lightColorScheme(
    primary = ListifyGreen,
    onPrimary = White,

    background = LightGray,
    onBackground = NavyBlue,

    surface = White,
    onSurface = NavyBlue,

    error = Red
)

// 🌙 Dark Theme Colors
private val DarkColors = darkColorScheme(
    primary = ListifyGreen,
    onPrimary = White,

    background = Color(0xFF121212),
    onBackground = Color(0xFFEAEAEA),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFEAEAEA),

    error = Red
)

// 🌗 Theme wrapper that auto-switches based on system settings
@Composable
fun ListifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
