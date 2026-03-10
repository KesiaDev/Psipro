package com.psipro.app.ui.theme.psipro

import androidx.compose.ui.graphics.Color

/**
 * Paleta de cores PsiPro - alinhada com o design do Web.
 * Primary: #C89B3C (dourado premium)
 * Background Dark: #0F0F10
 * Surface Dark: #1A1A1D
 */
object PsiproColors {
    // Primary - Dourado PsiPro
    val Primary = Color(0xFFC89B3C)
    val PrimaryDark = Color(0xFFA67C2E)
    val PrimaryLight = Color(0xFFE5C77A)
    val OnPrimary = Color(0xFF000000)

    // Background & Surface - Dark
    val BackgroundDark = Color(0xFF0F0F10)
    val SurfaceDark = Color(0xFF1A1A1D)
    val SurfaceVariantDark = Color(0xFF232326)
    val SurfaceElevatedDark = Color(0xFF252528)

    // Background & Surface - Light
    val BackgroundLight = Color(0xFFFAFAFA)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceVariantLight = Color(0xFFF5F5F5)
    val SurfaceElevatedLight = Color(0xFFFFFFFF)

    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA3A3A3)
    val TextTertiary = Color(0xFF737373)
    val TextOnPrimary = Color(0xFF000000)

    val TextPrimaryLight = Color(0xFF0F0F10)
    val TextSecondaryLight = Color(0xFF525252)
    val TextTertiaryLight = Color(0xFF737373)

    // Borders & Dividers
    val BorderDefault = Color(0xFF2E2E31)
    val BorderSubtle = Color(0xFF252528)
    val Divider = Color(0xFF2E2E31)

    val BorderLight = Color(0xFFE5E5E5)
    val DividerLight = Color(0xFFE5E5E5)

    // Accent borders (gold soft)
    val BorderGoldSoft = Color(0x4DC89B3C)

    // Status
    val Success = Color(0xFF22C55E)
    val SuccessBackground = Color(0xFF052E16)
    val Warning = Color(0xFFF59E0B)
    val WarningBackground = Color(0xFF422006)
    val Error = Color(0xFFEF4444)
    val ErrorBackground = Color(0xFF450A0A)
    val Info = Color(0xFF3B82F6)
    val Neutral = Color(0xFF9CA3AF)
}
