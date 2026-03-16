package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

/**
 * Ações que podem ser acionadas por comando de voz.
 */
enum class VoiceAction(val displayName: String) {
    NEW_SESSION("Nova sessão"),
    SEARCH_PATIENT("Buscar paciente"),
    TODAY_AGENDA("Agenda de hoje"),
    HOME("Dashboard"),
    UNKNOWN("")
}

/**
 * Gerencia reconhecimento de voz e interpretação de comandos.
 * Usa SpeechRecognizer para capturar fala e mapeia para ações do app.
 */
class VoiceCommandManager(
    private val context: Context,
    private val onListeningStarted: () -> Unit,
    private val onListeningStopped: () -> Unit,
    private val onCommandRecognized: (String, VoiceAction) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            onListeningStarted()
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            isListening = false
            onListeningStopped()
        }

        override fun onError(error: Int) {
            isListening = false
            onListeningStopped()
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                SpeechRecognizer.ERROR_CLIENT -> "Erro do cliente"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de microfone necessária"
                SpeechRecognizer.ERROR_NETWORK -> "Erro de rede. Verifique a conexão."
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tempo limite de rede"
                SpeechRecognizer.ERROR_NO_MATCH -> "Não reconheci. Tente novamente."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecimento ocupado"
                SpeechRecognizer.ERROR_SERVER -> "Erro no servidor"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Não ouvi nada. Tente novamente."
                else -> "Erro de reconhecimento: $error"
            }
            Log.w(TAG, "SpeechRecognizer error: $error - $message")
            onError(message)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spokenText = matches?.firstOrNull()?.trim() ?: ""
            if (spokenText.isNotEmpty()) {
                val action = interpretCommand(spokenText)
                onCommandRecognized(spokenText, action)
            } else {
                onError("Não foi possível reconhecer a fala")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    fun startListening() {
        if (context.packageManager.queryIntentActivities(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0
            ).isEmpty()
        ) {
            onError("Reconhecimento de voz não disponível neste dispositivo")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR"))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            onError("Erro ao iniciar reconhecimento de voz")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) { /* ignorar */ }
        isListening = false
        onListeningStopped()
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) { /* ignorar */ }
        speechRecognizer = null
        isListening = false
    }

    private fun interpretCommand(text: String): VoiceAction {
        val normalized = text.lowercase().trim()
        return when {
            normalized.contains("nova sessão") || normalized.contains("nova sessao") ||
            normalized.contains("novo agendamento") || normalized.contains("agendar") -> VoiceAction.NEW_SESSION
            normalized.contains("buscar paciente") || normalized.contains("buscar pacientes") ||
            normalized.contains("listar paciente") || normalized.contains("pacientes") -> VoiceAction.SEARCH_PATIENT
            normalized.contains("agenda") || normalized.contains("agenda de hoje") ||
            normalized.contains("agenda hoje") || normalized.contains("calendário") ||
            normalized.contains("calendario") || normalized.contains("hoje") -> VoiceAction.TODAY_AGENDA
            normalized.contains("dashboard") || normalized.contains("início") ||
            normalized.contains("inicio") || normalized.contains("home") -> VoiceAction.HOME
            else -> VoiceAction.UNKNOWN
        }
    }

    companion object {
        private const val TAG = "VoiceCommandManager"
    }
}
