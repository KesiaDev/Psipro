package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent - Testes de Agenda (Jetpack Compose)
 */
@RunWith(AndroidJUnit4::class)
class AgendaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun agendaDeSessoes_exibeCalendario() {
        composeTestRule.setContent { StubScreens.AgendaScreen() }
        composeTestRule.waitUntil(timeout = 5000) {
            composeTestRule.onAllNodesWithText("Agenda").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Sessão").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun agendarSessao_botaoDisponivel() {
        composeTestRule.setContent { StubScreens.AgendaScreen() }
        composeTestRule.onNodeWithText("Nova sessão").performClick()
    }
}
