package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.AnotacaoSessao
import kotlinx.coroutines.flow.Flow

@Dao
interface AnotacaoSessaoDao {
    @Query("SELECT * FROM anotacoes_sessao WHERE patientId = :patientId ORDER BY numeroSessao DESC")
    fun getByPatientId(patientId: Long): Flow<List<AnotacaoSessao>>

    @Query("SELECT * FROM anotacoes_sessao WHERE patientId = :patientId AND numeroSessao = :numeroSessao LIMIT 1")
    suspend fun getByPatientAndSession(patientId: Long, numeroSessao: Int): AnotacaoSessao?

    @Query("SELECT * FROM anotacoes_sessao WHERE id = :id")
    suspend fun getById(id: Long): AnotacaoSessao?

    @Query("SELECT MAX(numeroSessao) FROM anotacoes_sessao WHERE patientId = :patientId")
    suspend fun getMaxSessionNumber(patientId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(anotacao: AnotacaoSessao): Long

    @Update
    suspend fun update(anotacao: AnotacaoSessao)

    @Delete
    suspend fun delete(anotacao: AnotacaoSessao)
} 