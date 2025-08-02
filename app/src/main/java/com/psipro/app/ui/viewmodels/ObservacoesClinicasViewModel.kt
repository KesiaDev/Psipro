package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.ObservacoesClinicas
import com.psipro.app.data.repository.ObservacoesClinicasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ObservacoesClinicasViewModel @Inject constructor(
    private val repository: ObservacoesClinicasRepository
) : ViewModel() {
    private val _observacoes = MutableStateFlow<ObservacoesClinicas?>(null)
    val observacoes: StateFlow<ObservacoesClinicas?> = _observacoes.asStateFlow()

    private val _salvoComSucesso = MutableStateFlow(false)
    val salvoComSucesso: StateFlow<Boolean> = _salvoComSucesso.asStateFlow()

    fun carregar(patientId: Long) {
        viewModelScope.launch {
            _observacoes.value = repository.getByPatientId(patientId)
        }
    }

    fun salvar(observacoes: ObservacoesClinicas) {
        viewModelScope.launch {
            repository.insert(observacoes)
            _observacoes.value = observacoes
            _salvoComSucesso.value = true
        }
    }

    fun editar(observacoes: ObservacoesClinicas) {
        viewModelScope.launch {
            repository.update(observacoes)
            _observacoes.value = observacoes
            _salvoComSucesso.value = true
        }
    }

    fun resetarSucesso() {
        _salvoComSucesso.value = false
    }
} 



