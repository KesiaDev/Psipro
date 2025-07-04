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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.util.Date
import javax.inject.Inject
import com.google.gson.Gson
import com.example.psipro.data.dao.AnamneseModelDao
import com.example.psipro.data.entities.AnamneseModel
import com.example.psipro.ui.screens.SecaoAnamneseType

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

    // Estado da seção selecionada
    private val _secaoSelecionada = MutableStateFlow<SecaoAnamneseType?>(null)
    val secaoSelecionada: StateFlow<SecaoAnamneseType?> = _secaoSelecionada

    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado de erro
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Estado de preenchimento das seções
    private val _secoesPreenchidas = MutableStateFlow<Map<SecaoAnamneseType, Boolean>>(emptyMap())
    val secoesPreenchidas: StateFlow<Map<SecaoAnamneseType, Boolean>> = _secoesPreenchidas

    // Controle de jobs para evitar chamadas concorrentes
    private var carregarAnamnesesJob: Job? = null
    private var carregarModelosJob: Job? = null
    private var carregarCamposJob: Job? = null

    fun setSecaoSelecionada(secao: SecaoAnamneseType?) {
        _secaoSelecionada.value = secao
    }

    fun carregarCampos(modeloId: Long) {
        // Cancelar job anterior se existir
        carregarCamposJob?.cancel()
        
        carregarCamposJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _camposModelo.value = campoDao.getByModeloId(modeloId)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar campos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarAnamnesesPaciente(pacienteId: Long) {
        // Cancelar job anterior se existir
        carregarAnamnesesJob?.cancel()
        
        carregarAnamnesesJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Pequeno delay para evitar chamadas muito frequentes
                delay(100)
                
                _anamneses.value = preenchidaDao.getByPacienteId(pacienteId)
                // Atualizar status de preenchimento das seções
                atualizarStatusPreenchimento(pacienteId)
            } catch (e: Exception) {
                _error.value = "Erro ao carregar anamneses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun atualizarStatusPreenchimento(pacienteId: Long) {
        viewModelScope.launch {
            try {
                val anamnesesPaciente = preenchidaDao.getByPacienteId(pacienteId)
                val statusMap = mutableMapOf<SecaoAnamneseType, Boolean>()
                
                // Verificar se há dados para cada seção baseado no conteúdo das anamneses
                SecaoAnamneseType.values().forEach { secao ->
                    val temDados = anamnesesPaciente.any { anamnese ->
                        try {
                            val gson = Gson()
                            val respostas = gson.fromJson(anamnese.respostas, Map::class.java)
                            
                            // Verificar se há respostas não vazias para esta seção
                            // Isso pode ser expandido para verificar campos específicos
                            respostas.values.any { valor ->
                                valor != null && valor.toString().isNotBlank()
                            }
                        } catch (e: Exception) {
                            // Se não conseguir parsear, considerar como não preenchido
                            false
                        }
                    }
                    
                    statusMap[secao] = temDados
                }
                
                _secoesPreenchidas.value = statusMap
            } catch (e: Exception) {
                // Log do erro mas não falha a operação principal
                println("Erro ao atualizar status de preenchimento: ${e.message}")
                // Definir todos como não preenchidos em caso de erro
                _secoesPreenchidas.value = SecaoAnamneseType.values().associateWith { false }
            }
        }
    }

    fun salvarAnamnese(
        pacienteId: Long,
        modeloId: Long,
        respostas: Map<Long, String>,
        assinaturaPath: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
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
            } catch (e: Exception) {
                _error.value = "Erro ao salvar anamnese: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarModelos() {
        // Cancelar job anterior se existir
        carregarModelosJob?.cancel()
        
        carregarModelosJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _modelos.value = modelDao.getAll()
            } catch (e: Exception) {
                _error.value = "Erro ao carregar modelos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limparErro() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Cancelar todos os jobs quando o ViewModel for destruído
        carregarAnamnesesJob?.cancel()
        carregarModelosJob?.cancel()
        carregarCamposJob?.cancel()
    }

    // CRUD de modelos dinâmicos
    fun criarModelo(nome: String, campos: List<AnamneseCampo>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val novoModelo = AnamneseModel(nome = nome)
                val modeloId = modelDao.insert(novoModelo)
                campos.forEach { campo ->
                    campoDao.insert(campo.copy(modeloId = modeloId))
                }
                carregarModelos()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Erro ao criar modelo: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editarModelo(modelo: AnamneseModel, campos: List<AnamneseCampo>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                modelDao.update(modelo)
                // Remove campos antigos e insere os novos
                val antigos = campoDao.getByModeloId(modelo.id)
                antigos.forEach { campoDao.delete(it) }
                campos.forEach { campo ->
                    campoDao.insert(campo.copy(modeloId = modelo.id))
                }
                carregarModelos()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Erro ao editar modelo: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removerModelo(modelo: AnamneseModel, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val campos = campoDao.getByModeloId(modelo.id)
                campos.forEach { campoDao.delete(it) }
                modelDao.delete(modelo)
                carregarModelos()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Erro ao remover modelo: ${e.message}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun carregarCamposModelo(modeloId: Long): List<AnamneseCampo> {
        return campoDao.getByModeloId(modeloId)
    }
} 