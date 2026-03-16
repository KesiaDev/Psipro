package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.CategoriaArquivo
import com.psipro.app.data.entities.TipoArquivo
import com.psipro.app.data.repository.ArquivoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArquivoViewModel @Inject constructor(
    private val arquivoRepository: ArquivoRepository
) : ViewModel() {

    private val _arquivos = MutableStateFlow<List<Arquivo>>(emptyList())
    val arquivos: StateFlow<List<Arquivo>> = _arquivos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedCategoria = MutableStateFlow<CategoriaArquivo?>(null)
    val selectedCategoria: StateFlow<CategoriaArquivo?> = _selectedCategoria

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun carregarArquivos(patientId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val arquivosList = arquivoRepository.getArquivosByPatient(patientId).first()
                _arquivos.value = arquivosList
                
            } catch (e: Exception) {
                _error.value = "Erro ao carregar arquivos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filtrarPorCategoria(patientId: Long, categoria: CategoriaArquivo?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _selectedCategoria.value = categoria
                
                val arquivosList = if (categoria != null) {
                    arquivoRepository.getArquivosByPatientAndCategory(patientId, categoria).first()
                } else {
                    arquivoRepository.getArquivosByPatient(patientId).first()
                }
                
                _arquivos.value = arquivosList
                
            } catch (e: Exception) {
                _error.value = "Erro ao filtrar arquivos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscarArquivos(patientId: Long, query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _searchQuery.value = query
                
                val arquivosList = if (query.isNotEmpty()) {
                    arquivoRepository.searchArquivosByPatient(patientId, query).first()
                } else {
                    arquivoRepository.getArquivosByPatient(patientId).first()
                }
                
                _arquivos.value = arquivosList
                
            } catch (e: Exception) {
                _error.value = "Erro ao buscar arquivos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun adicionarArquivo(arquivo: Arquivo) {
        viewModelScope.launch {
            try {
                val id = arquivoRepository.insertArquivo(arquivo)
                // Recarregar a lista após adicionar
                carregarArquivos(arquivo.patientId)
            } catch (e: Exception) {
                _error.value = "Erro ao adicionar arquivo: ${e.message}"
            }
        }
    }

    fun deletarArquivo(arquivo: Arquivo) {
        viewModelScope.launch {
            try {
                arquivoRepository.deleteArquivo(arquivo)
                // Recarregar a lista após deletar
                carregarArquivos(arquivo.patientId)
            } catch (e: Exception) {
                _error.value = "Erro ao deletar arquivo: ${e.message}"
            }
        }
    }

    fun limparFiltros(patientId: Long) {
        _selectedCategoria.value = null
        _searchQuery.value = ""
        carregarArquivos(patientId)
    }

    fun getCategorias(): List<CategoriaArquivo> {
        return CategoriaArquivo.values().toList()
    }

    fun getTiposArquivo(): List<TipoArquivo> {
        return TipoArquivo.values().toList()
    }

    fun getCategoriaString(categoria: CategoriaArquivo): String {
        return when (categoria) {
            CategoriaArquivo.EXAMES_MEDICOS -> "Exames Médicos"
            CategoriaArquivo.TESTES_PSICOLOGICOS -> "Testes Psicológicos"
            CategoriaArquivo.MATERIAIS_PACIENTE -> "Materiais do Paciente"
            CategoriaArquivo.LAUDOS_RECEBIDOS -> "Laudos Recebidos"
            CategoriaArquivo.OUTROS -> "Outros"
        }
    }

    fun getTipoArquivoString(tipo: TipoArquivo): String {
        return when (tipo) {
            TipoArquivo.PDF -> "PDF"
            TipoArquivo.IMAGEM -> "Imagem"
            TipoArquivo.VIDEO -> "Vídeo"
            TipoArquivo.AUDIO -> "Áudio"
            TipoArquivo.DOCUMENTO -> "Documento"
            TipoArquivo.PLANILHA -> "Planilha"
            TipoArquivo.OUTRO -> "Outro"
        }
    }

    fun formatarTamanhoArquivo(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
} 



