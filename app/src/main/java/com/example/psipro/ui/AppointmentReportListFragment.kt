package com.example.psipro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.ui.AppointmentReportAdapter
import com.example.psipro.databinding.FragmentAppointmentReportListBinding
import com.example.psipro.ui.AppointmentReportListViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import com.example.psipro.R

class AppointmentReportListFragment : Fragment() {

    private val viewModel: AppointmentReportListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAppointmentReportListBinding.inflate(inflater, container, false)

        val adapter = AppointmentReportAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.appointmentReports.collect { reports ->
                adapter.submitList(reports)
            }
        }

        return binding.root
    }
} 