package com.psipro.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.psipro.app.ui.theme.psipro.LocalMinTouchTargetSize
import com.psipro.app.ui.theme.psipro.PsiproShapes

/**
 * FAB moderno PsiPro - tamanho maior, sombra suave, animação scale ao clicar.
 */
@Composable
fun PsiproFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Adicionar",
            modifier = Modifier.size(28.dp)
        )
    }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100), label = "fabScale"
    )

    val minTouchSize = LocalMinTouchTargetSize.current
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .sizeIn(minWidth = minTouchSize, minHeight = minTouchSize),
        shape = PsiproShapes.Fab,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 4.dp
        ),
        interactionSource = interactionSource,
        content = { icon() }
    )
}
