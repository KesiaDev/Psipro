package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent - Testes de Prontuário (Jetpack Compose)
 */
@RunWith(AndroidJUnit4::class)
class ProntuarioTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun visualizacaoProntuario_abreTela() {
        composeTestRule.setContent { StubScreens.ProntuarioScreen() }
        composeTestRule.waitUntil(timeout = 5000) {
            composeTestRule.onAllNodesWithText("Prontuário").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun adicionarAnotacao_campoDisponivel() {
        composeTestRule.setContent { StubScreens.ProntuarioScreen() }
        composeTestRule.onNodeWithText("Anotação").performTextInput("Anotação teste QA")
        composeTestRule.onNodeWithText("Salvar").performClick()
    }
}
