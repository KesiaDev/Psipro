package com.example.psipro.data.repository

import com.example.psipro.data.dao.HistoricoFamiliarDao
import com.example.psipro.data.entities.HistoricoFamiliar
import javax.inject.Inject

class HistoricoFamiliarRepository @Inject constructor(
    private val dao: HistoricoFamiliarDao
) {
    suspend fun getByPatientId(patientId: Long): HistoricoFamiliar? = dao.getByPatientId(patientId)
    suspend fun insert(historico: HistoricoFamiliar): Long = dao.insert(historico)
    suspend fun update(historico: HistoricoFamiliar) = dao.update(historico)
} 