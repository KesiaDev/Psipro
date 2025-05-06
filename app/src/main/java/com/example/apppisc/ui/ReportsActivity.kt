package com.example.apppisc.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.apppisc.R
import com.example.apppisc.data.entities.AppointmentStatus
import com.example.apppisc.reports.PdfReportExporter
import com.example.apppisc.viewmodel.ReportViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class ReportsActivity : AppCompatActivity() {
    private lateinit var viewModel: ReportViewModel
    private lateinit var pdfExporter: PdfReportExporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]
        pdfExporter = PdfReportExporter(this)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.btnPeriodReport).setOnClickListener {
            showDateRangePicker()
        }

        findViewById<MaterialButton>(R.id.btnPatientReport).setOnClickListener {
            val patientId = intent.getLongExtra("patient_id", 0)
            if (patientId == 0L) {
                Toast.makeText(this, "Selecione um paciente primeiro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generatePatientReport(patientId)
        }

        findViewById<MaterialButton>(R.id.btnPatientHistory).setOnClickListener {
            val patientId = intent.getLongExtra("patient_id", 0)
            if (patientId == 0L) {
                Toast.makeText(this, "Selecione um paciente primeiro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generatePatientHistory(patientId)
        }
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecione o período")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Date(selection.first)
            val endDate = Date(selection.second)
            generatePeriodReport(startDate, endDate)
        }

        dateRangePicker.show(supportFragmentManager, "date_range_picker")
    }

    private fun generatePatientReport(patientId: Long) {
        lifecycleScope.launch {
            viewModel.generatePatientReport(
                patientId = patientId,
                onSuccess = { report ->
                    val file = pdfExporter.exportPatientReport(report)
                    shareFile(file)
                },
                onError = { e ->
                    Toast.makeText(this@ReportsActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun generatePeriodReport(startDate: Date, endDate: Date) {
        lifecycleScope.launch {
            viewModel.generatePeriodReport(
                startDate = startDate,
                endDate = endDate,
                onSuccess = { report ->
                    val file = pdfExporter.exportPeriodReport(report)
                    shareFile(file)
                },
                onError = { e ->
                    Toast.makeText(this@ReportsActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun generatePatientHistory(patientId: Long) {
        lifecycleScope.launch {
            viewModel.generatePatientAppointmentHistory(
                patientId = patientId,
                status = AppointmentStatus.COMPLETED,
                onSuccess = { report ->
                    val file = pdfExporter.exportPatientReport(report)
                    shareFile(file)
                },
                onError = { e ->
                    Toast.makeText(this@ReportsActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Nenhum aplicativo encontrado para abrir PDF", Toast.LENGTH_LONG).show()
        }
    }
} 