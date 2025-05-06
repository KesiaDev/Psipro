package com.example.apppisc.data.repositories

import com.example.apppisc.data.dao.PatientDao
import com.example.apppisc.data.entities.Patient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao
) {
    companion object {
        const val FREE_VERSION_PATIENT_LIMIT = 50
        const val NEAR_LIMIT_THRESHOLD = 5
    }

    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()

    fun searchPatients(query: String): Flow<List<Patient>> = patientDao.searchPatients(query)

    suspend fun getPatientById(id: Long): Patient? = patientDao.getPatientById(id)

    suspend fun getPatientByCpf(cpf: String): Patient? = patientDao.getPatientByCpf(cpf)

    suspend fun insertPatient(patient: Patient) = patientDao.insertPatient(patient)

    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)

    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)

    suspend fun getPatientCount(): Int = patientDao.getPatientCount()

    suspend fun canAddMorePatients(): Boolean {
        val currentCount = getPatientCount()
        return currentCount < FREE_VERSION_PATIENT_LIMIT
    }

    suspend fun isNearLimit(): Boolean {
        val currentCount = getPatientCount()
        return currentCount >= (FREE_VERSION_PATIENT_LIMIT - NEAR_LIMIT_THRESHOLD)
    }

    suspend fun addPatient(patient: Patient) {
        val currentCount = getPatientCount()
        if (currentCount >= 100) {
            throw Exception("Limite máximo de pacientes atingido!")
        }
        patientDao.insertPatient(patient)
    }
} 