package com.example.apppisc.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.R
import com.example.apppisc.adapter.PatientReportAdapter
import com.example.apppisc.data.entities.PatientReport
import com.example.apppisc.databinding.ActivityPatientReportsBinding
import com.example.apppisc.viewmodel.PatientReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentManager
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast

@AndroidEntryPoint
class PatientReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientReportsBinding
    private val viewModel: PatientReportViewModel by viewModels()
    private lateinit var adapter: PatientReportAdapter
    private var patientId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra("patientId", -1L)
        if (patientId == -1L) finish()

        adapter = PatientReportAdapter(
            reports = listOf(),
            onItemClick = { report -> showReportDetail(report) },
            onAttachmentClick = { report -> viewAttachment(report) }
        )
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewReports.adapter = adapter

        binding.fabAddReport.setOnClickListener { addNewReport() }

        viewModel.reports.observe(this) { reports ->
            adapter.updateData(reports)
        }

        viewModel.loadReportsByPatient(patientId)
    }

    private fun addNewReport() {
        val dialog = PatientReportDialog(
            context = this,
            patientId = patientId,
            report = null
        ) { novoReport ->
            viewModel.insert(novoReport)
        }
        dialog.show(supportFragmentManager, "AddReportDialog")
    }

    private fun showReportDetail(report: PatientReport) {
        val dialog = PatientReportDialog(
            context = this,
            patientId = patientId,
            report = report
        ) { updatedReport ->
            viewModel.update(updatedReport)
        }
        dialog.show(supportFragmentManager, "EditReportDialog")
    }

    private fun viewAttachment(report: PatientReport) {
        val uriString = report.arquivoAnexo
        if (uriString.isNullOrEmpty()) {
            Toast.makeText(this, "Nenhum anexo disponível", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Não foi possível abrir o anexo", Toast.LENGTH_SHORT).show()
        }
    }
} 