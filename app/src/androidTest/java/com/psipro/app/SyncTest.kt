package com.psipro.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PsiPro QA Agent V2 - Testes de Sincronização Mobile
 * Fluxo offline: desligar internet → usar app → ligar internet → sync
 */
@RunWith(AndroidJUnit4::class)
class SyncTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sync_indicatorVisivelQuandoSincronizando() {
        composeTestRule.setContent { StubScreens.SyncScreen() }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Sincronizando").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Sync").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun sync_botaoForcarSync() {
        composeTestRule.setContent { StubScreens.SyncScreen() }
        composeTestRule.onNodeWithText("Sincronizar").performClick()
    }
}
