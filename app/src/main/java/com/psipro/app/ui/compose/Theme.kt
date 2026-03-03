package com.psipro.app.ui.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cores centralizadas do tema premium PsiPro (apenas modo escuro).
 */
object PsiProColors {
    val GoldPrimary = Color(0xFFB8860B)
    val GoldPrimaryDark = Color(0xFF8B6914)
    val BackgroundDark = Color(0xFF121212)
    val CardDark = Color(0xFF1E1E1E)
    val BorderGoldSoft = Color(0x4DB8860B) // Gold com 30% opacidade

    val OnPrimary = Color.Black
    val OnBackground = Color.White
    val OnSurface = Color.White
    val OnSurfaceVariant = Color(0xFFAAAAAA)
}

/**
 * Dimensões padrão do tema PsiPro.
 */
object StatusColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    val Info = Color(0xFF2196F3)
    val Neutral = Color(0xFF9E9E9E)
    val SuccessBackground = Color(0xFFE8F5E8)
    val WarningBackground = Color(0xFFFFF3E0)
    val ErrorBackground = Color(0xFFFFEBEE)
    val NeutralBackground = Color(0xFFF5F5F5)
}

object PsiProDimens {
    val RadiusDefault: Dp = 20.dp
    val RadiusSmall: Dp = 12.dp
    val RadiusLarge: Dp = 24.dp
}
