package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.ObservacoesClinicas

@Dao
interface ObservacoesClinicasDao {
    @Query("SELECT * FROM observacoes_clinicas WHERE patientId = :patientId LIMIT 1")
    suspend fun getByPatientId(patientId: Long): ObservacoesClinicas?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observacoes: ObservacoesClinicas): Long

    @Update
    suspend fun update(observacoes: ObservacoesClinicas)
} 