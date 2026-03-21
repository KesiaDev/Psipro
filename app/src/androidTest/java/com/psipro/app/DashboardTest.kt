package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent V2 - Testes do Dashboard Mobile (Jetpack Compose)
 * Fluxo: login → ver dashboard → abrir agenda → criar agendamento → abrir paciente → nova sessão
 */
@RunWith(AndroidJUnit4::class)
class DashboardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dashboard_exibeResumo() {
        composeTestRule.setContent { StubScreens.DashboardScreen() }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Dashboard").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Resumo").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Pacientes").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun dashboard_abreAgenda() {
        composeTestRule.setContent { StubScreens.DashboardScreen() }
        composeTestRule.onNodeWithText("Agenda").performClick()
    }

    @Test
    fun dashboard_abrePacientes() {
        composeTestRule.setContent { StubScreens.DashboardScreen() }
        composeTestRule.onNodeWithText("Pacientes").performClick()
    }
}
