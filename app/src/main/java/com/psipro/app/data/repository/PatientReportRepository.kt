package com.psipro.app.data.repository

import com.psipro.app.data.dao.PatientReportDao
import com.psipro.app.data.entities.PatientReport
import javax.inject.Inject

class PatientReportRepository @Inject constructor(
    private val dao: PatientReportDao
) {
    suspend fun insert(report: PatientReport) = dao.insertReport(report)
    suspend fun update(report: PatientReport) = dao.update(report)
    suspend fun delete(report: PatientReport) = dao.deleteReport(report)
    suspend fun getReportsByPatient(patientId: Long) = dao.getReportsByPatient(patientId)
    suspend fun getReportById(id: Long) = dao.getReportById(id)
} 



