package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.VidaEmocional

@Dao
interface VidaEmocionalDao {
    @Query("SELECT * FROM vida_emocional WHERE patientId = :patientId LIMIT 1")
    suspend fun getByPatientId(patientId: Long): VidaEmocional?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vidaEmocional: VidaEmocional): Long

    @Update
    suspend fun update(vidaEmocional: VidaEmocional)
} 



