package com.psipro.app.ui.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import org.json.JSONObject
import java.io.*
import android.content.Context
import android.net.Uri

class AudioTranscriptionViewModel(app: Application) : AndroidViewModel(app) {
    private var isRecordingAudio = false
    private var audioRecord: AudioRecord? = null
    private var wavFile: File? = null
    private var recordingThread: Thread? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription

    private val _docId = MutableStateFlow<String?>(null)
    val docId: StateFlow<String?> = _docId

    fun startRecording() {
        val context = getApplication<Application>()
        
        // Verificar permissão de microfone
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            != PackageManager.PERMISSION_GRANTED) {
            _status.value = "Permissão de microfone necessária"
            return
        }
        
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        wavFile = File(dir, "audio.wav")
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
        _status.value = "Áudio gravado: ${wavFile?.absolutePath}"
    }

    private fun writeAudioDataToWavFile(bufferSize: Int, sampleRate: Int, _channelConfig: Int, _audioFormat: Int) {
        val pcmBuffer = ByteArray(bufferSize)
        val outputStream = FileOutputStream(wavFile)
        val dataOutputStream = DataOutputStream(BufferedOutputStream(outputStream))
        try {
            // Placeholder for WAV header
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
            // Escreve o header WAV
            val wavHeader = createWavHeader(totalBytes, sampleRate, 1, 16)
            val raf = RandomAccessFile(wavFile, "rw")
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
        _status.value = "Transcrevendo..."
        viewModelScope.launch(Dispatchers.IO) {
            transcreverAudioVosk(pacienteId, dataSessao)
        }
    }

    private fun transcreverAudioVosk(pacienteId: String, dataSessao: String) {
        StorageService.unpack(
            getApplication(),
            "model-pt",
            "model-pt",
            { modelPath: Model ->
                try {
                    val recognizer = Recognizer(modelPath, 16000.0f)
                    val inputStream = FileInputStream(wavFile!!)
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } >= 0) {
                        recognizer.acceptWaveForm(buffer, bytesRead)
                    }
                    val result = recognizer.finalResult
                    val texto = JSONObject(result).optString("text")
                    _transcription.value = texto
                    _status.value = "Transcrição concluída!"
                    salvarTranscricaoFirestore(pacienteId, dataSessao, wavFile!!.absolutePath, texto)
                } catch (e: Exception) {
                    _status.value = "Erro na transcrição: ${e.message}"
                }
            },
            { exception ->
                _status.value = "Erro ao carregar modelo: ${exception.message}"
            }
        )
    }

    private fun salvarTranscricaoFirestore(
        pacienteId: String,
        dataSessao: String,
        urlAudio: String,
        textoTranscricao: String
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "pacienteId" to pacienteId,
            "dataSessao" to dataSessao,
            "urlAudio" to urlAudio,
            "textoTranscricao" to textoTranscricao
        )
        firestore.collection("transcricoes")
            .add(data)
            .addOnSuccessListener { docRef ->
                _docId.value = docRef.id
                _status.value = "Transcrição salva no Firestore!"
            }
            .addOnFailureListener { e ->
                _status.value = "Erro ao salvar no Firestore: ${e.message}"
            }
    }

    fun atualizarTranscricaoFirestore(novoTexto: String) {
        val docId = _docId.value ?: return
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("transcricoes").document(docId)
            .update("textoTranscricao", novoTexto)
            .addOnSuccessListener { _status.value = "Transcrição atualizada!" }
            .addOnFailureListener { e -> _status.value = "Erro ao atualizar: ${e.message}" }
    }

    fun transcribeFromUri(context: Context, uri: Uri, pacienteId: String, dataSessao: String) {
        _status.value = "Processando arquivo..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Copia o arquivo do Uri para um arquivo temporário WAV
                val inputStream = context.contentResolver.openInputStream(uri)
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                val tempFile = File(dir, "audio_temp.wav")
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                wavFile = tempFile
                _status.value = "Arquivo pronto para transcrição."
                transcreverAudioVosk(pacienteId, dataSessao)
            } catch (e: Exception) {
                _status.value = "Erro ao processar arquivo: ${e.message}"
            }
        }
    }
} 



