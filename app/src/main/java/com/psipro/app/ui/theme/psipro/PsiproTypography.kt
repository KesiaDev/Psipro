package com.psipro.app.ui.theme.psipro

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Aplica escala à tipografia para modo baixa visão (ex: fontScale 1.3f). */
fun Typography.scale(scale: Float): Typography = Typography(
    displayLarge = displayLarge.scaleTextStyle(scale),
    displayMedium = displayMedium.scaleTextStyle(scale),
    displaySmall = displaySmall.scaleTextStyle(scale),
    headlineLarge = headlineLarge.scaleTextStyle(scale),
    headlineMedium = headlineMedium.scaleTextStyle(scale),
    headlineSmall = headlineSmall.scaleTextStyle(scale),
    titleLarge = titleLarge.scaleTextStyle(scale),
    titleMedium = titleMedium.scaleTextStyle(scale),
    titleSmall = titleSmall.scaleTextStyle(scale),
    bodyLarge = bodyLarge.scaleTextStyle(scale),
    bodyMedium = bodyMedium.scaleTextStyle(scale),
    bodySmall = bodySmall.scaleTextStyle(scale),
    labelLarge = labelLarge.scaleTextStyle(scale),
    labelMedium = labelMedium.scaleTextStyle(scale),
    labelSmall = labelSmall.scaleTextStyle(scale)
)

private fun TextStyle.scaleTextStyle(scale: Float): TextStyle = copy(
    fontSize = (fontSize.value * scale).sp,
    lineHeight = (lineHeight.value * scale).sp,
    letterSpacing = (letterSpacing.value * scale).sp
)

// Usar font padrão do sistema (Roboto) até adicionar fontes customizadas
// Para Inter/Poppins: adicione fontes em res/font e use FontFamily(Font(R.font.inter_regular))
private val DefaultFontFamily = FontFamily.Default

/**
 * Tipografia PsiPro - elegante e legível.
 * Hierarquia: HeadlineLarge, TitleMedium, BodyMedium, LabelSmall
 */
val PsiproTypography = Typography(
    // Display - títulos de página
    headlineLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels
    labelLarge = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
