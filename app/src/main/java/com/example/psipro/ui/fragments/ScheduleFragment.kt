package com.example.psipro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.psipro.ui.compose.PsiproTheme
import com.example.psipro.ui.compose.WeeklyAgendaScreen
import com.example.psipro.viewmodel.AppointmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.psipro.data.entities.Patient
import com.example.psipro.ui.schedule.ScheduleViewModel

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Ler paciente pré-selecionado das SharedPreferences fora do Compose
        val prefs = requireContext().getSharedPreferences("appointment_prefs", android.content.Context.MODE_PRIVATE)
        val selectedPatientId = prefs.getLong("selected_patient_id", -1)
        val selectedPatientName = prefs.getString("selected_patient_name", "")
        
        android.util.Log.d("ScheduleFragment", "Paciente pré-selecionado: ID=$selectedPatientId, Nome=$selectedPatientName")

        return ComposeView(requireContext()).apply {
            setContent {
                val appointments by appointmentViewModel.allAppointments.collectAsState(initial = emptyList())
                val patients by scheduleViewModel.patients.collectAsState(initial = emptyList())
                
                val preSelectedPatient = if (selectedPatientId != -1L) {
                    patients.find { it.id == selectedPatientId }
                } else null
                
                android.util.Log.d("ScheduleFragment", "Paciente encontrado na lista: ${preSelectedPatient?.name}")
                
                PsiproTheme {
                    WeeklyAgendaScreen(
                        appointments = appointments
                    )
                }
            }
        }
    }
} 