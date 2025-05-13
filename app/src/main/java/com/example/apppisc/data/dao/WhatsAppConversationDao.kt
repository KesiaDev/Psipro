package com.example.apppisc.data.dao

import androidx.room.*
import com.example.apppisc.data.model.WhatsAppConversation
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppConversationDao {
    @Query("SELECT * FROM whatsapp_conversations WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getConversationsForPatient(patientId: Long): Flow<List<WhatsAppConversation>>

    @Query("SELECT * FROM whatsapp_conversations WHERE patientId = :patientId AND timestamp >= :startDate AND timestamp <= :endDate ORDER BY timestamp DESC")
    fun getConversationsForPatientInDateRange(patientId: Long, startDate: Long, endDate: Long): Flow<List<WhatsAppConversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: WhatsAppConversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<WhatsAppConversation>)

    @Update
    suspend fun updateConversation(conversation: WhatsAppConversation)

    @Delete
    suspend fun deleteConversation(conversation: WhatsAppConversation)

    @Query("SELECT COUNT(*) FROM whatsapp_conversations WHERE patientId = :patientId AND status = :status")
    fun getMessageCountByStatus(patientId: Long, status: String): Flow<Int>

    @Query("SELECT * FROM whatsapp_conversations WHERE messageId = :messageId")
    suspend fun getConversationByMessageId(messageId: String): WhatsAppConversation?
} 