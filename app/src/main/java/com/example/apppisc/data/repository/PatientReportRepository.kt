package com.example.apppisc.data.repository

import com.example.apppisc.data.dao.PatientReportDao
import com.example.apppisc.data.entities.PatientReport
import javax.inject.Inject

class PatientReportRepository @Inject constructor(
    private val dao: PatientReportDao
) {
    suspend fun insert(report: PatientReport) = dao.insert(report)
    suspend fun update(report: PatientReport) = dao.update(report)
    suspend fun delete(report: PatientReport) = dao.delete(report)
    suspend fun getReportsByPatient(patientId: Long) = dao.getReportsByPatient(patientId)
    suspend fun getReportById(id: Long) = dao.getReportById(id)
} 