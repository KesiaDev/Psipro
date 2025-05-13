package com.example.apppisc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "whatsapp_conversations")
data class WhatsAppConversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val messageId: String, // ID da mensagem no WhatsApp
    val direction: MessageDirection, // INCOMING ou OUTGOING
    val content: String,
    val timestamp: LocalDateTime,
    val status: MessageStatus,
    val mediaUrls: List<String> = emptyList(), // URLs de mídia (imagens, áudios, etc)
    val metadata: Map<String, String> = emptyMap() // Metadados adicionais
)

enum class MessageDirection {
    INCOMING,
    OUTGOING
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ,
    FAILED
} 