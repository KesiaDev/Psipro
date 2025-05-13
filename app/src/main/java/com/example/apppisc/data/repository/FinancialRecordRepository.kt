package com.example.apppisc.data.repository

import com.example.apppisc.data.dao.FinancialRecordDao
import com.example.apppisc.data.entities.FinancialRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FinancialRecordRepository @Inject constructor(
    private val dao: FinancialRecordDao
) {
    suspend fun insert(record: FinancialRecord) = dao.insert(record)
    suspend fun update(record: FinancialRecord) = dao.update(record)
    suspend fun delete(record: FinancialRecord) = dao.delete(record)
    fun getAll(): Flow<List<FinancialRecord>> = dao.getAll()
    fun getByPatient(patientId: Long): Flow<List<FinancialRecord>> = dao.getByPatient(patientId)
} 