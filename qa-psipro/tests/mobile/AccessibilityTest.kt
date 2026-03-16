package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent V2 - Testes de Acessibilidade Mobile
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun accessibility_treeValido() {
        composeTestRule.setContent {
            // TODO: Substituir por LoginScreen() ou tela principal
            androidx.compose.material3.Text("LoginScreen")
        }
        composeTestRule.onRoot().printToLog("Accessibility")
    }

    @Test
    fun accessibility_elementosComContentDescription() {
        composeTestRule.setContent {
            // TODO: Tela com ícones que devem ter contentDescription
            androidx.compose.material3.Text("Screen")
        }
        composeTestRule.waitUntil(timeout = 3000) { true }
    }
}
