package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.HistoricoMedico
import com.example.psipro.data.repository.HistoricoMedicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoricoMedicoViewModel @Inject constructor(
    private val repository: HistoricoMedicoRepository
) : ViewModel() {
    private val _historico = MutableStateFlow<HistoricoMedico?>(null)
    val historico: StateFlow<HistoricoMedico?> = _historico.asStateFlow()

    fun carregar(patientId: Long) {
        viewModelScope.launch {
            _historico.value = repository.getByPatientId(patientId)
        }
    }

    fun salvar(historico: HistoricoMedico) {
        viewModelScope.launch {
            repository.insert(historico)
            _historico.value = historico
        }
    }

    fun editar(historico: HistoricoMedico) {
        viewModelScope.launch {
            repository.update(historico)
            _historico.value = historico
        }
    }
} 