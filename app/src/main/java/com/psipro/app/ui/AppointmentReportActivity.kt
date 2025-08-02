package com.psipro.app.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.databinding.ActivityAppointmentReportBinding
import com.psipro.app.viewmodel.ReportViewModel
import com.psipro.app.R

class AppointmentReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentReportBinding
    private val viewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the view model
        viewModel.apply {
            // Add any necessary setup code here
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appointment_report, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
} 



