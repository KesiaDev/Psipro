package com.psipro.app.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AttachmentManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AttachmentManager"
        private const val IMAGES_FOLDER = "patient_notes_images"
        private const val AUDIO_FOLDER = "patient_notes_audio"
    }
    
    private val gson = Gson()
    
    // Salvar imagem
    fun saveImage(uri: Uri, patientId: Long, noteId: Long): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${patientId}_${noteId}_${System.currentTimeMillis()}.jpg"
            val file = createFileInFolder(IMAGES_FOLDER, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream?.use { input ->
                    input.copyTo(outputStream)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar imagem: ${e.message}")
            null
        }
    }
    
    // Salvar áudio
    fun saveAudio(uri: Uri, patientId: Long, noteId: Long): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "audio_${patientId}_${noteId}_${System.currentTimeMillis()}.mp3"
            val file = createFileInFolder(AUDIO_FOLDER, fileName)
            
            FileOutputStream(file).use { outputStream ->
                inputStream?.use { input ->
                    input.copyTo(outputStream)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar áudio: ${e.message}")
            null
        }
    }
    
    // Obter lista de imagens
    fun getImageAttachments(imagePathsJson: String): List<String> {
        return if (imagePathsJson.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(imagePathsJson, type) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao parsear imagens: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    // Obter lista de áudios
    fun getAudioAttachments(audioPathsJson: String): List<String> {
        return if (audioPathsJson.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(audioPathsJson, type) ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao parsear áudios: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    // Adicionar imagem
    fun addImageAttachment(currentImagesJson: String, newImagePath: String): String {
        val currentImages = getImageAttachments(currentImagesJson).toMutableList()
        currentImages.add(newImagePath)
        return gson.toJson(currentImages)
    }
    
    // Adicionar áudio
    fun addAudioAttachment(currentAudiosJson: String, newAudioPath: String): String {
        val currentAudios = getAudioAttachments(currentAudiosJson).toMutableList()
        currentAudios.add(newAudioPath)
        return gson.toJson(currentAudios)
    }
    
    // Remover imagem
    fun removeImageAttachment(currentImagesJson: String, imagePath: String): String {
        val currentImages = getImageAttachments(currentImagesJson).toMutableList()
        currentImages.remove(imagePath)
        
        // Deletar arquivo físico
        try {
            File(imagePath).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar arquivo: ${e.message}")
        }
        
        return gson.toJson(currentImages)
    }
    
    // Remover áudio
    fun removeAudioAttachment(currentAudiosJson: String, audioPath: String): String {
        val currentAudios = getAudioAttachments(currentAudiosJson).toMutableList()
        currentAudios.remove(audioPath)
        
        // Deletar arquivo físico
        try {
            File(audioPath).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar arquivo: ${e.message}")
        }
        
        return gson.toJson(currentAudios)
    }
    
    // Criar arquivo em pasta específica
    private fun createFileInFolder(folderName: String, fileName: String): File {
        val folder = File(context.filesDir, folderName)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        return File(folder, fileName)
    }
    
    // Limpar anexos de uma anotação
    fun clearAttachments(note: com.psipro.app.data.entities.PatientNote) {
        // Limpar imagens
        getImageAttachments(note.imageAttachments).forEach { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao deletar imagem: ${e.message}")
            }
        }
        
        // Limpar áudios
        getAudioAttachments(note.audioAttachments).forEach { path ->
            try {
                File(path).delete()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao deletar áudio: ${e.message}")
            }
        }
    }
} 



