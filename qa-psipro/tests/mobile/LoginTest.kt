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
 * PsiPro QA Agent - Testes de Login (Jetpack Compose)
 * Executar: ./gradlew connectedAndroidTest
 *
 * IMPORTANTE: Substitua LoginScreen() pelo nome da sua tela de login.
 */
@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginValido_redirecionaParaDashboard() {
        composeTestRule.setContent {
            // TODO: Substituir por LoginScreen() do seu app
            androidx.compose.material3.Text("LoginScreen")
        }
        composeTestRule.onNodeWithText("E-mail").performTextInput("terapeutaclaudiacruz@gmail.com")
        composeTestRule.onNodeWithText("Senha").performTextInput("senha123")
        composeTestRule.onNodeWithText("Entrar").performClick()
        composeTestRule.waitUntil(timeout = 5000) {
            composeTestRule.onAllNodesWithText("Dashboard").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun loginInvalido_exibeErro() {
        composeTestRule.setContent {
            // TODO: Substituir por LoginScreen() do seu app
            androidx.compose.material3.Text("LoginScreen")
        }
        composeTestRule.onNodeWithText("E-mail").performTextInput("invalido@test.com")
        composeTestRule.onNodeWithText("Senha").performTextInput("senhaerrada")
        composeTestRule.onNodeWithText("Entrar").performClick()
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("inválid").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("erro").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
