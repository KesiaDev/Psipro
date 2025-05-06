package com.example.apppisc.data.repositories

import com.example.apppisc.data.dao.PatientMessageDao
import com.example.apppisc.data.entities.PatientMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientMessageRepository @Inject constructor(
    private val patientMessageDao: PatientMessageDao
) {
    fun getMessagesForPatient(patientId: Long): Flow<List<PatientMessage>> =
        patientMessageDao.getMessagesForPatient(patientId)

    suspend fun insertMessage(message: PatientMessage): Long =
        patientMessageDao.insertMessage(message)

    suspend fun deleteMessage(message: PatientMessage) =
        patientMessageDao.deleteMessage(message)

    suspend fun deleteAllMessagesForPatient(patientId: Long) =
        patientMessageDao.deleteAllMessagesForPatient(patientId)
} 