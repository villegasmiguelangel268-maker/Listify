package com.example.listify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
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

@Composable
fun ListifyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
