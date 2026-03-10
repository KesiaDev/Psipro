package com.psipro.app.sync

import android.util.Log
import com.psipro.app.data.dao.DocumentoDao
import com.psipro.app.data.dao.PatientDao
import com.psipro.app.data.entities.Documento
import com.psipro.app.data.entities.TipoDocumento
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.RemoteDocument
import com.psipro.app.sync.api.SyncDocumentPayload
import com.psipro.app.sync.api.SyncDocumentsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Resultado do sync de documentos. */
data class SyncDocumentsResult(
    val success: Boolean,
    val message: String,
    val pushCount: Int = 0,
    val pullCount: Int = 0
)

@Singleton
class SyncDocumentsManager @Inject constructor(
    private val api: BackendApiService,
    private val auth: BackendAuthManager,
    private val store: BackendSessionStore,
    private val documentoDao: DocumentoDao,
    private val patientDao: PatientDao
) {
    suspend fun sync(reason: String) {
        syncWithResult(reason)
    }

    suspend fun syncWithResult(reason: String): SyncDocumentsResult = withContext(Dispatchers.IO) {
        try {
            if (!auth.isBackendAuthenticated()) {
                Log.w(TAG, "SYNC_DOCS_SKIP_NO_BACKEND reason=$reason")
                return@withContext SyncDocumentsResult(false, "Faça login novamente para sincronizar.")
            }

            val clinicId = auth.ensureClinicId()
            if (clinicId.isNullOrBlank()) {
                Log.w(TAG, "SYNC_DOCS_SKIP_NO_CLINIC reason=$reason")
                return@withContext SyncDocumentsResult(false, "Clínica não identificada.")
            }

            Log.i(TAG, "SYNC_DOCUMENTS_START reason=$reason clinicId=$clinicId")
            var pushCount = 0
            var pullCount = 0

            // 1) Push: enviar documentos dirty
            val dirty = documentoDao.getDirtyDocumentos()
            val toPush = mutableListOf<SyncDocumentPayload>()
            val toTrack = mutableListOf<Documento>()
            for (d in dirty) {
                val patient = patientDao.getPatientById(d.patientId) ?: continue
                val patientUuid = patient.uuid ?: continue
                toPush.add(d.toSyncPayload(patientUuid))
                toTrack.add(d)
            }

            if (toPush.isNotEmpty()) {
                val pushResp = api.syncDocuments(clinicId = clinicId, body = SyncDocumentsRequest(toPush))
                if (pushResp.isSuccessful && pushResp.body() != null) {
                    val remoteList = pushResp.body()!!
                    for (i in toTrack.indices) {
                        val localDoc = toTrack[i]
                        val remote = remoteList.getOrNull(i)
                        if (remote != null && remote.id != null) {
                            documentoDao.update(
                                localDoc.copy(
                                    backendId = remote.id,
                                    dirty = false,
                                    lastSyncedAt = Date()
                                )
                            )
                        }
                    }
                    pushCount = toPush.size
                    Log.i(TAG, "SYNC_DOCS_PUSH_OK count=$pushCount")
                } else {
                    val errBody = pushResp.errorBody()?.string() ?: ""
                    Log.e(TAG, "SYNC_DOCS_PUSH_FAIL http=${pushResp.code()} body=$errBody")
                    return@withContext SyncDocumentsResult(
                        false,
                        "Erro ao enviar documentos: ${parseErrorMessage(pushResp.code(), errBody)}"
                    )
                }
            }

            // 2) Pull: buscar documentos do backend
            val updatedAfter = store.getLastDocumentsSyncAtIso()
            val pullResp = api.getDocuments(clinicId = clinicId, patientId = null, updatedAfter = updatedAfter)
            if (pullResp.isSuccessful && pullResp.body() != null) {
                val remoteList = pullResp.body()!!
                for (rp in remoteList) {
                    val patientUuid = rp.patientId ?: continue
                    val patient = patientDao.getPatientByUuid(patientUuid) ?: continue
                    val existing = rp.id?.let { documentoDao.getByBackendId(it) }
                    val merged = mergeRemoteIntoLocal(existing, rp, patient.id)
                    if (existing == null) {
                        documentoDao.insert(merged)
                    } else {
                        documentoDao.update(merged.copy(id = existing.id))
                    }
                }
                val maxUpdated = remoteList.mapNotNull { it.updatedAt }.maxOrNull()
                if (maxUpdated != null) {
                    store.setLastDocumentsSyncAtIso(maxUpdated)
                }
                pullCount = remoteList.size
                Log.i(TAG, "SYNC_DOCS_PULL_OK count=$pullCount")
            } else {
                val errBody = pullResp.errorBody()?.string() ?: ""
                Log.e(TAG, "SYNC_DOCS_PULL_FAIL http=${pullResp.code()} body=$errBody")
                return@withContext SyncDocumentsResult(
                    false,
                    "Erro ao buscar documentos: ${parseErrorMessage(pullResp.code(), errBody)}"
                )
            }

            val msg = when {
                pushCount > 0 && pullCount > 0 -> "Documentos: $pushCount enviados, $pullCount recebidos"
                pushCount > 0 -> "Documentos: $pushCount enviado(s)"
                pullCount > 0 -> "Documentos: $pullCount recebido(s)"
                else -> "Sincronização de documentos concluída"
            }
            SyncDocumentsResult(true, msg, pushCount, pullCount)
        } catch (e: Exception) {
            Log.e(TAG, "SYNC_DOCUMENTS_ERROR reason=$reason", e)
            SyncDocumentsResult(false, "Erro: ${e.message ?: "Verifique sua conexão"}")
        }
    }

    private fun parseErrorMessage(code: Int, body: String): String {
        if (body.contains("x-clinic-id")) return "clínica não identificada"
        if (code == 401) return "sessão expirada"
        if (code == 403) return "sem permissão"
        if (body.length > 100) return "erro $code"
        return body.ifBlank { "erro $code" }
    }

    private fun mergeRemoteIntoLocal(existing: Documento?, rp: RemoteDocument, patientId: Long): Documento {
        val content = rp.content
        val html = when (content) {
            is Map<*, *> -> (content["html"] as? String) ?: (content["conteudo"] as? String) ?: ""
            else -> ""
        }
        val assinaturaPaciente = when (content) {
            is Map<*, *> -> (content["assinaturaPaciente"] as? String) ?: ""
            else -> ""
        }
        val assinaturaProfissional = when (content) {
            is Map<*, *> -> (content["assinaturaProfissional"] as? String) ?: ""
            else -> ""
        }
        val tipo = try {
            TipoDocumento.valueOf(rp.type.uppercase().replace(" ", "_").replace("-", "_"))
        } catch (_: Exception) {
            TipoDocumento.DOCUMENTO_PERSONALIZADO
        }
        val updatedAt = rp.updatedAt?.let { parseIsoToDate(it) } ?: Date()

        return Documento(
            id = existing?.id ?: 0,
            patientId = patientId,
            backendId = rp.id,
            dirty = false,
            titulo = rp.name,
            tipo = tipo,
            conteudo = html.ifBlank { existing?.conteudo ?: "" },
            conteudoOriginal = existing?.conteudoOriginal ?: "",
            dataCriacao = existing?.dataCriacao ?: updatedAt,
            dataModificacao = updatedAt,
            assinaturaPaciente = assinaturaPaciente.ifBlank { existing?.assinaturaPaciente ?: "" },
            assinaturaProfissional = assinaturaProfissional.ifBlank { existing?.assinaturaProfissional ?: "" },
            dataAssinaturaPaciente = existing?.dataAssinaturaPaciente,
            dataAssinaturaProfissional = existing?.dataAssinaturaProfissional,
            caminhoPDF = existing?.caminhoPDF ?: "",
            compartilhado = existing?.compartilhado ?: false,
            observacoes = existing?.observacoes ?: "",
            lastSyncedAt = Date()
        )
    }

    private fun Documento.toSyncPayload(patientUuid: String): SyncDocumentPayload {
        val contentMap = mutableMapOf<String, Any?>(
            "html" to conteudo,
            "conteudo" to conteudo,
            "assinaturaPaciente" to assinaturaPaciente.ifBlank { null },
            "assinaturaProfissional" to assinaturaProfissional.ifBlank { null }
        )
        return SyncDocumentPayload(
            id = backendId,
            patientId = patientUuid,
            name = titulo,
            type = tipo.name,
            fileUrl = caminhoPDF.ifBlank { null },
            content = contentMap,
            status = "Ativo",
            updatedAt = formatDateIsoUtc(dataModificacao),
            source = "app"
        )
    }

    private fun formatDateIsoUtc(date: Date): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date)
    }

    private fun parseIsoToDate(iso: String): Date? {
        return try {
            val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            df.timeZone = TimeZone.getTimeZone("UTC")
            df.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "SyncDocumentsManager"
    }
}
