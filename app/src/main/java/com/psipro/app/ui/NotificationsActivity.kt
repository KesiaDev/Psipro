package com.psipro.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.psipro.app.ui.screens.NotificationsScreen
import com.psipro.app.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PsiproTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NotificationsScreen(
                        onBack = { finish() }
                    )
                }
            }
        }
    }
} 