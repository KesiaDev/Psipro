package com.example.apppisc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.adapters.PatientSelectionAdapter
import com.example.apppisc.databinding.ActivityPatientSelectionBinding
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.ui.viewmodels.PatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PatientSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientSelectionBinding
    private val viewModel: PatientViewModel by viewModels()
    private lateinit var adapter: PatientSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Selecionar Paciente"
        }

        setupRecyclerView()
        observePatients()

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchPatients(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchPatients(it) }
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = PatientSelectionAdapter { patient ->
            val intent = Intent().apply {
                putExtra("patientId", patient.id)
                putExtra("patientName", patient.name)
                putExtra("patientPhone", patient.phone)
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PatientSelectionActivity)
            adapter = this@PatientSelectionActivity.adapter
        }
    }

    private fun observePatients() {
        lifecycleScope.launch {
            viewModel.patients.collect { patients ->
                adapter.submitList(patients)
            }
        }
    }

    private fun searchPatients(query: String) {
        lifecycleScope.launch {
            if (query.isBlank()) {
                viewModel.patients.collect { patients ->
                    adapter.submitList(patients)
                }
            } else {
                viewModel.searchPatients(query)
                viewModel.searchResults.collect { results ->
                    adapter.submitList(results)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 