package com.psipro.app.data.repository

import com.psipro.app.data.dao.FinancialRecordDao
import com.psipro.app.data.entities.FinancialRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FinancialRecordRepository @Inject constructor(
    private val dao: FinancialRecordDao
) {
    suspend fun insert(record: FinancialRecord) = dao.insertRecord(record)
    suspend fun update(record: FinancialRecord) = dao.updateRecord(record)
    suspend fun delete(record: FinancialRecord) = dao.deleteRecord(record)
    fun getAll(): Flow<List<FinancialRecord>> = dao.getAllRecords()
    fun getByPatient(patientId: Long): Flow<List<FinancialRecord>> = dao.getByPatient(patientId)
} 



