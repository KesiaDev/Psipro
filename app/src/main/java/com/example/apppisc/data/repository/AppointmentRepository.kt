package com.example.apppisc.data.repository

import com.example.apppisc.data.dao.AppointmentDao
import com.example.apppisc.data.entities.Appointment
import com.example.apppisc.data.entities.AppointmentStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    fun getAllAppointments(): Flow<List<Appointment>> = appointmentDao.getAllAppointments()

    fun getAppointmentsByPatient(patientId: Long): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsByPatient(patientId)

    fun getAppointmentsBetweenDates(startDate: Date, endDate: Date): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsBetweenDates(startDate, endDate)

    suspend fun insertAppointment(appointment: Appointment): Long = 
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: Appointment) = 
        appointmentDao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: Appointment) = 
        appointmentDao.deleteAppointment(appointment)

    suspend fun updateAppointmentStatus(appointmentId: Long, status: AppointmentStatus) =
        appointmentDao.updateAppointmentStatus(appointmentId, status)

    suspend fun hasConflictingAppointments(date: Date, startTime: String, endTime: String, excludeId: Long = 0L): Boolean =
        appointmentDao.hasConflictingAppointments(date, startTime, endTime, excludeId)

    fun getUpcomingAppointments(): Flow<List<Appointment>> = 
        appointmentDao.getUpcomingAppointments()

    fun getTodayAppointments(): Flow<List<Appointment>> = 
        appointmentDao.getTodayAppointments()

    fun getAppointmentsByDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>> = 
        appointmentDao.getAppointmentsByDateRange(startDate, endDate)

    fun getAppointmentsByStatus(status: AppointmentStatus): Flow<List<Appointment>> = 
        appointmentDao.getAppointmentsByStatus(status)

    fun searchAppointments(query: String): Flow<List<Appointment>> = 
        appointmentDao.searchAppointments(query)

    suspend fun deleteAppointmentsBySeriesId(seriesId: Long) =
        appointmentDao.deleteAppointmentsBySeriesId(seriesId)

    suspend fun updateAppointmentsBySeriesId(
        seriesId: Long,
        fromDate: Date,
        title: String,
        description: String?,
        startTime: String,
        endTime: String,
        reminderEnabled: Boolean,
        reminderMinutes: Int
    ) = appointmentDao.updateAppointmentsBySeriesId(
        seriesId, fromDate, title, description, startTime, endTime, reminderEnabled, reminderMinutes
    )

    fun getAppointmentsBySeriesId(seriesId: Long): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsBySeriesId(seriesId)

    suspend fun updateConfirmation(
        appointmentId: Long,
        isConfirmed: Boolean?,
        confirmationDate: Date?,
        absenceReason: String?
    ) = appointmentDao.updateConfirmation(appointmentId, isConfirmed, confirmationDate, absenceReason)

    suspend fun getAppointmentById(id: Long): Appointment? = appointmentDao.getAppointmentById(id)
} 