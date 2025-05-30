package com.example.psipro.data.repository

import com.example.psipro.data.dao.PatientReportDao
import com.example.psipro.data.entities.PatientReport
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