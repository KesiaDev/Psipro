package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "documentos",
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
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    /** ID do documento no backend (UUID). Usado para sync app↔web. */
    val backendId: String? = null,
    /** Marca alterações locais pendentes de envio ao backend. */
    val dirty: Boolean = true,
    val titulo: String,
    val tipo: TipoDocumento,
    val conteudo: String, // Conteúdo HTML/rich text
    val conteudoOriginal: String, // Template original
    val dataCriacao: Date = Date(),
    val dataModificacao: Date = Date(),
    val assinaturaPaciente: String = "", // Base64 da assinatura
    val assinaturaProfissional: String = "", // Base64 da assinatura
    val dataAssinaturaPaciente: Date? = null,
    val dataAssinaturaProfissional: Date? = null,
    val caminhoPDF: String = "", // Caminho do arquivo PDF gerado
    val compartilhado: Boolean = false,
    val observacoes: String = "",
    /** Última vez que o backend confirmou este documento (sync). */
    val lastSyncedAt: Date? = null
)

enum class TipoDocumento {
    TERMO_CONSENTIMENTO,
    TERMO_CONFIDENCIALIDADE,
    ENCAMINHAMENTO_PSICOLOGICO,
    DECLARACAO_COMPARECIMENTO,
    SOLICITACAO_EXAMES,
    AUTORIZACAO_IMAGEM,
    DOCUMENTO_PERSONALIZADO
} 



