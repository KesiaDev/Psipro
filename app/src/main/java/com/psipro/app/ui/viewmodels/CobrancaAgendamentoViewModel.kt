package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.CobrancaAgendamento
import com.psipro.app.data.repository.CobrancaAgendamentoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CobrancaAgendamentoViewModel @Inject constructor(
    private val repository: CobrancaAgendamentoRepository
) : ViewModel() {
    
    private val _cobrancas = MutableStateFlow<List<CobrancaAgendamento>>(emptyList())
    val cobrancas: StateFlow<List<CobrancaAgendamento>> = _cobrancas.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadAllCobrancas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllCobrancas().collect { cobrancas ->
                    _cobrancas.value = cobrancas
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadCobrancasByPatient(patientId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getCobrancasByPatient(patientId).collect { cobrancas ->
                    _cobrancas.value = cobrancas
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar cobranças do paciente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createCobranca(
        patientId: Long,
        appointmentId: Long,
        valor: Double,
        dataAgendamento: Date,
        dataEvento: Date, // Data do evento que gerou a cobrança
        dataVencimento: Date,
        motivo: String,
        observacoes: String = ""
    ) {
        viewModelScope.launch {
            try {
                val cobranca = CobrancaAgendamento(
                    patientId = patientId,
                    appointmentId = appointmentId,
                    valor = valor,
                    dataAgendamento = dataAgendamento,
                    dataEvento = dataEvento,
                    dataVencimento = dataVencimento,
                    motivo = motivo,
                    observacoes = observacoes
                )
                repository.insertCobranca(cobranca)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao criar cobrança: ${e.message}"
            }
        }
    }
    
    fun updateCobranca(cobranca: CobrancaAgendamento) {
        viewModelScope.launch {
            try {
                repository.updateCobranca(cobranca)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar cobrança: ${e.message}"
            }
        }
    }
    
    fun deleteCobranca(cobranca: CobrancaAgendamento) {
        viewModelScope.launch {
            try {
                repository.deleteCobranca(cobranca)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao excluir cobrança: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun marcarComoPago(cobrancaId: Long) {
        viewModelScope.launch {
            try {
                repository.marcarComoPago(cobrancaId)
                // Recarregar dados do paciente atual
                val currentCobrancas = _cobrancas.value
                if (currentCobrancas.isNotEmpty()) {
                    loadCobrancasByPatient(currentCobrancas.first().patientId)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao marcar como pago: ${e.message}"
            }
        }
    }
} 



