package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
        // ForeignKey para Appointment (appointmentId pode ser nullable, mas Room permite)
        ForeignKey(
            entity = Appointment::class,
            parentColumns = ["id"],
            childColumns = ["appointmentId"],
            onDelete = ForeignKey.SET_NULL
        )
        // Nota: anotacaoSessaoId é nullable, então não podemos usar ForeignKey aqui.
        // A integridade referencial será mantida no código da aplicação.
    ],
    indices = [Index("patientId"), Index("anotacaoSessaoId"), Index("appointmentId")]
)
data class CobrancaSessao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val anotacaoSessaoId: Long? = null, // Opcional: pode ser criada quando agendamento é REALIZADO sem anotação ainda
    val appointmentId: Long? = null, // Opcional: vinculado ao agendamento se a sessão veio de um agendamento
    val numeroSessao: Int,
    val valor: Double,
    val dataSessao: Date,
    val dataVencimento: Date,
    val dataPagamento: Date? = null,
    val status: StatusPagamento = StatusPagamento.A_RECEBER, // A_RECEBER = PENDENTE (mantido por compatibilidade)
    val metodoPagamento: String = "", // Ex: "PIX", "Dinheiro", "Cartão", "Transferência"
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



