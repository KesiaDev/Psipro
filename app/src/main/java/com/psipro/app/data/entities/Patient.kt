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
    /**
     * ID global (UUID) para sincronização Android <-> Web.
     * - Gerado no app ao criar/editar se estiver ausente.
     * - Também é usado como `id` no backend.
     */
    val uuid: String? = null,
    /**
     * Origem do último update local (ANDROID/WEB).
     */
    val origin: String = "ANDROID",
    /**
     * Marca que o registro tem mudanças locais pendentes de envio.
     */
    val dirty: Boolean = true,
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
    /**
     * Última vez que o backend confirmou/persistiu este paciente.
     * Não apaga dados locais; usado apenas como metadado.
     */
    val lastSyncedAt: Date? = null,
    @TypeConverters(com.psipro.app.data.converters.AnamneseGroupConverter::class)
    val anamneseGroup: AnamneseGroup = AnamneseGroup.ADULTO
) 



