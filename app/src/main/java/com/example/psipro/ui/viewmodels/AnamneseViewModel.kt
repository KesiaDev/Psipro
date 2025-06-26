package com.example.psipro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.dao.AnamneseCampoDao
import com.example.psipro.data.dao.AnamnesePreenchidaDao
import com.example.psipro.data.entities.AnamneseCampo
import com.example.psipro.data.entities.AnamnesePreenchida
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import com.google.gson.Gson
import com.example.psipro.data.dao.AnamneseModelDao
import com.example.psipro.data.entities.AnamneseModel

@HiltViewModel
class AnamneseViewModel @Inject constructor(
    private val campoDao: AnamneseCampoDao,
    private val preenchidaDao: AnamnesePreenchidaDao,
    private val modelDao: AnamneseModelDao
) : ViewModel() {

    private val _camposModelo = MutableStateFlow<List<AnamneseCampo>>(emptyList())
    val camposModelo: StateFlow<List<AnamneseCampo>> = _camposModelo

    private val _anamneses = MutableStateFlow<List<AnamnesePreenchida>>(emptyList())
    val anamneses: StateFlow<List<AnamnesePreenchida>> = _anamneses

    private val _modelos = MutableStateFlow<List<AnamneseModel>>(emptyList())
    val modelos: StateFlow<List<AnamneseModel>> = _modelos

    fun carregarCampos(modeloId: Long) {
        viewModelScope.launch {
            _camposModelo.value = campoDao.getByModeloId(modeloId)
        }
    }

    fun carregarAnamnesesPaciente(pacienteId: Long) {
        viewModelScope.launch {
            _anamneses.value = preenchidaDao.getByPacienteId(pacienteId)
        }
    }

    fun salvarAnamnese(
        pacienteId: Long,
        modeloId: Long,
        respostas: Map<Long, String>,
        assinaturaPath: String? = null
    ) {
        viewModelScope.launch {
            val gson = Gson()
            val respostasJson = gson.toJson(respostas)
            val nova = AnamnesePreenchida(
                pacienteId = pacienteId,
                modeloId = modeloId,
                respostas = respostasJson,
                assinaturaPath = assinaturaPath,
                data = Date(),
                versao = 1
            )
            preenchidaDao.insert(nova)
            carregarAnamnesesPaciente(pacienteId)
        }
    }

    fun carregarModelos() {
        viewModelScope.launch {
            _modelos.value = modelDao.getAll()
        }
    }
} 