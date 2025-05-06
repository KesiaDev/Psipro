package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.Appointment
import com.example.apppisc.data.entities.AppointmentStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date DESC, startTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY startTime ASC")
    fun getAppointmentsByDate(date: Date): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY date DESC, startTime DESC")
    fun getAppointmentsByPatient(patientId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("UPDATE appointments SET status = :status WHERE id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: Long, status: AppointmentStatus)

    @Query("""
        SELECT COUNT(*) > 0 FROM appointments 
        WHERE date = :date 
        AND id != :excludeId
        AND (
            (startTime <= :startTime AND endTime > :startTime)
            OR (startTime < :endTime AND endTime >= :endTime)
            OR (startTime >= :startTime AND endTime <= :endTime)
        )
    """)
    suspend fun hasConflictingAppointments(date: Date, startTime: String, endTime: String, excludeId: Long): Boolean

    @Query("""
        SELECT * FROM appointments 
        WHERE date >= date('now', 'localtime')
        AND status = 'SCHEDULED'
        ORDER BY date ASC, startTime ASC
    """)
    fun getUpcomingAppointments(): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE date = date('now', 'localtime')
        AND status = 'SCHEDULED'
        ORDER BY startTime ASC
    """)
    fun getTodayAppointments(): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC, startTime ASC
    """)
    fun getAppointmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE status = :status 
        ORDER BY date DESC, startTime ASC
    """)
    fun getAppointmentsByStatus(status: AppointmentStatus): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE (
            title LIKE '%' || :query || '%' 
            OR patientName LIKE '%' || :query || '%'
            OR patientPhone LIKE '%' || :query || '%'
        )
        ORDER BY date DESC, startTime ASC
    """)
    fun searchAppointments(query: String): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC, startTime ASC
    """)
    fun getAppointmentsBetweenDates(startDate: Date, endDate: Date): Flow<List<Appointment>>

    @Query("DELETE FROM appointments WHERE recurrenceSeriesId = :seriesId")
    suspend fun deleteAppointmentsBySeriesId(seriesId: Long)

    @Query("""
        UPDATE appointments SET 
            title = :title,
            description = :description,
            startTime = :startTime,
            endTime = :endTime,
            reminderEnabled = :reminderEnabled,
            reminderMinutes = :reminderMinutes
        WHERE recurrenceSeriesId = :seriesId AND date >= :fromDate
    """)
    suspend fun updateAppointmentsBySeriesId(
        seriesId: Long,
        fromDate: Date,
        title: String,
        description: String?,
        startTime: String,
        endTime: String,
        reminderEnabled: Boolean,
        reminderMinutes: Int
    )

    @Query("SELECT * FROM appointments WHERE recurrenceSeriesId = :seriesId ORDER BY date ASC, startTime ASC")
    fun getAppointmentsBySeriesId(seriesId: Long): Flow<List<Appointment>>

    @Query("""
        UPDATE appointments SET 
            isConfirmed = :isConfirmed,
            confirmationDate = :confirmationDate,
            absenceReason = :absenceReason
        WHERE id = :appointmentId
    """)
    suspend fun updateConfirmation(
        appointmentId: Long,
        isConfirmed: Boolean?,
        confirmationDate: Date?,
        absenceReason: String?
    )
} 