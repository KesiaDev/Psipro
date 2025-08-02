package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date
import com.psipro.app.data.converters.EncryptionConverter

enum class AnamneseGroup {
    ADULTO, CRIANCAS, ADOLESCENTES, IDOSOS
}

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cpf: String = "",
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
    val clinicalHistory: String? = null,
    @TypeConverters(EncryptionConverter::class)
    val medications: String? = null,
    @TypeConverters(EncryptionConverter::class)
    val allergies: String? = null,
    val isEncrypted: Boolean = false,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    @TypeConverters(com.psipro.app.data.converters.AnamneseGroupConverter::class)
    val anamneseGroup: AnamneseGroup = AnamneseGroup.ADULTO
) 



