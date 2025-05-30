package com.example.psipro.data.repository

import com.example.psipro.data.dao.PatientDao
import com.example.psipro.data.entities.Patient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao
) {
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()

    suspend fun getPatientById(id: Long): Patient? {
        return patientDao.getPatientById(id)
    }

    suspend fun getPatientByCpf(cpf: String): Patient? {
        return patientDao.getPatientByCpf(cpf)
    }

    fun searchPatients(query: String): Flow<List<Patient>> {
        return patientDao.searchPatients(query)
    }

    suspend fun insertPatient(patient: Patient): Long {
        return patientDao.insertPatient(patient)
    }

    suspend fun updatePatient(patient: Patient) {
        patientDao.updatePatient(patient)
    }

    suspend fun deletePatient(patient: Patient) {
        patientDao.deletePatient(patient)
    }

    suspend fun getPatientCount(): Int {
        return patientDao.getPatientCount()
    }

    suspend fun canAddMorePatients(): Boolean {
        // Exemplo: limite de 100 pacientes
        return getPatientCount() < 100
    }

    suspend fun isNearLimit(): Boolean {
        // Exemplo: alerta se >= 90 pacientes
        return getPatientCount() >= 90
    }
} 