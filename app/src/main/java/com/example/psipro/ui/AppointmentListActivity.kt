package com.example.psipro.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.R
import com.example.psipro.adapter.AppointmentAdapter
import com.example.psipro.databinding.ActivityAppointmentListBinding
import com.example.psipro.ui.viewmodels.AppointmentViewModel
import kotlinx.coroutines.flow.collect

class AppointmentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentListBinding
    private val viewModel: AppointmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the RecyclerView
        val adapter = AppointmentAdapter(
            onItemClick = { /* TODO: ação ao clicar em um item */ },
            onItemLongClick = { /* TODO: ação ao clicar e segurar */ },
            onRecurrenceClick = { /* TODO: ação ao clicar em recorrência */ }
        )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // Observe the data
        lifecycleScope.launchWhenStarted {
            viewModel.appointments.collect { appointments ->
                adapter.submitList(appointments)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_appointment_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
} 