package com.psipro.app.data.repository

import com.psipro.app.data.dao.ProntuarioDao
import com.psipro.app.data.entities.Prontuario
import javax.inject.Inject

class ProntuarioRepository @Inject constructor(private val prontuarioDao: ProntuarioDao) {
    suspend fun insert(prontuario: Prontuario) = prontuarioDao.insertProntuario(prontuario)
    suspend fun update(prontuario: Prontuario) = prontuarioDao.updateProntuario(prontuario)
    suspend fun delete(prontuario: Prontuario) = prontuarioDao.deleteProntuario(prontuario)
    suspend fun getProntuariosByPatient(patientId: Long) = prontuarioDao.getProntuariosByPatient(patientId)
    suspend fun getProntuarioById(id: Long) = prontuarioDao.getProntuarioById(id)
} 



