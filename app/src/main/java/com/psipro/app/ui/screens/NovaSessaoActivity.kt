package com.psipro.app.ui.screens

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.psipro.app.ui.compose.PsiproTheme
import dagger.hilt.android.AndroidEntryPoint
import com.psipro.app.ui.AppointmentScheduleActivity

@AndroidEntryPoint
class NovaSessaoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val patientId = intent.getLongExtra("PATIENT_ID", -1)
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Paciente"
        
        setContent {
            PsiproTheme {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier) {
                    NovaSessaoScreen(
                        patientId = patientId,
                        anotacaoId = intent.getLongExtra("ANOTACAO_ID", -1),
                        onSave = {
                            setResult(Activity.RESULT_OK)
                            finish()
                        },
                        onCancel = { finish() },
                        onAgendarReconsulta = { patientId, tipoConsulta ->
                            // Navegar para a tela de agendamento com o paciente pré-selecionado
                            val intent = Intent(this, AppointmentScheduleActivity::class.java).apply {
                                putExtra("PATIENT_ID", patientId)
                                putExtra("PATIENT_NAME", patientName)
                                putExtra("PRESELECTED_PATIENT", true)
                                putExtra("TIPO_CONSULTA", tipoConsulta)
                            }
                            startActivity(intent)
                        },
                        viewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    )
                }
            }
        }
    }
} 



