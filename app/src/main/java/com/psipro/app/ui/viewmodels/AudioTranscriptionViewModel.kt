package com.psipro.app.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.BackendAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*

@HiltViewModel
class AudioTranscriptionViewModel @javax.inject.Inject constructor(
    app: Application,
    private val backendApi: BackendApiService,
    private val backendAuth: BackendAuthManager
) : AndroidViewModel(app) {

    private var isRecordingAudio = false
    private var audioRecord: AudioRecord? = null
    private var wavFile: File? = null
    private var recordingThread: Thread? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    fun startRecording() {
        val context = getApplication<Application>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _status.value = "Permissão de microfone necessária"
            return
        }

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        wavFile = File(dir, "audio_${System.currentTimeMillis()}.wav")
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            audioRecord?.startRecording()
            isRecordingAudio = true
            _isRecording.value = true
            _status.value = "Gravando..."
            recordingThread = Thread {
                writeAudioDataToWavFile(bufferSize, sampleRate, channelConfig, audioFormat)
            }
            recordingThread?.start()
        } catch (e: SecurityException) {
            _status.value = "Erro de permissão: ${e.message}"
        } catch (e: Exception) {
            _status.value = "Erro ao iniciar gravação: ${e.message}"
        }
    }

    fun stopRecording() {
        isRecordingAudio = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
        _isRecording.value = false
        _status.value = "Áudio gravado. Toque em Transcrever para enviar ao servidor."
    }

    private fun writeAudioDataToWavFile(bufferSize: Int, sampleRate: Int, _channelConfig: Int, _audioFormat: Int) {
        val pcmBuffer = ByteArray(bufferSize)
        val file = wavFile ?: return
        val outputStream = FileOutputStream(file)
        val dataOutputStream = DataOutputStream(BufferedOutputStream(outputStream))
        try {
            for (i in 0 until 44) dataOutputStream.write(0)
            var totalBytes = 0
            while (isRecordingAudio) {
                val read = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: 0
                if (read > 0) {
                    dataOutputStream.write(pcmBuffer, 0, read)
                    totalBytes += read
                }
            }
            dataOutputStream.flush()
            val wavHeader = createWavHeader(totalBytes, sampleRate, 1, 16)
            val raf = RandomAccessFile(file, "rw")
            raf.seek(0)
            raf.write(wavHeader)
            raf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            dataOutputStream.close()
        }
    }

    private fun createWavHeader(totalAudioLen: Int, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        writeInt(header, 4, totalDataLen)
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        writeInt(header, 16, 16)
        writeShort(header, 20, 1.toShort())
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * bitsPerSample / 8).toShort())
        writeShort(header, 34, bitsPerSample.toShort())
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        writeInt(header, 40, totalAudioLen)
        return header
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = ((value shr 8) and 0xff).toByte()
        header[offset + 2] = ((value shr 16) and 0xff).toByte()
        header[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }

    fun transcribe(pacienteId: String, dataSessao: String) {
        val file = wavFile
        if (file == null || !file.exists()) {
            _status.value = "Grave ou selecione um áudio primeiro"
            return
        }
        if (!backendAuth.isBackendAuthenticated()) {
            _status.value = "Faça login no backend para usar transcrição de voz."
            return
        }
        _status.value = "Transcrevendo..."
        _isTranscribing.value = true
        viewModelScope.launch {
            try {
                val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val resp = withContext(Dispatchers.IO) { backendApi.transcribe(part) }
                if (resp.isSuccessful) {
                    val transcript = resp.body()?.transcript?.trim() ?: ""
                    _transcription.value = transcript
                    _status.value = "Transcrição concluída!"
                } else {
                    val msg = resp.errorBody()?.string() ?: "Erro ${resp.code()}"
                    _status.value = "Transcrição falhou: $msg"
                }
            } catch (e: retrofit2.HttpException) {
                _status.value = when (e.code()) {
                    401 -> "Faça login no backend para usar transcrição."
                    else -> "Erro ${e.code()}: ${e.message()}"
                }
            } catch (e: Exception) {
                _status.value = "Erro: ${e.message}"
            } finally {
                _isTranscribing.value = false
            }
        }
    }

    fun transcribeFromUri(context: Context, uri: Uri, pacienteId: String, dataSessao: String) {
        if (!backendAuth.isBackendAuthenticated()) {
            _status.value = "Faça login no backend para usar transcrição de voz."
            return
        }
        _status.value = "Processando arquivo..."
        _isTranscribing.value = true
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    _status.value = "Não foi possível abrir o arquivo"
                    return@launch
                }
                val ext = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) {
                        val name = cursor.getString(nameIdx) ?: ""
                        when {
                            name.endsWith(".m4a", ignoreCase = true) -> ".m4a"
                            name.endsWith(".mp3", ignoreCase = true) -> ".mp3"
                            name.endsWith(".wav", ignoreCase = true) -> ".wav"
                            name.endsWith(".webm", ignoreCase = true) -> ".webm"
                            name.endsWith(".ogg", ignoreCase = true) -> ".ogg"
                            name.endsWith(".flac", ignoreCase = true) -> ".flac"
                            name.endsWith(".mp4", ignoreCase = true) -> ".mp4"
                            else -> ".m4a"
                        }
                    } else ".m4a"
                } ?: ".m4a"
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val tempFile = File(dir, "audio_temp_${System.currentTimeMillis()}$ext")
                inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val mimeType = when {
                    ext.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
                    ext.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
                    ext.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                    else -> "audio/mp4"
                }
                val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                val resp = withContext(Dispatchers.IO) { backendApi.transcribe(part) }
                if (resp.isSuccessful) {
                    val transcript = resp.body()?.transcript?.trim() ?: ""
                    _transcription.value = transcript
                    _status.value = "Transcrição concluída!"
                } else {
                    _status.value = "Transcrição falhou: ${resp.code()}"
                }
                try { tempFile.delete() } catch (_: Exception) {}
            } catch (e: Exception) {
                _status.value = "Erro: ${e.message}"
            } finally {
                _isTranscribing.value = false
            }
        }
    }

    /** Permite editar/atualizar o texto da transcrição (para correções manuais) */
    fun updateTranscriptionText(text: String) {
        _transcription.value = text
        _status.value = "Transcrição atualizada"
    }
}
