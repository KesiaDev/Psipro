package com.example.apppisc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.R
import com.example.apppisc.data.entities.Patient
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class PatientAdapter(
    private val onItemClick: (Patient) -> Unit,
    private val onScheduleClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = getItem(position)
        holder.bind(patient)
    }

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.patientNameText)
        private val infoText: TextView = itemView.findViewById(R.id.patientInfoText)
        private val viewDetailsButton: MaterialButton = itemView.findViewById(R.id.viewDetailsButton)
        private val scheduleButton: MaterialButton = itemView.findViewById(R.id.scheduleButton)

        fun bind(patient: Patient) {
            nameText.text = patient.name
            infoText.text = buildPatientInfo(patient)

            itemView.setOnClickListener {
                onItemClick(patient)
            }

            viewDetailsButton.setOnClickListener {
                onItemClick(patient)
            }

            scheduleButton.setOnClickListener {
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