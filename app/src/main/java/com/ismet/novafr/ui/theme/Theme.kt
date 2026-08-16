package com.ismet.novafr.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColors = darkColorScheme(
    primary = Color(0xFF3B6FE0),
    onPrimary = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF232323),
    onSurfaceVariant = Color(0xFFDADADA),
    background = Color(0xFF0A0A0A)
)

@Composable
fun NovaFRTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NovaDarkColors,
        content = content
    )
}
