package com.psipro.app.data.repository

import com.psipro.app.data.dao.HistoricoMedicoDao
import com.psipro.app.data.entities.HistoricoMedico
import javax.inject.Inject

class HistoricoMedicoRepository @Inject constructor(
    private val dao: HistoricoMedicoDao
) {
    suspend fun getByPatientId(patientId: Long): HistoricoMedico? = dao.getByPatientId(patientId)
    suspend fun insert(historico: HistoricoMedico): Long = dao.insert(historico)
    suspend fun update(historico: HistoricoMedico) = dao.update(historico)
} 



