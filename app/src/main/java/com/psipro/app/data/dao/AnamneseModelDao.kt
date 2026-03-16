package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.AnamneseModel

@Dao
interface AnamneseModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: AnamneseModel): Long

    @Update
    suspend fun update(model: AnamneseModel)

    @Delete
    suspend fun delete(model: AnamneseModel)

    @Query("SELECT * FROM AnamneseModel WHERE id = :id")
    suspend fun getById(id: Long): AnamneseModel?

    @Query("SELECT * FROM AnamneseModel")
    suspend fun getAll(): List<AnamneseModel>
} 



