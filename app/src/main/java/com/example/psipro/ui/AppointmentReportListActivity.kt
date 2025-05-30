package com.example.psipro.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.ui.AppointmentReportAdapter
import com.example.psipro.databinding.ActivityAppointmentReportListBinding
import com.example.psipro.ui.AppointmentReportListViewModel
import com.example.psipro.R

class AppointmentReportListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentReportListBinding
    private val viewModel: AppointmentReportListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentReportListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the RecyclerView
        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set up the adapter
        val adapter = AppointmentReportAdapter()
        recyclerView.adapter = adapter

        // Set up the ViewModel
        lifecycleScope.launchWhenStarted {
            viewModel.appointmentReports.collect { reports ->
                adapter.submitList(reports)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appointment_report_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
} 