package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class AnamnesePreenchida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pacienteId: Long,
    val modeloId: Long,
    val respostas: String, // JSON: campoId -> resposta
    val assinaturaPath: String?,
    val data: Date,
    val versao: Int = 1
) 