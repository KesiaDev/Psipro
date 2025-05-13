package com.example.apppisc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.apppisc.R
import com.example.apppisc.ui.compose.WeeklyAgendaScreen
import com.example.apppisc.ui.fragments.AppointmentDialogFragment
import com.example.apppisc.viewmodel.AppointmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleFragment : Fragment() {
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout XML com AppBar, BottomNav, etc.
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val composeContainer = view.findViewById<FrameLayout>(R.id.composeAgendaContainer)
        val composeView = ComposeView(requireContext())
        composeContainer.addView(composeView)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appointmentViewModel.allAppointments.collectLatest { appointments ->
                    composeView.setContent {
                        WeeklyAgendaScreen(
                            appointments = appointments,
                            onAddEvent = { /* ação do botão + */ },
                            onTimeSlotClick = { date, hour ->
                                val appointment = appointments.find { appt ->
                                    val apptDate = appt.date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                    val apptHour = appt.startTime.split(":")[0].toIntOrNull() ?: -1
                                    apptDate == date && apptHour == hour
                                }
                                val dialog = AppointmentDialogFragment()
                                val bundle = Bundle().apply {
                                    if (appointment != null) {
                                        putLong("appointment_id", appointment.id)
                                    } else {
                                        putLong("selected_date", date.atTime(hour, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                                        putString("selected_hour", String.format("%02d:00", hour))
                                        putString("selected_end_hour", String.format("%02d:00", hour + 1))
                                    }
                                }
                                dialog.arguments = bundle
                                dialog.show(parentFragmentManager, "criarAgendamento")
                            }
                        )
                    }
                }
            }
        }
    }
} 