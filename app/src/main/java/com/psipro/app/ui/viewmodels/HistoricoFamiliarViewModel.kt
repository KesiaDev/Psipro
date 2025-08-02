package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.HistoricoFamiliar
import com.psipro.app.data.repository.HistoricoFamiliarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoricoFamiliarViewModel @Inject constructor(
    private val repository: HistoricoFamiliarRepository
) : ViewModel() {
    private val _historico = MutableStateFlow<HistoricoFamiliar?>(null)
    val historico: StateFlow<HistoricoFamiliar?> = _historico.asStateFlow()

    private val _salvoComSucesso = MutableStateFlow(false)
    val salvoComSucesso: StateFlow<Boolean> = _salvoComSucesso.asStateFlow()

    fun carregar(patientId: Long) {
        viewModelScope.launch {
            _historico.value = repository.getByPatientId(patientId)
        }
    }

    fun salvar(historico: HistoricoFamiliar) {
        viewModelScope.launch {
            repository.insert(historico)
            _historico.value = historico
            _salvoComSucesso.value = true
        }
    }

    fun editar(historico: HistoricoFamiliar) {
        viewModelScope.launch {
            repository.update(historico)
            _historico.value = historico
            _salvoComSucesso.value = true
        }
    }

    fun resetarSucesso() {
        _salvoComSucesso.value = false
    }
} 



