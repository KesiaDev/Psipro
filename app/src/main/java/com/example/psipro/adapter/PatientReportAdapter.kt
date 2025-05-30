package com.example.psipro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.psipro.R
import com.example.psipro.data.entities.PatientReport
import java.text.SimpleDateFormat
import java.util.*

class PatientReportAdapter(
    private var reports: List<PatientReport>,
    private val onItemClick: (PatientReport) -> Unit,
    private val onAttachmentClick: (PatientReport) -> Unit
) : RecyclerView.Adapter<PatientReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<PatientReport>) {
        reports = newReports
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitulo: TextView = itemView.findViewById(R.id.textTitulo)
        private val textData: TextView = itemView.findViewById(R.id.textData)
        private val textConteudo: TextView = itemView.findViewById(R.id.textConteudo)
        private val btnViewAttachment: ImageButton = itemView.findViewById(R.id.btnViewAttachment)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(report: PatientReport) {
            textTitulo.text = report.titulo
            textData.text = dateFormat.format(report.dataCriacao)
            textConteudo.text = report.conteudo
            itemView.setOnClickListener { onItemClick(report) }
            btnViewAttachment.visibility = if (report.arquivoAnexo.isNullOrEmpty()) View.GONE else View.VISIBLE
            btnViewAttachment.setOnClickListener { onAttachmentClick(report) }
        }
    }
} 