package com.psipro.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Utilitário de leitura em voz alta usando TextToSpeech do Android.
 * Funciona 100% offline usando os dados de fala instalados no dispositivo.
 * Utilizado para acessibilidade, permitindo que usuários cegos ou com baixa visão
 * ouçam o resumo da sessão gerado pela IA.
 */
class VoiceReader(context: Context) {

    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS && ::tts.isInitialized) {
                tts.language = Locale("pt", "BR")
            }
        }
    }

    /**
     * Lê o texto em voz alta. Usa QUEUE_FLUSH para interromper qualquer fala em andamento.
     */
    fun speak(text: String) {
        if (text.isNotBlank()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "psipro_voice")
        }
    }

    /**
     * Interrompe a fala em andamento.
     */
    fun stop() {
        tts.stop()
    }

    /**
     * Libera os recursos do TextToSpeech. Deve ser chamado quando o componente
     * que usa o VoiceReader for descartado.
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
