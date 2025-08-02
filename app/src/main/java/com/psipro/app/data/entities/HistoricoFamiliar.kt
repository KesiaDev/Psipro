package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "historico_familiar",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HistoricoFamiliar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val relacaoPais: String = "",
    val ambienteFamiliar: String = "",
    val transtornos: String = "",
    val vinculosAfetivos: String = ""
) 



