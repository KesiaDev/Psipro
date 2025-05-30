package com.example.psipro.ui.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme(
    primary = Color(0xFFB08D57), // bronze_gold
    onPrimary = Color.White,
    secondary = Color(0xFFE5D1B8), // bronze_gold_light
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFE5D1B8), // bronze_gold_light
    onSurface = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB08D57),
    onPrimary = Color.Black,
    secondary = Color(0xFFE5D1B8),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
)

@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
} 