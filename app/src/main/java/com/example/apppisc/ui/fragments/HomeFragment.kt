package com.example.apppisc.ui.fragments

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
import com.example.apppisc.R
import com.example.apppisc.adapter.AppointmentAdapter
import com.example.apppisc.databinding.FragmentHomeBinding
import com.example.apppisc.ui.viewmodels.AppointmentViewModel
import com.example.apppisc.ui.viewmodels.PatientViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val appointmentViewModel: AppointmentViewModel by viewModels()
    private val patientViewModel: PatientViewModel by viewModels()
    private lateinit var appointmentAdapter: AppointmentAdapter

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
        binding.welcomeText.text = "Bem-vinda, Marian Martins!"
        setupRecyclerView()
        loadTodayAppointments()
        observeSummary()
    }

    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter(
            onItemClick = { appointment ->
                // Abrir detalhes da consulta
            },
            onItemLongClick = { appointment ->
                // Opções de editar/excluir consulta
            },
            onRecurrenceClick = { appointment ->
                Toast.makeText(requireContext(), "Série: ${appointment.recurrenceSeriesId}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.todayAppointmentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appointmentAdapter
        }
    }

    private fun loadTodayAppointments() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.getAppointmentsByDate(today).collectLatest { appointments ->
                appointmentAdapter.submitList(appointments)
                binding.noAppointmentsText.visibility = 
                    if (appointments.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun observeSummary() {
        viewLifecycleOwner.lifecycleScope.launch {
            appointmentViewModel.getTodaySummary().collect { summary ->
                binding.appointmentsCountText.text = summary.appointmentsCount.toString()
                binding.patientsCountText.text = summary.activePatients.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 