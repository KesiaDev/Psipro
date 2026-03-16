package com.psipro.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.psipro.app.CadastroPacienteActivity
import com.psipro.app.DetalhePacienteActivity
import com.psipro.app.R
import com.psipro.app.sync.work.SyncScheduler
import com.psipro.app.ui.screens.patients.PatientsScreen
import com.psipro.app.ui.theme.psipro.PsiproTheme
import com.psipro.app.ui.viewmodels.PatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class PatientsFragment : Fragment() {

    private val patientViewModel: PatientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PsiproTheme {
                    val patients by patientViewModel.patients.collectAsState(initial = emptyList())
                    val searchQuery by patientViewModel.searchQuery.collectAsState(initial = "")

                    PatientsScreen(
                        patients = patients,
                        searchQuery = searchQuery,
                        onSearchChange = { patientViewModel.setSearchQuery(it) },
                        onPatientClick = { patient ->
                            startActivity(
                                Intent(requireContext(), DetalhePacienteActivity::class.java)
                                    .putExtra("PATIENT_ID", patient.id)
                            )
                        },
                        onScheduleClick = { patient ->
                            findNavController().navigate(R.id.navigation_schedule)
                            requireActivity()
                                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                                ?.selectedItemId = R.id.navigation_schedule
                        },
                        onAddPatient = {
                            startActivity(Intent(requireContext(), CadastroPacienteActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        // Dispara sincronização ao abrir a tela Pacientes (pacientes criados na web aparecem aqui)
        SyncScheduler.enqueueBoth(requireContext(), "patients_screen")
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



