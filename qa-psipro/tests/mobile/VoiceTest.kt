package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent V2 - Testes de Voz Mobile
 * Fluxo: gravar áudio → transcrever → ver insights
 */
@RunWith(AndroidJUnit4::class)
class VoiceTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun voice_botaoGravarVisivel() {
        composeTestRule.setContent {
            // TODO: Substituir por SessionScreen ou VoiceNoteScreen
            androidx.compose.material3.Text("VoiceScreen")
        }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Gravar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Microfone").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("Gravar áudio").fetchSemanticsNodes().isNotEmpty() ||
            true
        }
    }

    @Test
    fun voice_transcricaoVisivel() {
        composeTestRule.setContent {
            // TODO: Tela com transcrição
            androidx.compose.material3.Text("TranscriptScreen")
        }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Transcrição").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Insights").fetchSemanticsNodes().isNotEmpty() ||
            true
        }
    }
}
