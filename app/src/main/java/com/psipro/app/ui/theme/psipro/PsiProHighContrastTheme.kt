package com.psipro.app.ui.theme.psipro

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Paleta de cores em alto contraste para modo baixa visão.
 * Maximiza a diferença entre foreground e background.
 */
object PsiProHighContrastColors {

    // Light - fundo branco, texto preto puro
    val LightBackground = Color(0xFFFFFFFF)
    val LightSurface = Color(0xFFFFFFFF)
    val LightOnBackground = Color(0xFF000000)
    val LightOnSurface = Color(0xFF000000)
    val LightSurfaceVariant = Color(0xFFF0F0F0)
    val LightOnSurfaceVariant = Color(0xFF1A1A1A)
    val LightPrimary = Color(0xFF8B6914)
    val LightOnPrimary = Color(0xFFFFFFFF)
    val LightPrimaryContainer = Color(0xFFFFE082)
    val LightOnPrimaryContainer = Color(0xFF000000)
    val LightOutline = Color(0xFF000000)
    val LightError = Color(0xFFB00020)
    val LightOnError = Color(0xFFFFFFFF)

    // Dark - fundo preto, texto branco puro
    val DarkBackground = Color(0xFF000000)
    val DarkSurface = Color(0xFF000000)
    val DarkOnBackground = Color(0xFFFFFFFF)
    val DarkOnSurface = Color(0xFFFFFFFF)
    val DarkSurfaceVariant = Color(0xFF1A1A1A)
    val DarkOnSurfaceVariant = Color(0xFFE0E0E0)
    val DarkPrimary = Color(0xFFFFD54F)
    val DarkOnPrimary = Color(0xFF000000)
    val DarkPrimaryContainer = Color(0xFF8B6914)
    val DarkOnPrimaryContainer = Color(0xFFFFFFFF)
    val DarkOutline = Color(0xFFFFFFFF)
    val DarkError = Color(0xFFFF6B6B)
    val DarkOnError = Color(0xFF000000)
}

val PsiProHighContrastLightColorScheme = lightColorScheme(
    primary = PsiProHighContrastColors.LightPrimary,
    onPrimary = PsiProHighContrastColors.LightOnPrimary,
    primaryContainer = PsiProHighContrastColors.LightPrimaryContainer,
    onPrimaryContainer = PsiProHighContrastColors.LightOnPrimaryContainer,
    secondary = PsiProHighContrastColors.LightPrimary,
    onSecondary = PsiProHighContrastColors.LightOnPrimary,
    background = PsiProHighContrastColors.LightBackground,
    onBackground = PsiProHighContrastColors.LightOnBackground,
    surface = PsiProHighContrastColors.LightSurface,
    onSurface = PsiProHighContrastColors.LightOnSurface,
    surfaceVariant = PsiProHighContrastColors.LightSurfaceVariant,
    onSurfaceVariant = PsiProHighContrastColors.LightOnSurfaceVariant,
    outline = PsiProHighContrastColors.LightOutline,
    error = PsiProHighContrastColors.LightError,
    onError = PsiProHighContrastColors.LightOnError
)

val PsiProHighContrastDarkColorScheme = darkColorScheme(
    primary = PsiProHighContrastColors.DarkPrimary,
    onPrimary = PsiProHighContrastColors.DarkOnPrimary,
    primaryContainer = PsiProHighContrastColors.DarkPrimaryContainer,
    onPrimaryContainer = PsiProHighContrastColors.DarkOnPrimaryContainer,
    secondary = PsiProHighContrastColors.DarkPrimary,
    onSecondary = PsiProHighContrastColors.DarkOnPrimary,
    background = PsiProHighContrastColors.DarkBackground,
    onBackground = PsiProHighContrastColors.DarkOnBackground,
    surface = PsiProHighContrastColors.DarkSurface,
    onSurface = PsiProHighContrastColors.DarkOnSurface,
    surfaceVariant = PsiProHighContrastColors.DarkSurfaceVariant,
    onSurfaceVariant = PsiProHighContrastColors.DarkOnSurfaceVariant,
    outline = PsiProHighContrastColors.DarkOutline,
    error = PsiProHighContrastColors.DarkError,
    onError = PsiProHighContrastColors.DarkOnError
)
