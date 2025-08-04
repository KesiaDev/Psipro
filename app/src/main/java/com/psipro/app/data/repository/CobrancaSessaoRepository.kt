package com.psipro.app.data.repository

import com.psipro.app.data.dao.CobrancaSessaoDao
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import com.psipro.app.data.entities.PagamentoComPaciente

class CobrancaSessaoRepository @Inject constructor(
    private val dao: CobrancaSessaoDao
) {
    fun getByPatientId(patientId: Long): Flow<List<CobrancaSessao>> = dao.getByPatientId(patientId)
    
    fun getByStatus(status: StatusPagamento): Flow<List<CobrancaSessao>> = dao.getByStatus(status)
    
    fun getByPeriodo(dataInicio: Date, dataFim: Date): Flow<List<CobrancaSessao>> = dao.getByPeriodo(dataInicio, dataFim)
    
    fun getVencidas(dataAtual: Date): Flow<List<CobrancaSessao>> = dao.getVencidas(dataAtual)
    
    suspend fun getByAnotacaoSessao(anotacaoSessaoId: Long): CobrancaSessao? = dao.getByAnotacaoSessao(anotacaoSessaoId)
    
    suspend fun getCountByStatus(status: StatusPagamento): Int = dao.getCountByStatus(status)
    
    suspend fun getTotalRecebidoGeral(): Double = dao.getTotalRecebidoGeral() ?: 0.0
    
    suspend fun getTotalAReceber(): Double = dao.getTotalAReceber() ?: 0.0
    
    suspend fun getCountPendentes(): Int = dao.getCountPendentes()
    
    suspend fun insert(cobranca: CobrancaSessao): Long = dao.insert(cobranca)
    
    suspend fun update(cobranca: CobrancaSessao) = dao.update(cobranca)
    
    suspend fun delete(cobranca: CobrancaSessao) = dao.delete(cobranca)
    
    suspend fun marcarComoPago(cobrancaId: Long, dataPagamento: Date = Date()) = 
        dao.marcarComoPago(cobrancaId, StatusPagamento.PAGO, dataPagamento)
    
    suspend fun desmarcarComoPago(cobrancaId: Long) = 
        dao.desmarcarComoPago(cobrancaId, StatusPagamento.A_RECEBER)
    
    suspend fun editarCobranca(cobrancaId: Long, valor: Double, observacoes: String) = 
        dao.editarCobranca(cobrancaId, valor, observacoes)
    
    suspend fun marcarComoVencido(cobrancaId: Long) = 
        dao.marcarComoPago(cobrancaId, StatusPagamento.VENCIDO, null)
    
    suspend fun deleteByAnotacaoSessaoId(anotacaoSessaoId: Long) = 
        dao.deleteByAnotacaoSessaoId(anotacaoSessaoId)
} 



