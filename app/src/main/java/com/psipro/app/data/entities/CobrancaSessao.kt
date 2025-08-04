package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

enum class StatusPagamento {
    PAGO,
    A_RECEBER,
    VENCIDO,
    CANCELADO
}

@Entity(
    tableName = "cobrancas_sessao",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AnotacaoSessao::class,
            parentColumns = ["id"],
            childColumns = ["anotacaoSessaoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CobrancaSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val anotacaoSessaoId: Long,
    val numeroSessao: Int,
    val valor: Double,
    val dataSessao: Date,
    val dataVencimento: Date,
    val dataPagamento: Date? = null,
    val status: StatusPagamento = StatusPagamento.A_RECEBER,
    val observacoes: String = "",
    val pixCopiaCola: String = "",
    val tipoSessaoId: Long? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

// Classe para relatórios com nome do paciente
data class PagamentoComPaciente(
    val id: Long,
    val patientId: Long,
    val valor: Double,
    val dataPagamento: Date?,
    val status: StatusPagamento,
    val patientName: String
) 



