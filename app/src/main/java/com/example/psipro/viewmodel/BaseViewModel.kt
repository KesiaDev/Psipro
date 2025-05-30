package com.example.psipro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

abstract class BaseViewModel : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    
    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _error.value = throwable.message ?: "Erro desconhecido"
        _loading.value = false
    }
    
    protected fun launchWithTimeout(
        timeout: Long = 30,
        unit: TimeUnit = TimeUnit.SECONDS,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _loading.value = true
                _error.value = null
                withTimeout(unit.toMillis(timeout)) {
                    block()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
            } finally {
                _loading.value = false
            }
        }
    }
    
    protected fun <T> launchWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> T
    ) {
        var currentDelay = initialDelay
        var retries = 0
        
        viewModelScope.launch(exceptionHandler) {
            while (retries < maxRetries) {
                try {
                    _loading.value = true
                    _error.value = null
                    block()
                    break
                } catch (e: Exception) {
                    retries++
                    if (retries == maxRetries) {
                        _error.value = "Número máximo de tentativas excedido: ${e.message}"
                        break
                    }
                    kotlinx.coroutines.delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                } finally {
                    _loading.value = false
                }
            }
        }
    }
    
    protected fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        _error.value = null
        _loading.value = false
    }
} 