package com.example.psipro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.Prontuario
import com.example.psipro.data.repository.ProntuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProntuarioViewModel @Inject constructor(
    private val repository: ProntuarioRepository
) : ViewModel() {
    private val _prontuarios = MutableStateFlow<List<Prontuario>>(emptyList())
    val prontuarios: StateFlow<List<Prontuario>> = _prontuarios

    fun loadProntuarios(patientId: Long) {
        viewModelScope.launch {
            _prontuarios.value = repository.getProntuariosByPatient(patientId)
        }
    }

    fun saveProntuario(prontuario: Prontuario) {
        viewModelScope.launch {
            repository.insert(prontuario)
            loadProntuarios(prontuario.patientId)
        }
    }

    fun updateProntuario(prontuario: Prontuario) {
        viewModelScope.launch {
            repository.update(prontuario)
            loadProntuarios(prontuario.patientId)
        }
    }

    fun deleteProntuario(prontuario: Prontuario) {
        viewModelScope.launch {
            repository.delete(prontuario)
            loadProntuarios(prontuario.patientId)
        }
    }
} 