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

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val appointments by appointmentViewModel.allAppointments.collectAsState(initial = emptyList())
                PsiproTheme {
                    WeeklyAgendaScreen(appointments = appointments)
                }
            }
        }
    }
} 