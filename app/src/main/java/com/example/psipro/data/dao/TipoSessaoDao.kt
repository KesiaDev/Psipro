package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.TipoSessao
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoSessaoDao {
    @Query("SELECT * FROM tipos_sessao ORDER BY nome ASC")
    fun getAll(): Flow<List<TipoSessao>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tipo: TipoSessao): Long

    @Update
    suspend fun update(tipo: TipoSessao)

    @Delete
    suspend fun delete(tipo: TipoSessao)

    @Query("SELECT COUNT(*) FROM tipos_sessao")
    suspend fun countTiposSessao(): Int
} 