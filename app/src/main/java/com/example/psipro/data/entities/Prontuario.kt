package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "prontuarios",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Prontuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val content: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 