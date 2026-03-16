package com.psipro.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.psipro.app.ui.compose.PsiproTheme
import com.psipro.app.ui.compose.WeeklyAgendaScreen
import com.psipro.app.viewmodel.AppointmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.psipro.app.data.entities.Patient
import com.psipro.app.sync.work.SyncScheduler
import com.psipro.app.ui.schedule.ScheduleViewModel

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // Sincroniza pacientes e agendamentos ao abrir a Agenda (web → app)
        SyncScheduler.enqueueBoth(requireContext(), "agenda_screen")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Verificar se há um paciente pré-selecionado nos argumentos
        val preselectedPatientId = arguments?.getLong("PRESELECTED_PATIENT_ID", -1) ?: -1
        val preselectedPatientName = arguments?.getString("PRESELECTED_PATIENT_NAME") ?: ""
        val tipoConsulta = arguments?.getString("TIPO_CONSULTA")
        
        android.util.Log.d("ScheduleFragment", "Paciente pré-selecionado: ID=$preselectedPatientId, Nome=$preselectedPatientName")

        return ComposeView(requireContext()).apply {
            setContent {
                val appointments by appointmentViewModel.allAppointments.collectAsState(initial = emptyList())
                val patients by scheduleViewModel.patients.collectAsState(initial = emptyList())
                
                val preSelectedPatient = if (preselectedPatientId != -1L) {
                    patients.find { it.id == preselectedPatientId }
                } else null
                
                android.util.Log.d("ScheduleFragment", "Paciente encontrado na lista: ${preSelectedPatient?.name}")
                
                PsiproTheme {
                    WeeklyAgendaScreen(
                        appointments = appointments,
                        appointmentViewModel = appointmentViewModel,
                        preselectedPatient = preSelectedPatient,
                        tipoConsulta = tipoConsulta
                    )
                }
            }
        }
    }
} 



