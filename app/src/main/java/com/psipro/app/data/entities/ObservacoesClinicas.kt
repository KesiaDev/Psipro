package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "observacoes_clinicas",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class ObservacoesClinicas(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val observacoes: String = ""
) 



