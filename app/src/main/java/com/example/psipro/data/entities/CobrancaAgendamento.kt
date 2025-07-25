package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
)
data class CobrancaAgendamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val appointmentId: Long,
    val valor: Double,
    val dataAgendamento: Date,
    val dataVencimento: Date,
    val dataPagamento: Date? = null,
    val status: StatusPagamento = StatusPagamento.A_RECEBER,
    val motivo: String, // "AGENDAMENTO", "CANCELAMENTO", "FALTA"
    val observacoes: String = "",
    val pixCopiaCola: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 