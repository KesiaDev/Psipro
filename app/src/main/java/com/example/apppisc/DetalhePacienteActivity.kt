package com.example.apppisc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.databinding.ActivityDetalhesPacienteBinding
import com.example.apppisc.ui.screens.WhatsAppHistoryActivity
import com.example.apppisc.adapter.MenuAdapter

class DetalhePacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetalhesPacienteBinding
    private var currentPatientId: Long = -1
    private var patientName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPacienteBinding.inflate(layoutInflater)
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

        // Menu lateral
        val menuItems = listOf(
            MenuItem(R.drawable.ic_person, "Dados principais") {
                val intent = Intent(this, DadosPessoaisActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                startActivity(intent)
            },
            MenuItem(R.drawable.ic_assignment, "Prontuário") { /* abrir prontuário */ },
            MenuItem(R.drawable.ic_anamnese, "Anamnese") { /* abrir anamnese */ },
            MenuItem(R.drawable.ic_note, "Anotações da Sessão") { /* abrir anotações */ },
            MenuItem(R.drawable.ic_account_balance_wallet, "Financeiro") { /* abrir financeiro */ },
            MenuItem(R.drawable.ic_attach_money, "Cobranças") { /* abrir cobranças */ },
            MenuItem(R.drawable.ic_insert_drive_file, "Documentos") { /* abrir documentos */ },
            MenuItem(R.drawable.baseline_folder_24, "Arquivos") { /* abrir arquivos */ },
            MenuItem(R.drawable.ic_whatsapp, "WhatsApp") {
                val intent = Intent(this, WhatsAppHistoryActivity::class.java)
                intent.putExtra("PATIENT_ID", currentPatientId)
                intent.putExtra("PATIENT_NAME", patientName)
                startActivity(intent)
            }
        )
        val adapter = MenuAdapter(menuItems)
        binding.menuPacienteRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuPacienteRecyclerView.adapter = adapter

        // Botão exportar (exemplo)
        binding.btnExportar.setOnClickListener {
            Toast.makeText(this, "Exportar dados do paciente", Toast.LENGTH_SHORT).show()
        }

        // Botão de voltar
        binding.btnVoltarFicha.setOnClickListener {
            finish()
        }
    }
}

// Menu item data class
data class MenuItem(val iconRes: Int, val title: String, val onClick: () -> Unit) 