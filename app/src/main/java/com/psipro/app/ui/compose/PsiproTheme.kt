package com.psipro.app.ui.compose

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import android.util.Log

// Cores de status que funcionam bem em ambos os temas
object StatusColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)
    val Neutral = Color(0xFF9E9E9E)
    
    // Cores de fundo para status
    val SuccessBackground = Color(0xFFE8F5E8)
    val WarningBackground = Color(0xFFFFF3E0)
    val ErrorBackground = Color(0xFFFFEBEE)
    val NeutralBackground = Color(0xFFF5F5F5)
}

private val LightColors = lightColorScheme(
    primary = Color(0xFFB8860B), // bronze_gold equilibrado
    onPrimary = Color.White,
    secondary = Color(0xFF8B6914), // bronze_gold_dark
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White, // 100% branco
    onSurface = Color.Black,
    outline = Color(0xFFB8860B), // bronze_gold para bordas
    onSurfaceVariant = Color(0xFF6C757D), // text_gray
    surfaceVariant = Color(0xFFF5F5F5), // cinza muito claro para cartões
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8860B), // bronze_gold equilibrado
    onPrimary = Color.Black,
    secondary = Color(0xFF8B6914), // bronze_gold_dark
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    outline = Color(0xFFB8860B), // bronze_gold para bordas
    onSurfaceVariant = Color(0xFFAAAAAA), // text_hint
    surfaceVariant = Color(0xFF2D2D2D), // cinza escuro para cartões
)

@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Log detalhado para debug
    Log.d("PsiproTheme", "=== TEMA DEBUG ===")
    Log.d("PsiproTheme", "useDarkTheme parameter: $useDarkTheme")
    Log.d("PsiproTheme", "isSystemInDarkTheme(): ${isSystemInDarkTheme()}")
    Log.d("PsiproTheme", "Selected theme: ${if (useDarkTheme) "DARK" else "LIGHT"}")
    
    val selectedColors = if (useDarkTheme) DarkColors else LightColors
    
    // Log detalhado das cores selecionadas
    Log.d("PsiproTheme", "=== CORES SELECIONADAS ===")
    Log.d("PsiproTheme", "Surface: ${selectedColors.surface}")
    Log.d("PsiproTheme", "OnSurface: ${selectedColors.onSurface}")
    Log.d("PsiproTheme", "Background: ${selectedColors.background}")
    Log.d("PsiproTheme", "OnBackground: ${selectedColors.onBackground}")
    Log.d("PsiproTheme", "SurfaceVariant: ${selectedColors.surfaceVariant}")
    Log.d("PsiproTheme", "OnSurfaceVariant: ${selectedColors.onSurfaceVariant}")
    Log.d("PsiproTheme", "Primary: ${selectedColors.primary}")
    Log.d("PsiproTheme", "OnPrimary: ${selectedColors.onPrimary}")
    Log.d("PsiproTheme", "=== FIM DEBUG ===")
    
    MaterialTheme(
        colorScheme = selectedColors,
        typography = Typography(),
        content = content
    )
} 



