package com.example.psipro.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.example.psipro.data.converters.EncryptionConverter

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cpf: String,
    val birthDate: Date,
    val phone: String,
    val email: String = "",
    val cep: String = "",
    val endereco: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val complemento: String = "",
    val sessionValue: Double = 0.0,
    val diaCobranca: Int = 1,
    val lembreteCobranca: Boolean = false,
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