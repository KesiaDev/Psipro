package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.CategoriaArquivo
import com.psipro.app.data.entities.TipoArquivo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ArquivoManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ArquivoManager"
        private const val ARQUIVOS_FOLDER = "patient_files"
        private const val IMAGENS_FOLDER = "patient_images"
        private const val AUDIOS_FOLDER = "patient_audios"
    }
    
    private var currentPhotoUri: Uri? = null
    
    fun processarArquivoSelecionado(
        uri: Uri,
        patientId: Long,
        onSuccess: (Arquivo) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = getFileNameFromUri(uri)
            val fileExtension = getFileExtension(fileName)
            val tipoArquivo = getTipoArquivoFromExtension(fileExtension)
            val categoriaArquivo = CategoriaArquivo.OUTROS // Padrão, pode ser alterado depois
            
            val timestamp = System.currentTimeMillis()
            val newFileName = "${timestamp}_${fileName}"
            val file = createFileInFolder(ARQUIVOS_FOLDER, newFileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream?.use { input ->
                    input.copyTo(outputStream)
                }
            }
            
            val arquivo = Arquivo(
                patientId = patientId,
                nome = fileName,
                caminhoArquivo = file.absolutePath,
                tipoArquivo = tipoArquivo,
                categoriaArquivo = categoriaArquivo,
                tamanhoBytes = file.length(),
                descricao = null,
                dataUpload = Date(),
                dataModificacao = Date()
            )
            
            onSuccess(arquivo)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar arquivo selecionado: ${e.message}")
            onError("Erro ao processar arquivo: ${e.message}")
        }
    }
    
    fun iniciarCapturaFoto(launcher: ActivityResultLauncher<Uri>) {
        try {
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            launcher.launch(currentPhotoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar captura de foto: ${e.message}")
        }
    }
    
    fun processarFotoCapturada(
        patientId: Long,
        onSuccess: (Arquivo) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            currentPhotoUri?.let { uri ->
                val fileName = "foto_${System.currentTimeMillis()}.jpg"
                val file = createFileInFolder(IMAGENS_FOLDER, fileName)
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                
                val arquivo = Arquivo(
                    patientId = patientId,
                    nome = fileName,
                    caminhoArquivo = file.absolutePath,
                    tipoArquivo = TipoArquivo.IMAGEM,
                    categoriaArquivo = CategoriaArquivo.MATERIAIS_PACIENTE,
                    tamanhoBytes = file.length(),
                    descricao = "Foto capturada em ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date())}",
                    dataUpload = Date(),
                    dataModificacao = Date()
                )
                
                onSuccess(arquivo)
                currentPhotoUri = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar foto capturada: ${e.message}")
            onError("Erro ao processar foto: ${e.message}")
        }
    }
    
    fun processarAudioSelecionado(
        uri: Uri,
        patientId: Long,
        onSuccess: (Arquivo) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fileName = getFileNameFromUri(uri)
            val timestamp = System.currentTimeMillis()
            val newFileName = "${timestamp}_${fileName}"
            val file = createFileInFolder(AUDIOS_FOLDER, newFileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            val arquivo = Arquivo(
                patientId = patientId,
                nome = fileName,
                caminhoArquivo = file.absolutePath,
                tipoArquivo = TipoArquivo.AUDIO,
                categoriaArquivo = CategoriaArquivo.MATERIAIS_PACIENTE,
                tamanhoBytes = file.length(),
                descricao = "Áudio gravado em ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date())}",
                dataUpload = Date(),
                dataModificacao = Date()
            )
            
            onSuccess(arquivo)
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar áudio selecionado: ${e.message}")
            onError("Erro ao processar áudio: ${e.message}")
        }
    }
    
    fun abrirArquivo(context: Context, arquivo: Arquivo) {
        try {
            val file = File(arquivo.caminhoArquivo)
            if (!file.exists()) {
                Log.e(TAG, "Arquivo não encontrado: ${arquivo.caminhoArquivo}")
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(arquivo.tipoArquivo))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Log.e(TAG, "Nenhum app encontrado para abrir arquivo do tipo: ${arquivo.tipoArquivo}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir arquivo: ${e.message}")
        }
    }
    
    fun compartilharArquivo(context: Context, arquivo: Arquivo) {
        try {
            val file = File(arquivo.caminhoArquivo)
            if (!file.exists()) {
                Log.e(TAG, "Arquivo não encontrado: ${arquivo.caminhoArquivo}")
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(arquivo.tipoArquivo)
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, arquivo.nome)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Compartilhar arquivo"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao compartilhar arquivo: ${e.message}")
        }
    }
    
    private fun createFileInFolder(folderName: String, fileName: String): File {
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder, fileName)
    }
    
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.filesDir, IMAGENS_FOLDER)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    private fun getFileNameFromUri(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "arquivo_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            "arquivo_${System.currentTimeMillis()}"
        }
    }
    
    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".", "").lowercase()
        } else {
            ""
        }
    }
    
    private fun getTipoArquivoFromExtension(extension: String): TipoArquivo {
        return when (extension) {
            "pdf" -> TipoArquivo.PDF
            "jpg", "jpeg", "png", "gif", "bmp" -> TipoArquivo.IMAGEM
            "mp4", "avi", "mov", "mkv" -> TipoArquivo.VIDEO
            "mp3", "wav", "aac", "ogg" -> TipoArquivo.AUDIO
            "doc", "docx", "txt", "rtf" -> TipoArquivo.DOCUMENTO
            "xls", "xlsx", "csv" -> TipoArquivo.PLANILHA
            else -> TipoArquivo.OUTRO
        }
    }
    
    private fun getMimeType(tipoArquivo: TipoArquivo): String {
        return when (tipoArquivo) {
            TipoArquivo.PDF -> "application/pdf"
            TipoArquivo.IMAGEM -> "image/*"
            TipoArquivo.VIDEO -> "video/*"
            TipoArquivo.AUDIO -> "audio/*"
            TipoArquivo.DOCUMENTO -> "text/*"
            TipoArquivo.PLANILHA -> "application/vnd.ms-excel"
            TipoArquivo.OUTRO -> "*/*"
        }
    }
} 



