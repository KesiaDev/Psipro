package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.HistoricoFamiliar

@Dao
interface HistoricoFamiliarDao {
    @Query("SELECT * FROM historico_familiar WHERE patientId = :patientId LIMIT 1")
    suspend fun getByPatientId(patientId: Long): HistoricoFamiliar?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historico: HistoricoFamiliar): Long

    @Update
    suspend fun update(historico: HistoricoFamiliar)
} 