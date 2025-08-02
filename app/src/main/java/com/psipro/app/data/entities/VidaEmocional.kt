package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vida_emocional",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class VidaEmocional(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val ansiedade: String = "",
    val depressao: String = "",
    val trauma: String = "",
    val luto: String = ""
) 



