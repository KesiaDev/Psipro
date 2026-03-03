package com.psipro.app.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Esquema de cores escuro premium (único tema do app).
 */
private val PsiProDarkColorScheme = darkColorScheme(
    primary = PsiProColors.GoldPrimary,
    onPrimary = PsiProColors.OnPrimary,
    secondary = PsiProColors.GoldPrimaryDark,
    onSecondary = PsiProColors.OnPrimary,
    background = PsiProColors.BackgroundDark,
    onBackground = PsiProColors.OnBackground,
    surface = PsiProColors.CardDark,
    onSurface = PsiProColors.OnSurface,
    outline = PsiProColors.GoldPrimary,
    onSurfaceVariant = PsiProColors.OnSurfaceVariant,
    surfaceVariant = Color(0xFF2D2D2D)
)

@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = true, // Ignorado: app usa apenas tema escuro
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PsiProDarkColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
