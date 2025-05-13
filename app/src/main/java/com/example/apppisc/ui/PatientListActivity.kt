package com.example.apppisc.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.adapter.PatientAdapter
import com.example.apppisc.databinding.ActivityPatientListBinding
import com.example.apppisc.DetalhePacienteActivity
import com.example.apppisc.ui.AppointmentScheduleActivity
import com.example.apppisc.viewmodel.PatientViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.example.apppisc.CadastroPacienteActivity

class PatientListActivity : SecureActivity() {
    private lateinit var binding: ActivityPatientListBinding
    private val viewModel: PatientViewModel by viewModels()
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        setupSearch()
        observePatients()
        binding.addPatientButton.setOnClickListener {
            android.widget.Toast.makeText(this, "FAB Clicado", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CadastroPacienteActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter(
            onItemClick = { patient ->
                // Abrir detalhes do paciente
                val intent = Intent(this, DetalhePacienteActivity::class.java).apply {
                    putExtra("PATIENT_ID", patient.id)
                }
                startActivity(intent)
            },
            onScheduleClick = { patient ->
                // Abrir agendamento de consulta
                val intent = Intent(this, AppointmentScheduleActivity::class.java).apply {
                    putExtra("patient_id", patient.id)
                    putExtra("patient_name", patient.name)
                }
                startActivity(intent)
            }
        )

        binding.patientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientListActivity)
            adapter = this@PatientListActivity.adapter
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        // Observar mudanças na busca com debounce
        viewModel.searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .filter { it.length >= 3 || it.isEmpty() }
            .onEach { query ->
                if (query.isEmpty()) {
                    viewModel.loadAllPatients()
                } else {
                    viewModel.searchPatients(query)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun observePatients() {
        lifecycleScope.launchWhenStarted {
            viewModel.patients.collect { patients ->
                adapter.submitList(patients)
                binding.emptyView.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 