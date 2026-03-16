package com.psipro.app.data.repository

import com.psipro.app.data.dao.AutoavaliacaoDao
import com.psipro.app.data.entities.Autoavaliacao
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoavaliacaoRepository @Inject constructor(
    private val autoavaliacaoDao: AutoavaliacaoDao
) {
    
    fun getAll(): Flow<List<Autoavaliacao>> = autoavaliacaoDao.getAll()
    
    suspend fun getById(id: Long): Autoavaliacao? = autoavaliacaoDao.getById(id)
    
    fun getByPeriod(startDate: Date, endDate: Date): Flow<List<Autoavaliacao>> = 
        autoavaliacaoDao.getByPeriod(startDate, endDate)
    
    fun getFromDate(startDate: Date): Flow<List<Autoavaliacao>> = 
        autoavaliacaoDao.getFromDate(startDate)
    
    suspend fun getByPeriodSync(startDate: Date, endDate: Date): List<Autoavaliacao> = 
        autoavaliacaoDao.getByPeriodSync(startDate, endDate)
    
    suspend fun getAverageScoreByPeriod(startDate: Date, endDate: Date): Float? = 
        autoavaliacaoDao.getAverageScoreByPeriod(startDate, endDate)
    
    suspend fun getCountByPeriod(startDate: Date, endDate: Date): Int = 
        autoavaliacaoDao.getCountByPeriod(startDate, endDate)
    
    fun getByCategoryAndPeriod(categoria: String, startDate: Date, endDate: Date): Flow<List<Autoavaliacao>> = 
        autoavaliacaoDao.getByCategoryAndPeriod(categoria, startDate, endDate)
    
    suspend fun insert(autoavaliacao: Autoavaliacao): Long = autoavaliacaoDao.insert(autoavaliacao)
    
    suspend fun update(autoavaliacao: Autoavaliacao) = autoavaliacaoDao.update(autoavaliacao)
    
    suspend fun delete(autoavaliacao: Autoavaliacao) = autoavaliacaoDao.delete(autoavaliacao)
    
    suspend fun deleteById(id: Long) = autoavaliacaoDao.deleteById(id)
    
    suspend fun getLatest(): Autoavaliacao? = autoavaliacaoDao.getLatest()
    
    suspend fun getNextAfter(date: Date): Autoavaliacao? = autoavaliacaoDao.getNextAfter(date)
    
    suspend fun getPreviousBefore(date: Date): Autoavaliacao? = autoavaliacaoDao.getPreviousBefore(date)
    
    // Métodos auxiliares para análise
    suspend fun getCurrentMonthAverage(): Float? {
        val calendar = java.util.Calendar.getInstance()
        val startOfMonth = calendar.apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        val endOfMonth = calendar.apply {
            set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.time
        
        return getAverageScoreByPeriod(startOfMonth, endOfMonth)
    }
    
    suspend fun getLast3MonthsAverage(): Float? {
        val calendar = java.util.Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(java.util.Calendar.MONTH, -3)
        val startDate = calendar.time
        
        return getAverageScoreByPeriod(startDate, endDate)
    }
    
    suspend fun getLast6MonthsAverage(): Float? {
        val calendar = java.util.Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(java.util.Calendar.MONTH, -6)
        val startDate = calendar.time
        
        return getAverageScoreByPeriod(startDate, endDate)
    }
} 



