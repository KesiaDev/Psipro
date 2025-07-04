package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_sessao")
data class TipoSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val valorPadrao: Double
) 