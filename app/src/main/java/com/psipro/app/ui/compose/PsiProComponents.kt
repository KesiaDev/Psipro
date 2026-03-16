package com.psipro.app.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * Card reutilizável com estilo PsiPro (tema escuro).
 */
@Composable
fun PsiProCard(
    modifier: Modifier = Modifier,
    borderGold: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(PsiProDimens.RadiusDefault)
    var cardModifier = modifier
        .clip(shape)
        .then(
            if (onClick != null) Modifier.clickable { onClick() }
            else Modifier
        )
    if (borderGold) {
        cardModifier = cardModifier.border(1.dp, PsiProColors.BorderGoldSoft, shape)
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = PsiProColors.CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Botão primário PsiPro (dourado).
 */
@Composable
fun PsiProPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PsiProDimens.RadiusDefault)),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = PsiProColors.GoldPrimary,
            contentColor = PsiProColors.OnPrimary
        ),
        shape = RoundedCornerShape(PsiProDimens.RadiusDefault)
    ) {
        Text(text)
    }
}

/**
 * TopBar reutilizável PsiPro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsiProTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PsiProColors.CardDark,
            titleContentColor = PsiProColors.OnSurface
        )
    )
}
