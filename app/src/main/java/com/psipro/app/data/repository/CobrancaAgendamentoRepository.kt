package com.psipro.app.data.repository

import com.psipro.app.data.dao.CobrancaAgendamentoDao
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.entities.StatusPagamento
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class CobrancaAgendamentoRepository @Inject constructor(
    private val cobrancaAgendamentoDao: CobrancaAgendamentoDao
) {
    
    fun getAllCobrancas(): Flow<List<CobrancaAgendamento>> {
        return cobrancaAgendamentoDao.getAllCobrancas()
    }
    
    fun getCobrancasByPatient(patientId: Long): Flow<List<CobrancaAgendamento>> {
        return cobrancaAgendamentoDao.getCobrancasByPatient(patientId)
    }
    
    fun getCobrancaByAppointment(appointmentId: Long): Flow<CobrancaAgendamento?> {
        return cobrancaAgendamentoDao.getCobrancaByAppointment(appointmentId)
    }
    
    fun getCobrancasByStatus(status: String): Flow<List<CobrancaAgendamento>> {
        return cobrancaAgendamentoDao.getCobrancasByStatus(status)
    }
    
    fun getCobrancasVencidas(date: Date): Flow<List<CobrancaAgendamento>> {
        return cobrancaAgendamentoDao.getCobrancasVencidas(date)
    }
    
    suspend fun insertCobranca(cobranca: CobrancaAgendamento): Long {
        return cobrancaAgendamentoDao.insertCobranca(cobranca)
    }
    
    suspend fun updateCobranca(cobranca: CobrancaAgendamento) {
        cobrancaAgendamentoDao.updateCobranca(cobranca)
    }
    
    suspend fun deleteCobranca(cobranca: CobrancaAgendamento) {
        cobrancaAgendamentoDao.deleteCobranca(cobranca)
    }
    
    suspend fun deleteCobrancaById(id: Long) {
        cobrancaAgendamentoDao.deleteCobrancaById(id)
    }
    
    fun getCountCobrancasPendentes(): Flow<Int> {
        return cobrancaAgendamentoDao.getCountCobrancasPendentes()
    }
    
    fun getTotalRecebido(): Flow<Double?> {
        return cobrancaAgendamentoDao.getTotalRecebido()
    }
    
    fun getTotalAReceber(): Flow<Double?> {
        return cobrancaAgendamentoDao.getTotalAReceber()
    }
    
    suspend fun marcarComoPago(cobrancaId: Long) {
        val cobranca = cobrancaAgendamentoDao.getCobrancaById(cobrancaId)
        if (cobranca != null) {
            val cobrancaAtualizada = cobranca.copy(
                status = StatusPagamento.PAGO,
                dataPagamento = Date()
            )
            cobrancaAgendamentoDao.updateCobranca(cobrancaAtualizada)
        }
    }
} 



