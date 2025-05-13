package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.Prontuario

@Dao
interface ProntuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prontuario: Prontuario): Long

    @Update
    suspend fun update(prontuario: Prontuario)

    @Delete
    suspend fun delete(prontuario: Prontuario)

    @Query("SELECT * FROM prontuarios WHERE patientId = :patientId ORDER BY data DESC")
    suspend fun getProntuariosByPatient(patientId: Long): List<Prontuario>

    @Query("SELECT * FROM prontuarios WHERE id = :id")
    suspend fun getProntuarioById(id: Long): Prontuario?
} 