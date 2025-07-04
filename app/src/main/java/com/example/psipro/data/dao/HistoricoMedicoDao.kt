package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.HistoricoMedico

@Dao
interface HistoricoMedicoDao {
    @Query("SELECT * FROM historico_medico WHERE patientId = :patientId LIMIT 1")
    suspend fun getByPatientId(patientId: Long): HistoricoMedico?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historico: HistoricoMedico): Long

    @Update
    suspend fun update(historico: HistoricoMedico)
} 