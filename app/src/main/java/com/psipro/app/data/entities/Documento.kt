package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
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
    ]
)
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
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
    val observacoes: String = ""
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



