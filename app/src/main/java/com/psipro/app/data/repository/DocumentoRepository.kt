package com.psipro.app.data.repository

import com.psipro.app.data.dao.DocumentoDao
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.TipoDocumento
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class DocumentoRepository @Inject constructor(
    private val documentoDao: DocumentoDao
) {
    
    fun getByPatientId(patientId: Long): Flow<List<Documento>> = 
        documentoDao.getByPatientId(patientId)
    
    suspend fun getById(id: Long): Documento? = 
        documentoDao.getById(id)
    
    fun getByTipo(patientId: Long, tipo: TipoDocumento): Flow<List<Documento>> = 
        documentoDao.getByTipo(patientId, tipo)
    
    fun getCompartilhados(): Flow<List<Documento>> = 
        documentoDao.getCompartilhados()
    
    suspend fun getCountByPatient(patientId: Long): Int = 
        documentoDao.getCountByPatient(patientId)
    
    suspend fun getCountAssinados(patientId: Long): Int = 
        documentoDao.getCountAssinados(patientId)
    
    suspend fun insert(documento: Documento): Long {
        android.util.Log.d("DocumentoRepository", "Inserindo documento: ${documento.titulo}")
        val id = documentoDao.insert(documento)
        android.util.Log.d("DocumentoRepository", "Documento inserido com ID: $id")
        return id
    }
    
    suspend fun update(documento: Documento) = 
        documentoDao.update(documento)
    
    suspend fun delete(documento: Documento) = 
        documentoDao.delete(documento)
    
    suspend fun deleteByPatient(patientId: Long) = 
        documentoDao.deleteByPatient(patientId)
    
    suspend fun assinarPaciente(id: Long, assinatura: String, data: Date = Date()) = 
        documentoDao.assinarPaciente(id, assinatura, data)
    
    suspend fun assinarProfissional(id: Long, assinatura: String, data: Date = Date()) = 
        documentoDao.assinarProfissional(id, assinatura, data)
    
    suspend fun atualizarCaminhoPDF(id: Long, caminho: String) = 
        documentoDao.atualizarCaminhoPDF(id, caminho)
    
    suspend fun marcarCompartilhado(id: Long, compartilhado: Boolean) = 
        documentoDao.marcarCompartilhado(id, compartilhado)
} 



