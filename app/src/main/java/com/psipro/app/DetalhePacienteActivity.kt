package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.psipro.app.data.entities.Patient
import com.psipro.app.databinding.ActivityDetalhePacienteBinding

import com.psipro.app.viewmodel.PatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.adapter.MenuAdapter
import com.psipro.app.DashboardActivity
import com.psipro.app.ProntuarioListActivity
import com.psipro.app.AnamneseActivity
import com.psipro.app.data.entities.AnamneseGroup
import kotlinx.coroutines.flow.collectLatest
import com.psipro.app.ui.AppointmentScheduleActivity
import com.psipro.app.ui.screens.DocumentosActivity

// Menu item data class
data class MenuItem(val iconRes: Int, val title: String, val onClick: () -> Unit)

@AndroidEntryPoint
class DetalhePacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhePacienteBinding
    private var currentPatientId: Long = -1
    private var patientName: String = ""
    private var patientAnamneseGroup: AnamneseGroup = AnamneseGroup.ADULTO
    private val patientViewModel: PatientViewModel by viewModels()
    private var menuAdapter: MenuAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhePacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe dados do paciente
        currentPatientId = intent.getLongExtra("PATIENT_ID", -1)
        patientName = intent.getStringExtra("PATIENT_NAME") ?: ""
        if (currentPatientId == -1L) {
            Toast.makeText(this, "Erro ao carregar paciente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cards de resumo (exemplo fixo, adapte para dados reais)
        binding.tvSessoes.text = "0"
        binding.tvAtendidas.text = "0"
        binding.tvFaltas.text = "0"

        // Carrega dados do paciente e observa mudanças
        lifecycleScope.launch {
            patientViewModel.patient.collectLatest { patient ->
                patient?.let { 
                    patientAnamneseGroup = it.anamneseGroup
                    updateMenu()
                }
            }
        }

        // Carrega o paciente
        loadPatientData()

        // Botão de voltar
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

    private fun updateMenu() {
        val menuItems = listOf(
            MenuItem(R.drawable.ic_person, "Dados principais") {
                val intent = Intent(this, DadosPessoaisActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_assignment, "Prontuário") {
                val intent = Intent(this, ProntuarioListActivity::class.java)
                intent.putExtra("patient_id", currentPatientId)
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_assignment, "Anamnese") {
                val intent = Intent(this@DetalhePacienteActivity, AnamneseActivity::class.java).apply {
                    putExtra("PATIENT_ID", currentPatientId)
                    putExtra("PATIENT_NAME", patientName)
                    putExtra("ANAMNESE_GROUP", patientAnamneseGroup.name)
                }
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_note, "Anotações da Sessão") {
                val intent = Intent(this, com.psipro.app.ui.screens.AnotacoesSessaoActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                intent.putExtra("PATIENT_NAME", patientName)
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_account_balance_wallet, "Financeiro") {
                val intent = Intent(this, FinanceiroPacienteActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                intent.putExtra("PATIENT_NAME", patientName)
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_insert_drive_file, "Documentos") { 
                val intent = Intent(this, DocumentosActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                intent.putExtra("PATIENT_NAME", patientName)
                startActivity(intent)
            },
            MenuItem(R.drawable.baseline_folder_24, "Arquivos") {
                val intent = Intent(this, com.psipro.app.ui.screens.ArquivosActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                intent.putExtra("PATIENT_NAME", patientName)
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
            }
        )

        menuAdapter = MenuAdapter(menuItems)
        binding.menuPacienteRecyclerView.adapter = menuAdapter
        binding.menuPacienteRecyclerView.layoutManager = LinearLayoutManager(this)
    }
} 



