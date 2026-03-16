package com.psipro.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.screens.SystemHealthScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemHealthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PsiproTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SystemHealthScreen(onBack = { finish() })
                }
            }
        }
    }
}
