package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.Autoavaliacao
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AutoavaliacaoDao {
    
    @Query("SELECT * FROM autoavaliacoes ORDER BY dataAvaliacao DESC")
    fun getAll(): Flow<List<Autoavaliacao>>
    
    @Query("SELECT * FROM autoavaliacoes WHERE id = :id")
    suspend fun getById(id: Long): Autoavaliacao?
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao >= :startDate AND dataAvaliacao <= :endDate ORDER BY dataAvaliacao DESC")
    fun getByPeriod(startDate: Date, endDate: Date): Flow<List<Autoavaliacao>>
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao >= :startDate ORDER BY dataAvaliacao DESC")
    fun getFromDate(startDate: Date): Flow<List<Autoavaliacao>>
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao >= :startDate AND dataAvaliacao <= :endDate ORDER BY dataAvaliacao DESC")
    suspend fun getByPeriodSync(startDate: Date, endDate: Date): List<Autoavaliacao>
    
    @Query("SELECT AVG(scoreGeral) FROM autoavaliacoes WHERE dataAvaliacao >= :startDate AND dataAvaliacao <= :endDate")
    suspend fun getAverageScoreByPeriod(startDate: Date, endDate: Date): Float?
    
    @Query("SELECT COUNT(*) FROM autoavaliacoes WHERE dataAvaliacao >= :startDate AND dataAvaliacao <= :endDate")
    suspend fun getCountByPeriod(startDate: Date, endDate: Date): Int
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao >= :startDate AND dataAvaliacao <= :endDate AND categoriaGeral = :categoria ORDER BY dataAvaliacao DESC")
    fun getByCategoryAndPeriod(categoria: String, startDate: Date, endDate: Date): Flow<List<Autoavaliacao>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(autoavaliacao: Autoavaliacao): Long
    
    @Update
    suspend fun update(autoavaliacao: Autoavaliacao)
    
    @Delete
    suspend fun delete(autoavaliacao: Autoavaliacao)
    
    @Query("DELETE FROM autoavaliacoes WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM autoavaliacoes ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun getLatest(): Autoavaliacao?
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao >= :date ORDER BY dataAvaliacao ASC LIMIT 1")
    suspend fun getNextAfter(date: Date): Autoavaliacao?
    
    @Query("SELECT * FROM autoavaliacoes WHERE dataAvaliacao <= :date ORDER BY dataAvaliacao DESC LIMIT 1")
    suspend fun getPreviousBefore(date: Date): Autoavaliacao?
} 



