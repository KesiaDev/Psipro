package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.adapters.AnotacoesAdapter
import com.psipro.app.databinding.ActivityProntuarioListBinding
import com.psipro.app.ui.viewmodels.PatientNoteViewModel
import com.psipro.app.ui.viewmodels.PatientViewModel
import com.psipro.app.utils.AttachmentManager
import com.psipro.app.data.entities.PatientNote
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.TypedValue
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class ProntuarioListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProntuarioListBinding
    private lateinit var adapter: AnotacoesAdapter
    private lateinit var viewModel: PatientNoteViewModel
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var attachmentManager: AttachmentManager
    
    private var patientId: Long = 0
    private var showOnlyFavorites = false
    private var allNotes: List<PatientNote> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProntuarioListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter ID do paciente da intent
        patientId = intent.getLongExtra("patient_id", 0)
        if (patientId == 0L) {
            finish()
            return
        }

        // Inicializar ViewModels e AttachmentManager
        viewModel = ViewModelProvider(this)[PatientNoteViewModel::class.java]
        patientViewModel = ViewModelProvider(this)[PatientViewModel::class.java]
        attachmentManager = AttachmentManager(this)

        // Buscar e exibir dados do paciente
        patientViewModel.loadPatient(patientId)
        lifecycleScope.launchWhenStarted {
            patientViewModel.currentPatient.collect { patient ->
                if (patient != null) {
                    binding.tvNomePaciente.text = patient.name
                    val dados = listOfNotNull(
                        calcularIdade(patient.birthDate)?.let { "$it anos" },
                        patient.phone.takeIf { it.isNotBlank() },
                        patient.email.takeIf { it.isNotBlank() },
                        patient.cpf.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    binding.tvDadosBasicos.text = dados
                    if (patient.clinicalHistory.isNullOrBlank()) {
                        binding.tvHistoricoClinico.text = "Não informado"
                        binding.tvHistoricoClinico.setTextColor(ContextCompat.getColor(this@ProntuarioListActivity, R.color.text_gray))
                    } else {
                        binding.tvHistoricoClinico.text = patient.clinicalHistory
                        val typedValue = TypedValue()
                        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
                        binding.tvHistoricoClinico.setTextColor(typedValue.data)
                    }
                }
            }
        }
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = AnotacoesAdapter(
            onFavoriteClick = { note ->
                val updatedNote = note.copy(isFavorite = !note.isFavorite)
                viewModel.updateNote(
                    note = updatedNote,
                    onSuccess = {},
                    onError = { exception ->
                        Toast.makeText(this, "Erro ao atualizar favorito: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onItemClick = { note ->
                val intent = Intent(this, NoteEditActivity::class.java).apply {
                    putExtra("patient_id", patientId)
                    putExtra("note_id", note.id)
                }
                startActivity(intent)
            }
        )
        adapter.setAttachmentManager(attachmentManager)
        
        binding.recyclerProntuarios.apply {
            layoutManager = LinearLayoutManager(this@ProntuarioListActivity)
            adapter = this@ProntuarioListActivity.adapter
        }
    }
    
    private fun setupObservers() {
        viewModel.getNotesByPatientLiveData(patientId).observe(this) { notes: List<PatientNote>? ->
            allNotes = notes ?: emptyList()
            updateNotesList()
        }
    }
    
    private fun updateNotesList() {
        val filtered = if (showOnlyFavorites) allNotes.filter { it.isFavorite } else allNotes
        adapter.submitList(filtered)
        val btnFiltro = binding.root.findViewById<android.widget.ImageButton>(R.id.btnFiltroFavoritos)
        btnFiltro.alpha = if (showOnlyFavorites) 1.0f else 0.4f
    }
    
    private fun setupClickListeners() {
        binding.btnNovoProntuario.setOnClickListener {
            val intent = Intent(this, NoteEditActivity::class.java).apply {
                putExtra("patient_id", patientId)
            }
            startActivity(intent)
        }
        binding.btnFiltroFavoritos.setOnClickListener {
            showOnlyFavorites = !showOnlyFavorites
            updateNotesList()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnNovoProntuario.isEnabled = true
        binding.btnNovoProntuario.visibility = android.view.View.VISIBLE
    }

    // Função utilitária para calcular idade
    private fun calcularIdade(dataNascimento: java.util.Date?): Int? {
        if (dataNascimento == null) return null
        val hoje = java.util.Calendar.getInstance()
        val nascimento = java.util.Calendar.getInstance().apply { time = dataNascimento }
        var idade = hoje.get(java.util.Calendar.YEAR) - nascimento.get(java.util.Calendar.YEAR)
        if (hoje.get(java.util.Calendar.DAY_OF_YEAR) < nascimento.get(java.util.Calendar.DAY_OF_YEAR)) {
            idade--
        }
        return idade
    }
} 



