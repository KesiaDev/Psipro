package com.example.psipro.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfReportExporter(private val context: Context) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val paint = Paint().apply {
        textSize = 12f
    }

    fun exportPatientReport(report: PatientReport): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 em pontos (72 dpi)
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPosition = 50f

        // Cabeçalho
        paint.textSize = 20f
        canvas.drawText("Relatório do Paciente", 50f, yPosition, paint)
        yPosition += 30f

        // Dados do paciente
        paint.textSize = 14f
        canvas.drawText("Nome: ${report.patientName}", 50f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("CPF: ${report.patientCpf}", 50f, yPosition, paint)
        yPosition += 30f

        // Estatísticas
        paint.textSize = 16f
        canvas.drawText("Resumo das Consultas", 50f, yPosition, paint)
        yPosition += 20f

        paint.textSize = 12f
        canvas.drawText("Total de consultas: ${report.totalAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Consultas realizadas: ${report.completedAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Consultas canceladas: ${report.cancelledAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Consultas futuras: ${report.upcomingAppointments}", 50f, yPosition, paint)
        yPosition += 30f

        // Histórico de consultas
        paint.textSize = 16f
        canvas.drawText("Histórico de Consultas", 50f, yPosition, paint)
        yPosition += 20f

        paint.textSize = 12f
        report.appointmentHistory.forEach { appointment ->
            if (yPosition > 750f) { // Verificar se precisa de nova página
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText("Data: ${appointment.date}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Horário: ${appointment.time}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Status: ${appointment.status}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Título: ${appointment.title}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Descrição: ${appointment.description}", 50f, yPosition, paint)
            yPosition += 25f
        }

        pdfDocument.finishPage(page)

        // Salvar o arquivo
        val fileName = "relatorio_${report.patientCpf}_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun exportPeriodReport(report: PeriodReport): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create() // A4 em pontos (72 dpi)
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPosition = 50f

        // Cabeçalho
        paint.textSize = 20f
        canvas.drawText("Relatório do Período", 50f, yPosition, paint)
        yPosition += 30f

        // Período
        paint.textSize = 14f
        canvas.drawText("Período: ${report.startDate} até ${report.endDate}", 50f, yPosition, paint)
        yPosition += 30f

        // Estatísticas
        paint.textSize = 16f
        canvas.drawText("Resumo do Período", 50f, yPosition, paint)
        yPosition += 20f

        paint.textSize = 12f
        canvas.drawText("Total de consultas: ${report.totalAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Consultas realizadas: ${report.completedAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Consultas canceladas: ${report.cancelledAppointments}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Pacientes ativos: ${report.activePatients}", 50f, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Média de consultas por dia: ${String.format("%.1f", report.averageAppointmentsPerDay)}", 50f, yPosition, paint)
        yPosition += 30f

        // Detalhes das consultas
        paint.textSize = 16f
        canvas.drawText("Detalhes das Consultas", 50f, yPosition, paint)
        yPosition += 20f

        paint.textSize = 12f
        report.appointmentDetails.forEach { detail ->
            if (yPosition > 750f) { // Verificar se precisa de nova página
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText("Data: ${detail.date}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Horário: ${detail.time}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Paciente: ${detail.patientName}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Status: ${detail.status}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Título: ${detail.title}", 50f, yPosition, paint)
            yPosition += 25f
        }

        pdfDocument.finishPage(page)

        // Salvar o arquivo
        val fileName = "relatorio_periodo_${dateFormat.format(Date())}_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    private fun createPdfDocument(): PdfDocument {
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        return PrintedPdfDocument(context, printAttributes)
    }

    private fun createTitlePaint() = android.graphics.Paint().apply {
        textSize = 24f
        color = android.graphics.Color.BLACK
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private fun createSubtitlePaint() = android.graphics.Paint().apply {
        textSize = 20f
        color = android.graphics.Color.BLACK
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private fun createTextPaint() = android.graphics.Paint().apply {
        textSize = 16f
        color = android.graphics.Color.BLACK
        typeface = android.graphics.Typeface.DEFAULT
    }

    private fun createOutputFile(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${prefix}_${timestamp}.pdf"
        val directory = File(context.getExternalFilesDir(null), "reports")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }
} 