package com.psipro.app.data.dao

import androidx.room.*
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.TipoDocumento
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DocumentoDao {
    
    @Query("SELECT * FROM documentos WHERE patientId = :patientId ORDER BY dataModificacao DESC")
    fun getByPatientId(patientId: Long): Flow<List<Documento>>
    
    @Query("SELECT * FROM documentos WHERE id = :id")
    suspend fun getById(id: Long): Documento?
    
    @Query("SELECT * FROM documentos WHERE tipo = :tipo AND patientId = :patientId ORDER BY dataModificacao DESC")
    fun getByTipo(patientId: Long, tipo: TipoDocumento): Flow<List<Documento>>
    
    @Query("SELECT * FROM documentos WHERE compartilhado = 1 ORDER BY dataModificacao DESC")
    fun getCompartilhados(): Flow<List<Documento>>
    
    @Query("SELECT COUNT(*) FROM documentos WHERE patientId = :patientId")
    suspend fun getCountByPatient(patientId: Long): Int
    
    @Query("SELECT COUNT(*) FROM documentos WHERE patientId = :patientId AND assinaturaPaciente != ''")
    suspend fun getCountAssinados(patientId: Long): Int
    
    @Insert
    suspend fun insert(documento: Documento): Long
    
    @Update
    suspend fun update(documento: Documento)
    
    @Delete
    suspend fun delete(documento: Documento)
    
    @Query("DELETE FROM documentos WHERE patientId = :patientId")
    suspend fun deleteByPatient(patientId: Long)
    
    @Query("UPDATE documentos SET assinaturaPaciente = :assinatura, dataAssinaturaPaciente = :data WHERE id = :id")
    suspend fun assinarPaciente(id: Long, assinatura: String, data: Date)
    
    @Query("UPDATE documentos SET assinaturaProfissional = :assinatura, dataAssinaturaProfissional = :data WHERE id = :id")
    suspend fun assinarProfissional(id: Long, assinatura: String, data: Date)
    
    @Query("UPDATE documentos SET caminhoPDF = :caminho WHERE id = :id")
    suspend fun atualizarCaminhoPDF(id: Long, caminho: String)
    
    @Query("UPDATE documentos SET compartilhado = :compartilhado WHERE id = :id")
    suspend fun marcarCompartilhado(id: Long, compartilhado: Boolean)

    @Query("SELECT * FROM documentos WHERE dirty = 1")
    suspend fun getDirtyDocumentos(): List<Documento>

    @Query("SELECT * FROM documentos WHERE backendId = :backendId LIMIT 1")
    suspend fun getByBackendId(backendId: String): Documento?

    @Query("UPDATE documentos SET dirty = 0, lastSyncedAt = :syncedAt WHERE backendId IN (:backendIds)")
    suspend fun markDocumentosSyncedByBackendId(backendIds: List<String>, syncedAt: java.util.Date)
} 



