package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.StatusPagamento
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CobrancaSessaoDao {
    @Query("SELECT * FROM cobrancas_sessao WHERE patientId = :patientId ORDER BY dataSessao DESC")
    fun getByPatientId(patientId: Long): Flow<List<CobrancaSessao>>

    @Query("SELECT * FROM cobrancas_sessao WHERE status = :status ORDER BY dataVencimento ASC")
    fun getByStatus(status: StatusPagamento): Flow<List<CobrancaSessao>>

    @Query("SELECT * FROM cobrancas_sessao WHERE dataSessao BETWEEN :dataInicio AND :dataFim ORDER BY dataSessao DESC")
    fun getByPeriodo(dataInicio: Date, dataFim: Date): Flow<List<CobrancaSessao>>

    @Query("SELECT * FROM cobrancas_sessao WHERE dataVencimento < :dataAtual AND status = 'A_RECEBER' ORDER BY dataVencimento ASC")
    fun getVencidas(dataAtual: Date): Flow<List<CobrancaSessao>>

    @Query("SELECT * FROM cobrancas_sessao WHERE anotacaoSessaoId = :anotacaoSessaoId LIMIT 1")
    suspend fun getByAnotacaoSessao(anotacaoSessaoId: Long?): CobrancaSessao?
    
    @Query("SELECT * FROM cobrancas_sessao WHERE appointmentId = :appointmentId LIMIT 1")
    suspend fun getByAppointmentId(appointmentId: Long?): CobrancaSessao?

    @Query("SELECT COUNT(*) FROM cobrancas_sessao WHERE status = :status")
    suspend fun getCountByStatus(status: StatusPagamento): Int

    @Query("SELECT SUM(valor) FROM cobrancas_sessao WHERE status = 'PAGO'")
    suspend fun getTotalRecebidoGeral(): Double?
    
    @Query("SELECT SUM(valor) FROM cobrancas_sessao WHERE status = 'PAGO' AND dataPagamento >= :startOfDay AND dataPagamento <= :endOfDay")
    suspend fun getTotalRecebidoHoje(startOfDay: Date, endOfDay: Date): Double?

    @Query("SELECT SUM(valor) FROM cobrancas_sessao WHERE status IN ('A_RECEBER', 'VENCIDO')")
    suspend fun getTotalAReceber(): Double?

    @Query("SELECT COUNT(*) FROM cobrancas_sessao WHERE status IN ('A_RECEBER', 'VENCIDO')")
    suspend fun getCountPendentes(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cobranca: CobrancaSessao): Long

    @Update
    suspend fun update(cobranca: CobrancaSessao)

    @Delete
    suspend fun delete(cobranca: CobrancaSessao)

    @Query("UPDATE cobrancas_sessao SET status = :status, dataPagamento = :dataPagamento, dirty = 1 WHERE id = :cobrancaId")
    suspend fun marcarComoPago(cobrancaId: Long, status: StatusPagamento, dataPagamento: Date?)

    @Query("UPDATE cobrancas_sessao SET status = :status, dataPagamento = null, dirty = 1 WHERE id = :cobrancaId")
    suspend fun desmarcarComoPago(cobrancaId: Long, status: StatusPagamento)

    @Query("UPDATE cobrancas_sessao SET valor = :valor, observacoes = :observacoes, dirty = 1 WHERE id = :cobrancaId")
    suspend fun editarCobranca(cobrancaId: Long, valor: Double, observacoes: String)

    @Query("DELETE FROM cobrancas_sessao WHERE anotacaoSessaoId = :anotacaoSessaoId")
    suspend fun deleteByAnotacaoSessaoId(anotacaoSessaoId: Long)

    @Query("SELECT * FROM cobrancas_sessao WHERE dirty = 1")
    suspend fun getDirtyPayments(): List<CobrancaSessao>

    @Query("SELECT * FROM cobrancas_sessao WHERE backendId = :backendId LIMIT 1")
    suspend fun getByBackendId(backendId: String): CobrancaSessao?

    @Query("UPDATE cobrancas_sessao SET dirty = 0, lastSyncedAt = :syncedAt WHERE backendId IN (:backendIds)")
    suspend fun markSyncedByBackendId(backendIds: List<String>, syncedAt: Date)
} 



