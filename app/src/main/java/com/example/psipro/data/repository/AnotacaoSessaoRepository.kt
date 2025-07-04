package com.example.psipro.data.repository

import com.example.psipro.data.dao.AnotacaoSessaoDao
import com.example.psipro.data.entities.AnotacaoSessao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnotacaoSessaoRepository @Inject constructor(
    private val dao: AnotacaoSessaoDao
) {
    fun getByPatientId(patientId: Long): Flow<List<AnotacaoSessao>> = dao.getByPatientId(patientId)
    suspend fun getByPatientAndSession(patientId: Long, numeroSessao: Int): AnotacaoSessao? = dao.getByPatientAndSession(patientId, numeroSessao)
    suspend fun getById(id: Long): AnotacaoSessao? = dao.getById(id)
    suspend fun getMaxSessionNumber(patientId: Long): Int? = dao.getMaxSessionNumber(patientId)
    suspend fun insert(anotacao: AnotacaoSessao): Long = dao.insert(anotacao)
    suspend fun update(anotacao: AnotacaoSessao) = dao.update(anotacao)
    suspend fun delete(anotacao: AnotacaoSessao) = dao.delete(anotacao)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
} 