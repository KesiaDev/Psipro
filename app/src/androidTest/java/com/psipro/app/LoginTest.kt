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
 * PsiPro QA - Testes de Login (Jetpack Compose)
 * Executar: ./gradlew connectedAndroidTest
 *
 * Usa StubScreens.LoginScreen() para testes isolados.
 * Ao integrar no app, substitua por LoginScreen() real.
 */
@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginValido_redirecionaParaDashboard() {
        composeTestRule.setContent { StubScreens.LoginScreen() }
        composeTestRule.onNodeWithText("E-mail").performTextInput("terapeutaclaudiacruz@gmail.com")
        composeTestRule.onNodeWithText("Senha").performTextInput("senha123")
        composeTestRule.onNodeWithText("Entrar").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Dashboard").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun loginInvalido_exibeErro() {
        composeTestRule.setContent { StubScreens.LoginScreen() }
        composeTestRule.onNodeWithText("E-mail").performTextInput("invalido@test.com")
        composeTestRule.onNodeWithText("Senha").performTextInput("senhaerrada")
        composeTestRule.onNodeWithText("Entrar").performClick()
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("inválido").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("inválid", substring = true).fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("erro", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
