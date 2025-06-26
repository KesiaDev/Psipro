package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AnamneseCampo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeloId: Long,
    val tipo: String, // TEXTO_CURTO, TEXTO_LONGO, DATA, SELECAO_UNICA, MULTIPLA_ESCOLHA, TITULO
    val label: String,
    val opcoes: String? = null, // JSON para opções de seleção
    val obrigatorio: Boolean = false // NOVO: indica se o campo é obrigatório
) 