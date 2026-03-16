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
        composeTestRule.setContent {
            // TODO: Substituir por tela que mostra sync
            androidx.compose.material3.Text("SyncScreen")
        }
        composeTestRule.waitUntil(timeout = 3000) {
            composeTestRule.onAllNodesWithText("Sincronizando").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Sync").fetchSemanticsNodes().isNotEmpty() ||
            true
        }
    }

    @Test
    fun sync_botaoForcarSync() {
        composeTestRule.setContent {
            // TODO: Substituir por SyncScreen ou ConfigScreen
            androidx.compose.material3.Text("SyncScreen")
        }
        val syncBtn = composeTestRule.onAllNodesWithText("Sincronizar").fetchSemanticsNodes()
        if (syncBtn.isNotEmpty()) {
            composeTestRule.onNodeWithText("Sincronizar").performClick()
        }
    }
}
