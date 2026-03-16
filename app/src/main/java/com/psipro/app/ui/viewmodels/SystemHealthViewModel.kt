package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.sync.api.SystemHealthApi
import com.psipro.app.sync.api.SystemHealthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class SystemHealthUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val data: SystemHealthResponse? = null
)

@HiltViewModel
class SystemHealthViewModel @Inject constructor(
    private val systemHealthApi: SystemHealthApi
) : ViewModel() {

    private val _state = MutableStateFlow(SystemHealthUiState())
    val state: StateFlow<SystemHealthUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val response = withContext(Dispatchers.IO) {
                    systemHealthApi.getSystemHealth()
                }
                if (response.isSuccessful) {
                    _state.value = SystemHealthUiState(
                        isLoading = false,
                        data = response.body()
                    )
                } else {
                    _state.value = SystemHealthUiState(
                        isLoading = false,
                        error = "Erro: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _state.value = SystemHealthUiState(
                    isLoading = false,
                    error = e.message ?: "Falha ao conectar"
                )
            }
        }
    }
}
