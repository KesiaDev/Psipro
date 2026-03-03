package com.psipro.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.CadastroPacienteActivity
import com.psipro.app.DetalhePacienteActivity
import com.psipro.app.databinding.FragmentPatientsBinding
import com.psipro.app.adapter.PatientAdapter
import com.psipro.app.ui.viewmodels.PatientViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController
import com.psipro.app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import android.app.Activity
import android.net.Uri
@AndroidEntryPoint
class PatientsFragment : Fragment() {
    private var _binding: FragmentPatientsBinding? = null
    private val binding get() = _binding!!
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var patientAdapter: PatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.addPatientFab.setOnClickListener {
            startActivity(Intent(requireContext(), CadastroPacienteActivity::class.java))
        }

        patientAdapter = PatientAdapter(
            onItemClick = { patient ->
                val intent = Intent(requireContext(), DetalhePacienteActivity::class.java)
                intent.putExtra("PATIENT_ID", patient.id)
                startActivity(intent)
            },
            onScheduleClick = { patient ->
                // Navegar para a Agenda (ScheduleFragment)
                parentFragment?.parentFragmentManager?.let { fm ->
                    // Tenta obter o NavController do fragmento pai
                    val navController = findNavController()
                    navController.navigate(R.id.navigation_schedule)
                } ?: run {
                    // Fallback: tenta obter o NavController da Activity
                    requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.navigation_schedule
                }
            },
            onDeleteClick = { patient ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Excluir paciente")
                    .setMessage("Tem certeza que deseja excluir este paciente?")
                    .setPositiveButton("Excluir") { _, _ ->
                        patientViewModel.deletePatient(patient)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        binding.patientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.patientsRecyclerView.adapter = patientAdapter

        // Observa a lista de pacientes
        viewLifecycleOwner.lifecycleScope.launch {
            patientViewModel.patients.collectLatest { patients ->
                patientAdapter.submitList(patients)
            }
        }

        // Busca
        binding.searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                patientViewModel.setSearchQuery(s?.toString() ?: "")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_patient_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_patients -> {
                Toast.makeText(requireContext(), "Importação de pacientes disponível na versão Web.", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_test -> {
                Toast.makeText(requireContext(), "Use a versão Web para importação em lote.", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 



