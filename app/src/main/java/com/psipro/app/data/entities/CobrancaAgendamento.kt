package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "cobrancas_agendamento",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Appointment::class,
            parentColumns = ["id"],
            childColumns = ["appointmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId"), Index("appointmentId")]
)
data class CobrancaAgendamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val appointmentId: Long,
    val valor: Double,
    val dataAgendamento: Date,
    val dataEvento: Date, // Data do evento que gerou a cobrança (falta, cancelamento, etc.)
    val dataVencimento: Date,
    val dataPagamento: Date? = null,
    val status: StatusPagamento = StatusPagamento.A_RECEBER, // A_RECEBER = PENDENTE (mantido por compatibilidade)
    val motivo: String, // "AGENDAMENTO", "CANCELAMENTO", "FALTA"
    val observacoes: String = "",
    val pixCopiaCola: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 



