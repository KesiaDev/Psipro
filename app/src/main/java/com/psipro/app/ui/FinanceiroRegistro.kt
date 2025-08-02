package com.psipro.app.ui.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.psipro.app.R
import com.psipro.app.data.entities.FinancialRecord

data class FinanceiroRegistro(
    val id: Long,
    val descricao: String,
    val valor: Double,
    val data: String,
    val status: String // Ex: "Pago", "Pendente"
) 



