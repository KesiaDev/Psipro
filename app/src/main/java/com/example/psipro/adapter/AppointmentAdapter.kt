package com.example.psipro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.Appointment
import com.example.psipro.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit,
    private val onItemLongClick: (Appointment) -> Unit,
    private val onRecurrenceClick: (Appointment) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.ViewHolder>(AppointmentDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = getItem(position)
        holder.bind(appointment)
    }

    inner class ViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.apply {
                timeText.text = appointment.startTime
                endTimeText.text = appointment.endTime
                titleText.text = appointment.patientName ?: appointment.title
                subtitleText.text = appointment.description ?: "Consulta"
                root.setOnClickListener {
                    onItemClick(appointment)
                }
                root.setOnLongClickListener {
                    onItemLongClick(appointment)
                    true
                }
            }
        }
    }

    private class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {
        override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
            return oldItem == newItem
        }
    }
} 