package com.example.psipro.data.repository

import com.example.psipro.data.dao.ProntuarioDao
import com.example.psipro.data.entities.Prontuario
import javax.inject.Inject

class ProntuarioRepository @Inject constructor(private val prontuarioDao: ProntuarioDao) {
    suspend fun insert(prontuario: Prontuario) = prontuarioDao.insert(prontuario)
    suspend fun update(prontuario: Prontuario) = prontuarioDao.update(prontuario)
    suspend fun delete(prontuario: Prontuario) = prontuarioDao.delete(prontuario)
    suspend fun getProntuariosByPatient(patientId: Long) = prontuarioDao.getProntuariosByPatient(patientId)
    suspend fun getProntuarioById(id: Long) = prontuarioDao.getProntuarioById(id)
} 