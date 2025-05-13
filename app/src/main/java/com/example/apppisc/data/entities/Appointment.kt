package com.example.apppisc.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.Calendar

enum class AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY, CUSTOM
}

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("patientId"),
        Index(value = ["date", "startTime", "endTime"], unique = true)
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val patientId: Long,
    val patientName: String,
    val patientPhone: String,
    val date: Date,
    val startTime: String,
    val endTime: String,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int = 30,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    // Valor da sessão
    val sessionValue: Double = 0.0,
    // Recorrência
    val recurrenceType: RecurrenceType = RecurrenceType.NONE, // Tipo de recorrência
    val recurrenceInterval: Int? = null, // Ex: a cada X dias/semanas/meses
    val recurrenceEndDate: Date? = null, // Data de término da recorrência
    val recurrenceCount: Int? = null,    // Número de repetições
    val recurrenceSeriesId: Long? = null, // Para agrupar ocorrências
    // Confirmação de presença
    val isConfirmed: Boolean? = null, // true = confirmado, false = faltou, null = não respondido
    val confirmationDate: Date? = null, // data/hora da confirmação
    val absenceReason: String? = null, // motivo da ausência
    // Cor do agendamento
    val colorHex: String = "#FFF9C4" // Amarelo suave padrão
)

fun generateRecurrenceDates(
    startDate: Date,
    recurrenceType: RecurrenceType,
    interval: Int? = null,
    endDate: Date? = null,
    count: Int? = null
): List<Date> {
    if (recurrenceType == RecurrenceType.NONE) return listOf(startDate)
    val result = mutableListOf<Date>()
    val calendar = Calendar.getInstance().apply { time = startDate }
    val maxCount = count ?: 100 // Limite de segurança
    var occurrences = 0
    while (true) {
        val current = calendar.time
        if (endDate != null && current.after(endDate)) break
        if (occurrences >= maxCount) break
        result.add(current)
        occurrences++
        when (recurrenceType) {
            RecurrenceType.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, interval ?: 1)
            RecurrenceType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, interval ?: 1)
            RecurrenceType.BIWEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, (interval ?: 1) * 2)
            RecurrenceType.MONTHLY -> calendar.add(Calendar.MONTH, interval ?: 1)
            RecurrenceType.CUSTOM -> calendar.add(Calendar.DAY_OF_MONTH, interval ?: 1)
            else -> break
        }
    }
    return result
} 