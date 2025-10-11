package com.example.listify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ListifyGreen,
    onPrimary = White,
    background = LightGray,
    onBackground = NavyBlue,
    surface = White,
    onSurface = NavyBlue,
    error = Red
)

private val DarkColors = darkColorScheme(
    primary = ListifyGreen,
    onPrimary = White,
    background = NavyBlue,
    onBackground = LightGray,
    surface = NavyBlue,
    onSurface = LightGray,
    error = Red
)

@Composable
fun ListifyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
