package com.psipro.app.ui.theme.psipro

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Formas e cantos arredondados do design system PsiPro.
 * Bordas suaves e modernas (16dp padrão).
 */
object PsiproShapes {
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val ExtraLarge = RoundedCornerShape(24.dp)
    val Full = RoundedCornerShape(percent = 50)

    // Aliases
    val Card = Large
    val Button = Large
    val Input = Medium
    val Fab = RoundedCornerShape(16.dp)
}
