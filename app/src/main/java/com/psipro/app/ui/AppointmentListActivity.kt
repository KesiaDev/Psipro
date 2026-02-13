package com.psipro.app.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.R
import com.psipro.app.adapter.AppointmentAdapter
import com.psipro.app.databinding.ActivityAppointmentListBinding
import com.psipro.app.ui.viewmodels.AppointmentViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

        // Observe the data usando repeatOnLifecycle ao invés de launchWhenStarted
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appointments.collect { appointments ->
                    adapter.submitList(appointments)
                }
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



