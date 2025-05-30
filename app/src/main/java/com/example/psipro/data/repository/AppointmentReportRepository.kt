package com.example.psipro.data.repository

import com.example.psipro.data.dao.AppointmentReportDao
import com.example.psipro.data.entities.AppointmentReport
import javax.inject.Inject

class AppointmentReportRepository @Inject constructor(
    private val appointmentReportDao: AppointmentReportDao
) {
    suspend fun getAllAppointmentReports(): List<AppointmentReport> =
        appointmentReportDao.getAllAppointmentReports()
    suspend fun insertAppointmentReport(report: AppointmentReport) =
        appointmentReportDao.insertAppointmentReport(report)
    suspend fun deleteAppointmentReport(report: AppointmentReport) =
        appointmentReportDao.deleteAppointmentReport(report)
} 