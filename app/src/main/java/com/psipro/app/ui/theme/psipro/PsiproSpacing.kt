package com.psipro.app.ui.theme.psipro

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sistema de espaçamento PsiPro - consistente com o Web.
 * Base: 4dp (unidade fundamental)
 */
object PsiproSpacing {
    val none: Dp = 0.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    // Aliases comuns
    val cardPadding: Dp = md
    val screenPadding: Dp = md
    val listItemSpacing: Dp = sm
}
