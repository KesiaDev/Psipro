package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.CobrancaAgendamento
import kotlinx.coroutines.flow.Flow

@Dao
interface CobrancaAgendamentoDao {
    
    @Query("SELECT * FROM cobrancas_agendamento ORDER BY createdAt DESC")
    fun getAllCobrancas(): Flow<List<CobrancaAgendamento>>
    
    @Query("SELECT * FROM cobrancas_agendamento WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getCobrancasByPatient(patientId: Long): Flow<List<CobrancaAgendamento>>
    
    @Query("SELECT * FROM cobrancas_agendamento WHERE appointmentId = :appointmentId")
    fun getCobrancaByAppointment(appointmentId: Long): Flow<CobrancaAgendamento?>
    
    @Query("SELECT * FROM cobrancas_agendamento WHERE status = :status ORDER BY dataVencimento ASC")
    fun getCobrancasByStatus(status: String): Flow<List<CobrancaAgendamento>>
    
    @Query("SELECT * FROM cobrancas_agendamento WHERE dataVencimento <= :date AND status = 'A_RECEBER'")
    fun getCobrancasVencidas(date: java.util.Date): Flow<List<CobrancaAgendamento>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCobranca(cobranca: CobrancaAgendamento): Long
    
    @Update
    suspend fun updateCobranca(cobranca: CobrancaAgendamento)
    
    @Delete
    suspend fun deleteCobranca(cobranca: CobrancaAgendamento)
    
    @Query("DELETE FROM cobrancas_agendamento WHERE id = :id")
    suspend fun deleteCobrancaById(id: Long)
    
    @Query("SELECT COUNT(*) FROM cobrancas_agendamento WHERE status = 'A_RECEBER'")
    fun getCountCobrancasPendentes(): Flow<Int>
    
    @Query("SELECT SUM(valor) FROM cobrancas_agendamento WHERE status = 'PAGO'")
    fun getTotalRecebido(): Flow<Double?>
    
    @Query("SELECT SUM(valor) FROM cobrancas_agendamento WHERE status = 'A_RECEBER'")
    fun getTotalAReceber(): Flow<Double?>
    
    @Query("SELECT * FROM cobrancas_agendamento WHERE id = :id")
    suspend fun getCobrancaById(id: Long): CobrancaAgendamento?
} 



