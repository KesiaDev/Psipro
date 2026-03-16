package com.psipro.app.data.repository

import com.psipro.app.data.dao.ObservacoesClinicasDao
import com.psipro.app.data.entities.ObservacoesClinicas
import javax.inject.Inject

class ObservacoesClinicasRepository @Inject constructor(
    private val dao: ObservacoesClinicasDao
) {
    suspend fun getByPatientId(patientId: Long): ObservacoesClinicas? = dao.getByPatientId(patientId)
    suspend fun insert(observacoes: ObservacoesClinicas): Long = dao.insert(observacoes)
    suspend fun update(observacoes: ObservacoesClinicas) = dao.update(observacoes)
} 



