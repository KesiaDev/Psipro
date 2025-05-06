package com.example.apppisc.data.entities

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "patient_reports",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patientId")
    ]
)
data class PatientReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val titulo: String,
    val conteudo: String,
    val dataCriacao: Date = Date(),
    val arquivoAnexo: String? = null // caminho ou URI do arquivo
) 