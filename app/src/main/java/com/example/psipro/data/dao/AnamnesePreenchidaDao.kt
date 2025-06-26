package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.AnamnesePreenchida

@Dao
interface AnamnesePreenchidaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preenchida: AnamnesePreenchida): Long

    @Update
    suspend fun update(preenchida: AnamnesePreenchida)

    @Delete
    suspend fun delete(preenchida: AnamnesePreenchida)

    @Query("SELECT * FROM AnamnesePreenchida WHERE pacienteId = :pacienteId")
    suspend fun getByPacienteId(pacienteId: Long): List<AnamnesePreenchida>

    @Query("SELECT * FROM AnamnesePreenchida WHERE id = :id")
    suspend fun getById(id: Long): AnamnesePreenchida?
} 