package com.example.apppisc.data.entities

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "patient_messages",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class PatientMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val texto: String,
    val data: Date = Date()
) 