package com.psipro.app.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * Grava áudio usando MediaRecorder e salva em arquivo temporário.
 * Formato: AAC em container MPEG_4 (.m4a) - aceito pelo backend Whisper.
 */
class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): Result<File> {
        return try {
            outputFile = File(context.cacheDir, "psipro_voice_${System.currentTimeMillis()}.m4a")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder = recorder
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(44100)
            recorder.setAudioChannels(1)
            recorder.setOutputFile(outputFile!!.absolutePath)
            recorder.prepare()
            recorder.start()
            Result.success(outputFile!!)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile?.takeIf { it.exists() }
        } catch (e: Exception) {
            null
        } finally {
            mediaRecorder = null
        }
    }

    fun isRecording(): Boolean = mediaRecorder != null
}
