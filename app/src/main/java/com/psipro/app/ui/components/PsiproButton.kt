package com.psipro.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.psipro.app.ui.theme.psipro.LocalMinTouchTargetSize
import com.psipro.app.ui.theme.psipro.PsiproShapes
import com.psipro.app.ui.theme.psipro.PsiproSpacing

/**
 * Botão primário PsiPro - dourado, animação scale ao clicar.
 */
@Composable
fun PsiproButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100), label = "buttonScale"
    )

    val minTouchSize = LocalMinTouchTargetSize.current
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(vertical = PsiproSpacing.xs)
            .sizeIn(minWidth = minTouchSize, minHeight = minTouchSize),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = PsiproShapes.Button,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
