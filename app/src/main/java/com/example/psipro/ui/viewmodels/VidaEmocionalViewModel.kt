package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.VidaEmocional
import com.example.psipro.data.repository.VidaEmocionalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VidaEmocionalViewModel @Inject constructor(
    private val repository: VidaEmocionalRepository
) : ViewModel() {
    private val _vidaEmocional = MutableStateFlow<VidaEmocional?>(null)
    val vidaEmocional: StateFlow<VidaEmocional?> = _vidaEmocional.asStateFlow()

    private val _salvoComSucesso = MutableStateFlow(false)
    val salvoComSucesso: StateFlow<Boolean> = _salvoComSucesso.asStateFlow()

    fun carregar(patientId: Long) {
        viewModelScope.launch {
            _vidaEmocional.value = repository.getByPatientId(patientId)
        }
    }

    fun salvar(vidaEmocional: VidaEmocional) {
        viewModelScope.launch {
            repository.insert(vidaEmocional)
            _vidaEmocional.value = vidaEmocional
            _salvoComSucesso.value = true
        }
    }

    fun editar(vidaEmocional: VidaEmocional) {
        viewModelScope.launch {
            repository.update(vidaEmocional)
            _vidaEmocional.value = vidaEmocional
            _salvoComSucesso.value = true
        }
    }

    fun resetarSucesso() {
        _salvoComSucesso.value = false
    }
} 