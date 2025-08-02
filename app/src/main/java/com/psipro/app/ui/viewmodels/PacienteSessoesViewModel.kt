package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.AnotacaoSessao
import com.psipro.app.data.repository.AnotacaoSessaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PacienteSessoesViewModel @Inject constructor(
    private val repository: AnotacaoSessaoRepository
) : ViewModel() {
    
    private val _sessoes = MutableStateFlow<List<AnotacaoSessao>>(emptyList())
    val sessoes: StateFlow<List<AnotacaoSessao>> = _sessoes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadSessoesByPatient(patientId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getByPatientId(patientId).collect { sessoes ->
                    _sessoes.value = sessoes
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar sessões: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
} 



