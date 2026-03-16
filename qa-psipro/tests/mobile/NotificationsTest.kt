package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent V2 - Testes de Notificações Mobile
 */
@RunWith(AndroidJUnit4::class)
class NotificationsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun notificacoes_exibeLista() {
        composeTestRule.setContent {
            // TODO: Substituir por NotificationsScreen()
            androidx.compose.material3.Text("NotificationsScreen")
        }
        composeTestRule.waitUntil(timeout = 5000) {
            composeTestRule.onAllNodesWithText("Notificações").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Avisos").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
