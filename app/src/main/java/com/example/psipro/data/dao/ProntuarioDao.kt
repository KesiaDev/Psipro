package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.Prontuario
import kotlinx.coroutines.flow.Flow

@Dao
interface ProntuarioDao {
    @Query("SELECT * FROM prontuarios WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getProntuariosByPatient(patientId: Long): Flow<List<Prontuario>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProntuario(prontuario: Prontuario): Long

    @Update
    suspend fun updateProntuario(prontuario: Prontuario)

    @Delete
    suspend fun deleteProntuario(prontuario: Prontuario)

    @Query("SELECT * FROM prontuarios WHERE id = :id")
    suspend fun getProntuarioById(id: Long): Prontuario?
} 