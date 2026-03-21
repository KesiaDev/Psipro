package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent - Testes de Pacientes (Jetpack Compose)
 */
@RunWith(AndroidJUnit4::class)
class PacienteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cadastroDePaciente_preencheFormulario() {
        composeTestRule.setContent { StubScreens.CadastroPacienteScreen() }
        composeTestRule.onNodeWithText("Nome").performTextInput("Paciente Teste QA")
        composeTestRule.onNodeWithText("E-mail").performTextInput("paciente.teste@email.com")
        composeTestRule.onNodeWithText("Telefone").performTextInput("11999999999")
        composeTestRule.onNodeWithText("Salvar").performClick()
    }

    @Test
    fun listarPacientes_exibeLista() {
        composeTestRule.setContent { StubScreens.ListaPacientesScreen() }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Pacientes").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
