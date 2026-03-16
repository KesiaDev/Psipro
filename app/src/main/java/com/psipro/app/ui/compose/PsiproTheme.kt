package com.psipro.app.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.psipro.app.ui.theme.psipro.PsiproTheme as NewPsiproTheme

/**
 * Tema PsiPro - delega para o Design System em ui.theme.psipro.
 * Mantido para compatibilidade com telas que ainda usam este pacote.
 */
@Composable
fun PsiproTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    NewPsiproTheme(useDarkTheme = useDarkTheme, content = content)
}
