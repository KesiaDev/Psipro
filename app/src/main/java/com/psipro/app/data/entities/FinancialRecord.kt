package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "financial_records")
data class FinancialRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long? = null, // Opcional: pode estar vinculado a um paciente
    val description: String,
    val value: Double,
    val type: String, // "RECEITA" ou "DESPESA"
    val categoria: String = "", // Categoria da receita/despesa (ex: "Material", "Aluguel", "Equipamento")
    val date: Date = Date(),
    val observacao: String = "" // Observações adicionais
) 



