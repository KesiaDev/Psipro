package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.psipro.app.databinding.ActivityDetalhePacienteBinding
import com.psipro.app.viewmodel.PatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.adapter.MenuAdapter
import com.psipro.app.DashboardActivity
import com.psipro.app.ui.screens.QuickSessionActivity
import com.psipro.app.ui.screens.AnotacoesSessaoActivity
import com.psipro.app.utils.AccessibilityPreferences
import com.psipro.app.utils.WebNavigator

// Menu item data class
data class MenuItem(val iconRes: Int, val title: String, val onClick: () -> Unit)

@AndroidEntryPoint
class DetalhePacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhePacienteBinding
    private var currentPatientId: Long = -1
    private var patientName: String = ""
    private val patientViewModel: PatientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AccessibilityPreferences.getHighContrast(this)) {
            setTheme(com.psipro.app.R.style.Theme_Psipro_HighContrast)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhePacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPatientId = intent.getLongExtra("PATIENT_ID", -1)
        patientName = intent.getStringExtra("PATIENT_NAME") ?: ""
        if (currentPatientId == -1L) {
            Toast.makeText(this, "Erro ao carregar paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPatientData()
        setupMenu()
        lifecycleScope.launch {
            patientViewModel.patient.collectLatest { patient ->
                patient?.let { updateCounters(it) }
            }
        }

        binding.btnVoltarFicha.setOnClickListener {
            finish()
        }
        
        // O diálogo será mostrado quando necessário
    }

    override fun onResume() {
        super.onResume()
        // Recarrega os dados do paciente quando voltar de outras telas
        loadPatientData()
    }

    private fun loadPatientData() {
        patientViewModel.getPatient(currentPatientId)
    }

    private fun updateCounters(patient: com.psipro.app.data.entities.Patient) {
        patientViewModel.getSessionCounts(patient.id) { sessions, attended, absences ->
            runOnUiThread {
                binding.tvSessoes.text = sessions.toString()
                binding.tvAtendidas.text = attended.toString()
                binding.tvFaltas.text = absences.toString()
            }
        }
    }

    private fun setupMenu() {
        val menuItems = listOf(
            MenuItem(R.drawable.ic_assignment, "Ver sessões") {
                val intent = Intent(this, AnotacoesSessaoActivity::class.java).apply {
                    putExtra("PATIENT_ID", currentPatientId)
                    putExtra("PATIENT_NAME", patientName)
                }
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_note, "Nova anotação rápida") {
                val intent = Intent(this, QuickSessionActivity::class.java).apply {
                    putExtra("PATIENT_ID", currentPatientId)
                    putExtra("PATIENT_NAME", patientName)
                }
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_calendar, "Agendar consulta") {
                val intent = Intent(this, DashboardActivity::class.java).apply {
                    putExtra("NAVIGATE_TO", R.id.navigation_schedule)
                    putExtra("PRESELECTED_PATIENT_ID", currentPatientId)
                    putExtra("PRESELECTED_PATIENT_NAME", patientName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_language, "Abrir no Web") {
                WebNavigator.openPatientOnWeb(this, currentPatientId)
            }
        )
        binding.menuPacienteRecyclerView.adapter = MenuAdapter(menuItems)
        binding.menuPacienteRecyclerView.layoutManager = LinearLayoutManager(this)
    }
} 



