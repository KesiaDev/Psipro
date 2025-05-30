package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(
    tableName = "whatsapp_conversations",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class WhatsAppConversation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Long,
    val messageId: String,
    val message: String,
    val timestamp: Date,
    val status: String,
    val direction: String
) 