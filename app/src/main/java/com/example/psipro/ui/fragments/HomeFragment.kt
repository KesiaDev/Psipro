package com.example.psipro.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.psipro.R
import com.example.psipro.adapter.AppointmentAdapter
import com.example.psipro.databinding.FragmentHomeBinding
import com.example.psipro.viewmodel.AppointmentViewModel
import com.example.psipro.viewmodel.PatientViewModel
import com.example.psipro.data.entities.Appointment
import com.example.psipro.data.entities.AppointmentStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import android.util.Log

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var appointmentAdapter: AppointmentAdapter
    private var todayAppointments: List<Appointment> = emptyList()
    private var tomorrowAppointments: List<Appointment> = emptyList()
    private var weekAppointments: List<Appointment> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.welcomeText.text = "Bem-vindo (a)!"
        setupRecyclerView()
        loadAllAppointments()

        val tabLayout = binding.appointmentsTabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Hoje"))
        tabLayout.addTab(tabLayout.newTab().setText("Amanhã"))
        tabLayout.addTab(tabLayout.newTab().setText("Esta semana"))

        showTab(0)

        // Observar mudanças nos agendamentos
        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.allAppointments.collect { appointments ->
                Log.d("DEBUG_AGENDA", "Todos os agendamentos: ${appointments.map { it.patientName + " - " + it.date + " - " + it.startTime }}")
                loadAllAppointments() // Recarrega os agendamentos quando houver mudanças
            }
        }

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                showTab(tab.position)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(
            onItemClick = { appointment ->
                // A navegação para edição será feita a partir da tela da agenda principal.
                // Clicar aqui não fará nada por enquanto.
                Toast.makeText(requireContext(), "Edite o agendamento na tela da Agenda", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { appointment -> },
            onRecurrenceClick = { appointment -> }
        )
        binding.appointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.appointmentsRecyclerView.adapter = appointmentAdapter

        // Swipe para excluir
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val appointment = appointmentAdapter.currentList[position]
                // Confirmação antes de excluir
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Excluir consulta")
                    .setMessage("Tem certeza que deseja excluir esta consulta?")
                    .setPositiveButton("Excluir") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            appointmentViewModel.deleteAppointment(appointment)
                        }
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        appointmentAdapter.notifyItemChanged(position)
                    }
                    .show()
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.appointmentsRecyclerView)
    }

    private fun loadAllAppointments() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfToday = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfTomorrow = Calendar.getInstance().apply {
            time = tomorrow
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val endOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            add(Calendar.DAY_OF_WEEK, 7)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.getAppointmentsByDateRange(today, endOfToday).collectLatest { appointments ->
                Log.d("DEBUG_AGENDA", "Hoje: ${appointments.map { it.patientName + " - " + it.date + " - " + it.startTime }}")
                todayAppointments = appointments
                if (binding.appointmentsTabLayout.selectedTabPosition == 0) {
                    appointmentAdapter.submitList(todayAppointments)
                }

                // Atualiza os contadores de resumo do dia
                val totalHoje = todayAppointments.size
                val atendidas = todayAppointments.count { it.status == AppointmentStatus.COMPLETED }
                val faltas = todayAppointments.count { it.status == AppointmentStatus.NO_SHOW }

                binding.appointmentsCountText.text = totalHoje.toString()
                binding.tvAtendidas.text = atendidas.toString()
                binding.tvFaltas.text = faltas.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.getAppointmentsByDateRange(tomorrow, endOfTomorrow).collectLatest { appointments ->
                Log.d("DEBUG_AGENDA", "Amanhã: ${appointments.map { it.patientName + " - " + it.date + " - " + it.startTime }}")
                tomorrowAppointments = appointments
                if (binding.appointmentsTabLayout.selectedTabPosition == 1) {
                    appointmentAdapter.submitList(tomorrowAppointments)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.getAppointmentsByDateRange(startOfWeek, endOfWeek).collectLatest { appointments ->
                Log.d("DEBUG_AGENDA", "Semana: ${appointments.map { it.patientName + " - " + it.date + " - " + it.startTime }}")
                weekAppointments = appointments
                if (binding.appointmentsTabLayout.selectedTabPosition == 2) {
                    appointmentAdapter.submitList(weekAppointments)
                }
            }
        }
    }

    private fun showTab(tabIndex: Int) {
        when (tabIndex) {
            0 -> {
                appointmentAdapter.submitList(todayAppointments)
                // Atualiza o resumo do dia ao trocar para a aba HOJE
                val totalHoje = todayAppointments.size
                val atendidas = todayAppointments.count { it.status == AppointmentStatus.COMPLETED }
                val faltas = todayAppointments.count { it.status == AppointmentStatus.NO_SHOW }

                binding.appointmentsCountText.text = totalHoje.toString()
                binding.tvAtendidas.text = atendidas.toString()
                binding.tvFaltas.text = faltas.toString()
            }
            1 -> appointmentAdapter.submitList(tomorrowAppointments)
            2 -> appointmentAdapter.submitList(weekAppointments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 