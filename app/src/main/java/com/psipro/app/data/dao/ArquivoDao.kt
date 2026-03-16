package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.CategoriaArquivo
import com.psipro.app.data.entities.TipoArquivo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArquivoDao {
    @Query("SELECT * FROM arquivos WHERE patientId = :patientId ORDER BY dataUpload DESC")
    fun getArquivosByPatient(patientId: Long): Flow<List<Arquivo>>

    @Query("SELECT * FROM arquivos WHERE patientId = :patientId AND categoriaArquivo = :categoria ORDER BY dataUpload DESC")
    fun getArquivosByPatientAndCategory(patientId: Long, categoria: CategoriaArquivo): Flow<List<Arquivo>>

    @Query("SELECT * FROM arquivos WHERE patientId = :patientId AND tipoArquivo = :tipo ORDER BY dataUpload DESC")
    fun getArquivosByPatientAndType(patientId: Long, tipo: TipoArquivo): Flow<List<Arquivo>>

    @Query("SELECT * FROM arquivos WHERE patientId = :patientId AND (nome LIKE '%' || :query || '%' OR descricao LIKE '%' || :query || '%') ORDER BY dataUpload DESC")
    fun searchArquivosByPatient(patientId: Long, query: String): Flow<List<Arquivo>>

    @Query("SELECT * FROM arquivos WHERE id = :id")
    suspend fun getArquivoById(id: Long): Arquivo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArquivo(arquivo: Arquivo): Long

    @Update
    suspend fun updateArquivo(arquivo: Arquivo)

    @Delete
    suspend fun deleteArquivo(arquivo: Arquivo)

    @Query("DELETE FROM arquivos WHERE id = :id")
    suspend fun deleteArquivoById(id: Long)

    @Query("SELECT COUNT(*) FROM arquivos WHERE patientId = :patientId")
    suspend fun getArquivoCountByPatient(patientId: Long): Int

    @Query("SELECT SUM(tamanhoBytes) FROM arquivos WHERE patientId = :patientId")
    suspend fun getTotalSizeByPatient(patientId: Long): Long?
} 



