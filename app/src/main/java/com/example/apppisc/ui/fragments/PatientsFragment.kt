package com.example.apppisc.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.CadastroPacienteActivity
import com.example.apppisc.DetalhePacienteActivity
import com.example.apppisc.databinding.FragmentPatientsBinding
import com.example.apppisc.adapter.PatientAdapter
import com.example.apppisc.viewmodel.PatientViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.fragment.findNavController
import com.example.apppisc.R

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
            }
        )
        binding.patientsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.patientsRecyclerView.adapter = patientAdapter

        // Observa a lista de pacientes
        viewLifecycleOwner.lifecycleScope.launch {
            patientViewModel.patients.collectLatest { patients ->
                patientAdapter.submitList(patients)
                if (patients.isEmpty()) {
                    Snackbar.make(binding.root, "Nenhum paciente encontrado.", Snackbar.LENGTH_SHORT).show()
                }
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
} 