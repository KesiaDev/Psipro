package com.psipro.app.data.repositories

import com.psipro.app.data.dao.PatientMessageDao
import com.psipro.app.data.entities.PatientMessage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientMessageRepository @Inject constructor(
    private val patientMessageDao: PatientMessageDao
) {
    fun getMessagesForPatient(patientId: Long): Flow<List<PatientMessage>> =
        patientMessageDao.getMessagesByPatient(patientId)

    suspend fun insertMessage(message: PatientMessage): Long =
        patientMessageDao.insertMessage(message)

    suspend fun deleteMessage(message: PatientMessage) =
        patientMessageDao.deleteMessage(message)

    suspend fun deleteAllMessagesForPatient(patientId: Long) =
        patientMessageDao.deleteAllMessagesForPatient(patientId)
} 



