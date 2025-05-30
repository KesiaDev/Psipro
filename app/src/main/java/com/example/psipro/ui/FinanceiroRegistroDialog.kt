package com.example.psipro.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.psipro.R

class FinanceiroRegistroDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_financeiro_registro, null)
        return android.app.AlertDialog.Builder(requireContext())
            .setTitle("Registro Financeiro")
            .setView(view)
            .setPositiveButton("Salvar") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
    }
} 