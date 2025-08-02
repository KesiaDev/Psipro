package com.psipro.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.Autoavaliacao
import com.psipro.app.data.repository.AutoavaliacaoRepository
import com.psipro.app.utils.AIMotivationalService
import com.psipro.app.ui.screens.AutoavaliacaoStats
import com.psipro.app.notification.AutoavaliacaoNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import android.content.Context
import android.content.SharedPreferences

@HiltViewModel
class AutoavaliacaoViewModel @Inject constructor(
    private val repository: AutoavaliacaoRepository,
    private val notificationService: AutoavaliacaoNotificationService,
    private val context: Context
) : ViewModel() {
    
    private val aiService = AIMotivationalService()
    
    private val _autoavaliacoes = MutableStateFlow<List<Autoavaliacao>>(emptyList())
    val autoavaliacoes: StateFlow<List<Autoavaliacao>> = _autoavaliacoes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentAutoavaliacao = MutableStateFlow<Autoavaliacao?>(null)
    val currentAutoavaliacao: StateFlow<Autoavaliacao?> = _currentAutoavaliacao.asStateFlow()
    
    private val _stats = MutableStateFlow(AutoavaliacaoStats())
    val stats: StateFlow<AutoavaliacaoStats> = _stats.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("autoavaliacao_prefs", Context.MODE_PRIVATE)
    }
    
    init {
        loadAllAutoavaliacoes()
        loadStats()
        loadNotificationState()
    }
    
    fun loadAllAutoavaliacoes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAll().collect { autoavaliacoes ->
                    _autoavaliacoes.value = autoavaliacoes
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar autoavaliações: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadStats() {
        viewModelScope.launch {
            try {
                val currentMonth = repository.getCurrentMonthAverage()
                val last3Months = repository.getLast3MonthsAverage()
                val last6Months = repository.getLast6MonthsAverage()
                
                _stats.value = AutoavaliacaoStats(
                    currentMonthAverage = currentMonth,
                    last3MonthsAverage = last3Months,
                    last6MonthsAverage = last6Months
                )
            } catch (e: Exception) {
                _error.value = "Erro ao carregar estatísticas: ${e.message}"
            }
        }
    }
    
    fun saveAutoavaliacao(
        bemEstarEmocional: Int,
        satisfacaoProfissional: Int,
        equilibrioVidaTrabalho: Int,
        energiaVital: Int,
        qualidadeSono: Int,
        nivelEstresse: Int,
        principaisDesafios: String,
        conquistasMes: String,
        objetivosProximoMes: String,
        gratidao: String,
        observacoes: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val scoreGeral = calculateScore(
                    bemEstarEmocional, satisfacaoProfissional, equilibrioVidaTrabalho,
                    energiaVital, qualidadeSono, nivelEstresse
                )
                
                val categoriaGeral = getCategoriaGeral(scoreGeral)
                
                val autoavaliacao = Autoavaliacao(
                    dataAvaliacao = Date(),
                    bemEstarEmocional = bemEstarEmocional,
                    satisfacaoProfissional = satisfacaoProfissional,
                    equilibrioVidaTrabalho = equilibrioVidaTrabalho,
                    energiaVital = energiaVital,
                    qualidadeSono = qualidadeSono,
                    nivelEstresse = nivelEstresse,
                    principaisDesafios = principaisDesafios,
                    conquistasMes = conquistasMes,
                    objetivosProximoMes = objetivosProximoMes,
                    gratidao = gratidao,
                    scoreGeral = scoreGeral,
                    categoriaGeral = categoriaGeral,
                    observacoes = observacoes
                )
                
                val id = repository.insert(autoavaliacao)
                _currentAutoavaliacao.value = autoavaliacao.copy(id = id)
                loadStats()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao salvar autoavaliação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAutoavaliacao(autoavaliacao: Autoavaliacao) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.update(autoavaliacao.copy(dataAtualizacao = Date()))
                loadStats()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao atualizar autoavaliação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAutoavaliacao(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteById(id)
                loadStats()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Erro ao deletar autoavaliação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getAutoavaliacaoById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val autoavaliacao = repository.getById(id)
                _currentAutoavaliacao.value = autoavaliacao
            } catch (e: Exception) {
                _error.value = "Erro ao carregar autoavaliação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getLatestAutoavaliacao() {
        viewModelScope.launch {
            try {
                val latest = repository.getLatest()
                _currentAutoavaliacao.value = latest
            } catch (e: Exception) {
                _error.value = "Erro ao carregar última autoavaliação: ${e.message}"
            }
        }
    }
    
    private fun calculateScore(
        bemEstarEmocional: Int,
        satisfacaoProfissional: Int,
        equilibrioVidaTrabalho: Int,
        energiaVital: Int,
        qualidadeSono: Int,
        nivelEstresse: Int
    ): Float {
        return (bemEstarEmocional + satisfacaoProfissional + equilibrioVidaTrabalho + 
                energiaVital + qualidadeSono + nivelEstresse) / 6.0f
    }
    
    private fun getCategoriaGeral(score: Float): String {
        return when {
            score >= 8.5f -> "Excelente"
            score >= 7.0f -> "Bom"
            score >= 5.5f -> "Regular"
            else -> "Precisa Atenção"
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun loadNotificationState() {
        val enabled = prefs.getBoolean("daily_notifications_enabled", false)
        _notificationsEnabled.value = enabled
        
        // Se as notificações estiverem habilitadas, agendar novamente
        if (enabled) {
            notificationService.scheduleDailyMotivationalMessage()
        }
    }
    
    private fun saveNotificationState(enabled: Boolean) {
        prefs.edit().putBoolean("daily_notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }
    
    fun getMotivationalMessage(): String {
        val latest = _currentAutoavaliacao.value
        return if (latest != null) {
            aiService.generateMotivationalMessage(latest)
        } else {
            aiService.generateDefaultMessage()
        }
    }
    
    fun getWeeklyTip(): String {
        return aiService.generateWeeklyTip()
    }
    
    fun getMonthlyReflectionPrompt(): String {
        return aiService.generateMonthlyReflectionPrompt()
    }
    
    // Métodos para gerenciar notificações motivacionais
    fun enableDailyMotivationalNotifications() {
        notificationService.scheduleDailyMotivationalMessage()
        saveNotificationState(true)
    }
    
    fun disableDailyMotivationalNotifications() {
        notificationService.cancelDailyMotivationalMessage()
        saveNotificationState(false)
    }
    
    fun showMotivationalNotificationNow() {
        val message = getMotivationalMessage()
        notificationService.showMotivationalNotification(message)
    }
} 



