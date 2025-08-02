package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "historico_medico",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HistoricoMedico(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val condicoes: String = "",
    val medicamentos: String = "",
    val internacoes: String = "",
    val queixasFisicas: String = "",
    val motivoTerapia: String = "",
    val tempoSintomas: String = "",
    val jaFezTerapia: String = "",
    val sono: String = "",
    val alimentacao: String = "",
    val substancias: String = "",
    val vidaSocial: String = ""
) 



