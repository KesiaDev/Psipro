package com.psipro.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.psipro.app.data.converters.DateConverter
import java.util.Date

enum class CategoriaArquivo {
    EXAMES_MEDICOS,
    TESTES_PSICOLOGICOS,
    MATERIAIS_PACIENTE,
    LAUDOS_RECEBIDOS,
    OUTROS
}

enum class TipoArquivo {
    PDF,
    IMAGEM,
    VIDEO,
    AUDIO,
    DOCUMENTO,
    PLANILHA,
    OUTRO
}

@Entity(
    tableName = "arquivos",
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
data class Arquivo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val nome: String,
    val caminhoArquivo: String,
    val tipoArquivo: TipoArquivo,
    val categoriaArquivo: CategoriaArquivo,
    val tamanhoBytes: Long,
    val descricao: String? = null,
    @TypeConverters(DateConverter::class)
    val dataUpload: Date = Date(),
    @TypeConverters(DateConverter::class)
    val dataModificacao: Date = Date(),
    val isEncrypted: Boolean = false,
    val hashArquivo: String? = null
) 



