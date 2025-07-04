package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "anotacoes_sessao",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AnotacaoSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val numeroSessao: Int,
    val dataHora: Date = Date(),
    val assuntos: String = "",
    val estadoEmocional: String = "",
    val intervencoes: String = "",
    val tarefas: String = "",
    val evolucao: String = "",
    val observacoes: String = "",
    val anexos: String = "", // JSON array de caminhos de anexos
    val metaTerapeutica: String = "", // Meta terapêutica da sessão
    val proximoAgendamento: String = "", // Próximo agendamento sugerido
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val tipoSessaoId: Long? = null
) 