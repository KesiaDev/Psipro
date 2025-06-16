package com.example.psipro.data.dao

import androidx.room.*
import com.example.psipro.data.entities.WhatsAppConversation
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppConversationDao {
    @Query("SELECT * FROM whatsapp_conversations WHERE patientId = :patientId ORDER BY sentAt DESC")
    fun getConversationsByPatient(patientId: Long): Flow<List<WhatsAppConversation>>

    @Query("SELECT * FROM whatsapp_conversations WHERE patientId = :patientId AND sentAt BETWEEN :startDate AND :endDate ORDER BY sentAt DESC")
    fun getConversationsInDateRange(patientId: Long, startDate: Long, endDate: Long): Flow<List<WhatsAppConversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: WhatsAppConversation): Long

    @Delete
    suspend fun deleteConversation(conversation: WhatsAppConversation)
} 