package com.example.psipro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.psipro.data.entities.AuditLog
import com.example.psipro.data.repository.AuditLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuditLog(
    val id: Long = 0,
    val userId: Long?,
    val action: String,
    val target: String,
    val timestamp: Long
)

class AuditLogViewModel @Inject constructor(
    private val repository: AuditLogRepository
) : ViewModel() {
    private val _logs = MutableStateFlow<List<AuditLog>>(emptyList())
    val logs: StateFlow<List<AuditLog>> = _logs

    fun loadLogs() {
        viewModelScope.launch {
            _logs.value = repository.getAllLogs()
        }
    }
} 