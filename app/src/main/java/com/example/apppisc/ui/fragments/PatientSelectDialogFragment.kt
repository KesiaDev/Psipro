package com.example.apppisc.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apppisc.R
import com.example.apppisc.data.entities.Patient

class PatientSelectDialogFragment(
    private val patients: List<Patient>,
    private val onPatientSelected: (Patient) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_patient_select)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.patientRecyclerView)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelar)
        val titleText = dialog.findViewById<TextView>(R.id.titleText)
        titleText.text = "Selecione o paciente"
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = PatientAdapter(patients) { patient ->
            onPatientSelected(patient)
            dismiss()
        }
        btnCancelar.setOnClickListener { dismiss() }
        return dialog
    }

    class PatientAdapter(
        private val patients: List<Patient>,
        private val onClick: (Patient) -> Unit
    ) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_patient_select, parent, false)
            return PatientViewHolder(view)
        }
        override fun getItemCount() = patients.size
        override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
            holder.bind(patients[position], onClick)
        }
        class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(patient: Patient, onClick: (Patient) -> Unit) {
                val nameText = itemView.findViewById<TextView>(R.id.patientNameText)
                nameText.text = "${patient.name} - ${patient.phone}"
                nameText.setTextColor(itemView.context.getColor(R.color.bronze_gold))
                itemView.setOnClickListener { onClick(patient) }
            }
        }
    }
} 