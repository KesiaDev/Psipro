package com.example.psipro.ui.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.psipro.R
import com.example.psipro.data.entities.FinancialRecord

data class FinanceiroRegistro(
    val id: Long,
    val descricao: String,
    val valor: Double,
    val data: String,
    val status: String // Ex: "Pago", "Pendente"
) 