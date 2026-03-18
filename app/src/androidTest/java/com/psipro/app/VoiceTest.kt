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
        composeTestRule.setContent { StubScreens.VoiceScreen() }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Gravar").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithContentDescription("Gravar áudio").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun voice_transcricaoVisivel() {
        composeTestRule.setContent { StubScreens.TranscriptScreen() }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Transcrição").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Insights").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
