package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.FinancialRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialRecordDao {
    @Query("SELECT * FROM financial_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<FinancialRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: FinancialRecord): Long

    @Delete
    suspend fun deleteRecord(record: FinancialRecord)

    @Query("SELECT * FROM financial_records WHERE patientId = :patientId ORDER BY date DESC")
    fun getByPatient(patientId: Long): Flow<List<FinancialRecord>>

    @Update
    suspend fun updateRecord(record: FinancialRecord)
} 