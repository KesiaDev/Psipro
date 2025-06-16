package com.example.psipro.ui.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.R
import com.example.psipro.ui.compose.WeeklyAgendaScreen
import com.example.psipro.ui.fragments.AppointmentDialogFragment
import com.example.psipro.viewmodel.AppointmentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import dagger.hilt.android.AndroidEntryPoint
import com.example.psipro.ui.compose.PsiproTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.*
import com.example.psipro.ui.compose.AppointmentForm
import androidx.compose.ui.window.Dialog

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
                        PsiproTheme {
                            var showAppointmentForm by remember { mutableStateOf(false) }
                            var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
                            var selectedHour by remember { mutableStateOf<Int?>(null) }

                            WeeklyAgendaScreen(
                                appointments = appointments,
                                onAddEvent = { /* ação do botão + */ },
                                onTimeSlotClick = { date, hour ->
                                    selectedDate = date
                                    selectedHour = hour
                                    showAppointmentForm = true
                                }
                            )

                            if (showAppointmentForm && selectedDate != null && selectedHour != null) {
                                Dialog(onDismissRequest = { showAppointmentForm = false }) {
                                    AppointmentForm(
                                        initialDate = selectedDate!!.toString(),
                                        initialStartTime = "%02d:00".format(selectedHour),
                                        initialEndTime = "%02d:00".format(selectedHour!! + 1),
                                        onSave = { _, _, _, _, _, _, _, _, _, _, _ ->
                                            // salvar evento no banco
                                            showAppointmentForm = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 