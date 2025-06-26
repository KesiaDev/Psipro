package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.AnamneseCampo

@Dao
interface AnamneseCampoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(campo: AnamneseCampo): Long

    @Update
    suspend fun update(campo: AnamneseCampo)

    @Delete
    suspend fun delete(campo: AnamneseCampo)

    @Query("SELECT * FROM AnamneseCampo WHERE modeloId = :modeloId")
    suspend fun getByModeloId(modeloId: Long): List<AnamneseCampo>
} 