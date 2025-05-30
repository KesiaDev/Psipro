package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import com.example.psipro.data.converters.StringListConverter

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
@TypeConverters(StringListConverter::class)
data class Prontuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val perguntas: List<String>, // Exemplo: ["Queixa Inicial?", "Evolução", ...]
    val respostas: List<String>, // Exemplo: ["Dor de cabeça", "Melhorou", ...]
    val data: Long = System.currentTimeMillis()
) 