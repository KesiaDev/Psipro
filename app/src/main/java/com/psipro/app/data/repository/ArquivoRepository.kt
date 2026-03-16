package com.psipro.app.data.repository

import com.psipro.app.data.dao.ArquivoDao
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.CategoriaArquivo
import com.psipro.app.data.entities.TipoArquivo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArquivoRepository @Inject constructor(
    private val arquivoDao: ArquivoDao
) {
    fun getArquivosByPatient(patientId: Long): Flow<List<Arquivo>> {
        return arquivoDao.getArquivosByPatient(patientId)
    }

    fun getArquivosByPatientAndCategory(patientId: Long, categoria: CategoriaArquivo): Flow<List<Arquivo>> {
        return arquivoDao.getArquivosByPatientAndCategory(patientId, categoria)
    }

    fun getArquivosByPatientAndType(patientId: Long, tipo: TipoArquivo): Flow<List<Arquivo>> {
        return arquivoDao.getArquivosByPatientAndType(patientId, tipo)
    }

    fun searchArquivosByPatient(patientId: Long, query: String): Flow<List<Arquivo>> {
        return arquivoDao.searchArquivosByPatient(patientId, query)
    }

    suspend fun getArquivoById(id: Long): Arquivo? {
        return arquivoDao.getArquivoById(id)
    }

    suspend fun insertArquivo(arquivo: Arquivo): Long {
        return arquivoDao.insertArquivo(arquivo)
    }

    suspend fun updateArquivo(arquivo: Arquivo) {
        arquivoDao.updateArquivo(arquivo)
    }

    suspend fun deleteArquivo(arquivo: Arquivo) {
        arquivoDao.deleteArquivo(arquivo)
    }

    suspend fun deleteArquivoById(id: Long) {
        arquivoDao.deleteArquivoById(id)
    }

    suspend fun getArquivoCountByPatient(patientId: Long): Int {
        return arquivoDao.getArquivoCountByPatient(patientId)
    }

    suspend fun getTotalSizeByPatient(patientId: Long): Long? {
        return arquivoDao.getTotalSizeByPatient(patientId)
    }
} 



