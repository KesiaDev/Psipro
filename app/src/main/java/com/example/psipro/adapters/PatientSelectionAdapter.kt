package com.example.psipro.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.data.entities.Patient
import com.example.psipro.databinding.ItemPatientSelectionBinding

class PatientSelectionAdapter(
    private val onPatientSelected: (Patient) -> Unit
) : ListAdapter<Patient, PatientSelectionAdapter.ViewHolder>(PatientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPatientSelectionBinding.inflate(
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
        private val binding: ItemPatientSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(patient: Patient) {
            binding.patientNameText.text = patient.name
            binding.patientInfoText.text = "${patient.cpf} - ${patient.phone}"
            
            binding.root.setOnClickListener {
                onPatientSelected(patient)
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