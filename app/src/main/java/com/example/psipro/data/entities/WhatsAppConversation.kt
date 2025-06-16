package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "whatsapp_conversations")
data class WhatsAppConversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val message: String,
    val sentAt: Date = Date()
) 