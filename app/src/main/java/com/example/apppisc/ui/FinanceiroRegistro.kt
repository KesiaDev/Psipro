package com.example.apppisc.ui.fragments

data class FinanceiroRegistro(
    val id: Long,
    val descricao: String,
    val valor: Double,
    val data: String,
    val status: String // Ex: "Pago", "Pendente"
) 