package com.example.psipro.ui.screens

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.psipro.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NovaSessaoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        setContent {
            PsiproTheme {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier) {
                    NovaSessaoScreen(
                        patientId = patientId,
                        onSave = {
                            setResult(Activity.RESULT_OK)
                            finish()
                        },
                        onCancel = { finish() },
                        viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    )
                }
            }
        }
    }
} 