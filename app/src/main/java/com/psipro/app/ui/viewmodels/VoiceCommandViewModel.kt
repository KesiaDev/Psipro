package com.psipro.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.ui.components.VoiceCommandState
import com.psipro.app.utils.VoiceAction
import com.psipro.app.utils.VoiceCommandManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para comandos de voz. Gerencia o estado do diálogo e emite
 * ações reconhecidas para que a Activity execute a navegação.
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(VoiceCommandState())
    val state: StateFlow<VoiceCommandState> = _state.asStateFlow()

    private val _pendingAction = MutableSharedFlow<VoiceAction>(replay = 0)
    val pendingAction: SharedFlow<VoiceAction> = _pendingAction.asSharedFlow()

    private var voiceCommandManager: VoiceCommandManager? = null

    fun startListening() {
        _state.update {
            it.copy(
                isListening = true,
                recognizedText = null,
                recognizedAction = null,
                error = null
            )
        }
        voiceCommandManager?.destroy()
        voiceCommandManager = VoiceCommandManager(
            context = getApplication(),
            onListeningStarted = { /* já atualizado acima */ },
            onListeningStopped = {
                _state.update { s -> s.copy(isListening = false) }
            },
            onCommandRecognized = { text, action ->
                _state.update {
                    it.copy(
                        isListening = false,
                        recognizedText = text,
                        recognizedAction = action
                    )
                }
                viewModelScope.launch {
                    _pendingAction.emit(action)
                }
            },
            onError = { message ->
                _state.update {
                    it.copy(
                        isListening = false,
                        error = message
                    )
                }
            }
        )
        voiceCommandManager?.startListening()
    }

    fun stopListening() {
        voiceCommandManager?.stopListening()
    }

    fun setError(message: String) {
        _state.update { it.copy(error = message) }
    }

    override fun onCleared() {
        voiceCommandManager?.destroy()
        voiceCommandManager = null
        super.onCleared()
    }
}
