package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.entities.FinancialRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: FinancialRecord): Long

    @Update
    suspend fun update(record: FinancialRecord)

    @Delete
    suspend fun delete(record: FinancialRecord)

    @Query("SELECT * FROM financial_records ORDER BY date DESC")
    fun getAll(): Flow<List<FinancialRecord>>

    @Query("SELECT * FROM financial_records WHERE patientId = :patientId ORDER BY date DESC")
    fun getByPatient(patientId: Long): Flow<List<FinancialRecord>>
} 