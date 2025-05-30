package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.Patient
import com.example.psipro.databinding.ItemPatientBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PatientAdapter(
    private val onItemClick: (Patient) -> Unit,
    private val onScheduleClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.ViewHolder>(PatientDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPatientBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(patient: Patient) {
            binding.patientNameText.text = patient.name
            binding.patientInfoText.text = buildPatientInfo(patient)
            
            binding.viewDetailsButton.setOnClickListener {
                onItemClick(patient)
            }
            
            binding.scheduleButton.setOnClickListener {
                onScheduleClick(patient)
            }
        }

        private fun buildPatientInfo(patient: Patient): String {
            return buildString {
                append("CPF: ${patient.cpf}")
                append(" • Tel: ${patient.phone}")
                append(" • Nascimento: ${dateFormat.format(patient.birthDate)}")
            }
        }
    }

    private class PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem == newItem
        }
    }
} 