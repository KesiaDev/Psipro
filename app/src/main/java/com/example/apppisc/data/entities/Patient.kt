package com.example.apppisc.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.example.apppisc.data.converters.EncryptionConverter

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cpf: String,
    val birthDate: Date,
    val phone: String,
    @TypeConverters(EncryptionConverter::class)
    val clinicalHistory: String?,
    @TypeConverters(EncryptionConverter::class)
    val medications: String?,
    @TypeConverters(EncryptionConverter::class)
    val allergies: String?,
    val isEncrypted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) 