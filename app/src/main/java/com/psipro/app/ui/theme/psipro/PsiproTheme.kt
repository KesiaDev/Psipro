package com.psipro.app.ui.theme.psipro

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.psipro.app.utils.AccessibilityPreferences

/**
 * Tema principal PsiPro - Dark e Light Mode.
 * Consistente com o design do PsiPro Web.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PsiproColors.Primary,
    onPrimary = PsiproColors.OnPrimary,
    primaryContainer = PsiproColors.PrimaryDark,
    onPrimaryContainer = PsiproColors.TextPrimary,
    secondary = PsiproColors.PrimaryLight,
    onSecondary = PsiproColors.TextOnPrimary,
    background = PsiproColors.BackgroundDark,
    onBackground = PsiproColors.TextPrimary,
    surface = PsiproColors.SurfaceDark,
    onSurface = PsiproColors.TextPrimary,
    surfaceVariant = PsiproColors.SurfaceVariantDark,
    onSurfaceVariant = PsiproColors.TextSecondary,
    outline = PsiproColors.BorderDefault,
    error = PsiproColors.Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PsiproColors.Primary,
    onPrimary = PsiproColors.OnPrimary,
    primaryContainer = PsiproColors.PrimaryLight,
    onPrimaryContainer = PsiproColors.TextPrimaryLight,
    secondary = PsiproColors.PrimaryDark,
    onSecondary = PsiproColors.TextPrimary,
    background = PsiproColors.BackgroundLight,
    onBackground = PsiproColors.TextPrimaryLight,
    surface = PsiproColors.SurfaceLight,
    onSurface = PsiproColors.TextPrimaryLight,
    surfaceVariant = PsiproColors.SurfaceVariantLight,
    onSurfaceVariant = PsiproColors.TextSecondaryLight,
    outline = PsiproColors.BorderLight,
    error = PsiproColors.Error,
    onError = Color.White
)

/** Tamanho mínimo de toque para botões (acessibilidade). 48.dp = padrão, 56.dp = maior. */
val LocalMinTouchTargetSize = staticCompositionLocalOf<Dp> { 48.dp }

/** Multiplicador de espaçamento (acessibilidade). 1f = normal, 1.25f = aumentado. */
val LocalSpacingScale = staticCompositionLocalOf<Float> { 1f }

@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val highContrast = AccessibilityPreferences.getHighContrast(context)
    val largerButtons = AccessibilityPreferences.getLargerButtons(context)
    val increasedSpacing = AccessibilityPreferences.getIncreasedSpacing(context)

    // fontScale é aplicado via App.attachBaseContext (Configuration) - tipografia usa sp e escala automaticamente
    val typography = PsiproTypography

    val colorScheme = when {
        highContrast && useDarkTheme -> PsiProHighContrastDarkColorScheme
        highContrast && !useDarkTheme -> PsiProHighContrastLightColorScheme
        useDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val minTouchTargetSize = if (largerButtons) 56.dp else 48.dp
    val spacingScale = if (increasedSpacing) 1.25f else 1f

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                (view.context as? Activity)?.let { activity ->
                    val window = activity.window
                    window.statusBarColor = colorScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
                }
            } catch (_: Exception) { }
        }
    }

    CompositionLocalProvider(
        LocalMinTouchTargetSize provides minTouchTargetSize,
        LocalSpacingScale provides spacingScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = androidx.compose.material3.Shapes(
                extraSmall = PsiproShapes.Small,
                small = PsiproShapes.Small,
                medium = PsiproShapes.Medium,
                large = PsiproShapes.Large,
                extraLarge = PsiproShapes.ExtraLarge
            ),
            content = content
        )
    }
}

