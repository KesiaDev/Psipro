package com.example.psipro.data.repository

import com.example.psipro.data.dao.HistoricoMedicoDao
import com.example.psipro.data.entities.HistoricoMedico
import javax.inject.Inject

class HistoricoMedicoRepository @Inject constructor(
    private val dao: HistoricoMedicoDao
) {
    suspend fun getByPatientId(patientId: Long): HistoricoMedico? = dao.getByPatientId(patientId)
    suspend fun insert(historico: HistoricoMedico): Long = dao.insert(historico)
    suspend fun update(historico: HistoricoMedico) = dao.update(historico)
} 