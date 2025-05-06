package com.example.apppisc.data.repository

import com.example.apppisc.data.dao.PatientDao
import com.example.apppisc.data.entities.Patient
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
} 