package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "autoavaliacoes")
data class Autoavaliacao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Data da avaliação
    val dataAvaliacao: Date,
    
    // Escalas de 1-10
    val bemEstarEmocional: Int, // 1-10
    val satisfacaoProfissional: Int, // 1-10
    val equilibrioVidaTrabalho: Int, // 1-10
    val energiaVital: Int, // 1-10
    val qualidadeSono: Int, // 1-10
    val nivelEstresse: Int, // 1-10 (invertido: 10 = baixo estresse)
    
    // Perguntas qualitativas
    val principaisDesafios: String,
    val conquistasMes: String,
    val objetivosProximoMes: String,
    val gratidao: String, // 3 coisas pelas quais é grato
    
    // Análise automática
    val scoreGeral: Float, // Média das escalas
    val categoriaGeral: String, // "Excelente", "Bom", "Regular", "Precisa Atenção"
    
    // Notas adicionais
    val observacoes: String? = null,
    
    // Timestamps
    val dataCriacao: Date = Date(),
    val dataAtualizacao: Date = Date()
) 



