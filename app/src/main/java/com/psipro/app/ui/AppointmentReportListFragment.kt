package com.psipro.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.ui.AppointmentReportAdapter
import com.psipro.app.databinding.FragmentAppointmentReportListBinding
import com.psipro.app.ui.AppointmentReportListViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.psipro.app.R

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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appointmentReports.collect { reports ->
                    adapter.submitList(reports)
                }
            }
        }

        return binding.root
    }
} 



