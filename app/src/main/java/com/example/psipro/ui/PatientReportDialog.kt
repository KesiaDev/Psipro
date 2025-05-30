package com.example.psipro.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.psipro.R
import com.example.psipro.data.entities.PatientReport
import java.util.Date

class PatientReportDialog(
    private val context: Context,
    private val patientId: Long,
    private val report: PatientReport? = null,
    private val onSave: (PatientReport) -> Unit
) : DialogFragment() {
    private var selectedAttachment: String? = null
    private var selectAttachmentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAttachment = it.toString()
            view?.findViewById<TextView>(R.id.textAnexo)?.text = it.lastPathSegment ?: it.toString()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_patient_report, null)
        val editTitulo = view.findViewById<EditText>(R.id.editTitulo)
        val editConteudo = view.findViewById<EditText>(R.id.editConteudo)
        val textAnexo = view.findViewById<TextView>(R.id.textAnexo)
        val btnSelecionarAnexo = view.findViewById<View>(R.id.btnSelecionarAnexo)

        // Preencher campos se for edição
        report?.let {
            editTitulo.setText(it.titulo)
            editConteudo.setText(it.conteudo)
            selectedAttachment = it.arquivoAnexo
            textAnexo.text = it.arquivoAnexo ?: "Nenhum anexo"
        }

        btnSelecionarAnexo.setOnClickListener {
            selectAttachmentLauncher.launch("*/*")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle(if (report == null) "Novo Laudo" else "Editar Laudo")
            .setNegativeButton("Cancelar") { d, _ -> d.dismiss() }
            .setPositiveButton("Salvar") { _, _ ->
                val titulo = editTitulo.text.toString().trim()
                val conteudo = editConteudo.text.toString().trim()
                if (titulo.isNotEmpty() && conteudo.isNotEmpty()) {
                    val novoReport = PatientReport(
                        id = report?.id ?: 0,
                        patientId = patientId,
                        titulo = titulo,
                        conteudo = conteudo,
                        dataCriacao = report?.dataCriacao ?: Date(),
                        arquivoAnexo = selectedAttachment
                    )
                    onSave(novoReport)
                }
            }
            .create()
        return dialog
    }
} 