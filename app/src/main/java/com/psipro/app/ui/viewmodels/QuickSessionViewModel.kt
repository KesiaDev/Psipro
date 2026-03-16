package com.psipro.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psipro.app.data.entities.AnotacaoSessao
import com.psipro.app.data.entities.AppointmentStatus
import com.psipro.app.data.repository.AnotacaoSessaoRepository
import com.psipro.app.data.repository.AppointmentRepository
import com.psipro.app.data.repository.PatientRepository
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.CreateSessionRequest
import com.psipro.app.sync.api.SessionResponse
import com.psipro.app.sync.api.VoiceNoteRequest
import com.psipro.app.sync.BackendAuthManager
import com.psipro.app.voice.VoiceRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/** Insights clínicos retornados pela IA */
data class SessionInsights(
    val summary: String,
    val themes: List<String>,
    val emotions: List<String>
)

@HiltViewModel
class QuickSessionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val anotacaoRepository: AnotacaoSessaoRepository,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val backendApi: BackendApiService,
    private val backendAuth: BackendAuthManager
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess.asStateFlow()

    private val _nextSessionNumber = MutableStateFlow(1)
    val nextSessionNumber: StateFlow<Int> = _nextSessionNumber.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _sessionTimeStr = MutableStateFlow("")
    val sessionTimeStr: StateFlow<String> = _sessionTimeStr.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val _todayAppointmentId = MutableStateFlow<Long?>(null)
    val todayAppointmentId: StateFlow<Long?> = _todayAppointmentId.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _isGeneratingInsights = MutableStateFlow(false)
    val isGeneratingInsights: StateFlow<Boolean> = _isGeneratingInsights.asStateFlow()

    private val _insights = MutableStateFlow<SessionInsights?>(null)
    val insights: StateFlow<SessionInsights?> = _insights.asStateFlow()

    /** Backend sessionId quando insights foram gerados (para sync) */
    private val _backendSessionId = MutableStateFlow<String?>(null)
    val backendSessionId: StateFlow<String?> = _backendSessionId.asStateFlow()

    private var recorder: VoiceRecorder? = null

    private var timerJob: Job? = null
    private var autoSaveJob: Job? = null

    val emotionalTags = listOf("Ansiedade", "Estresse", "Progresso", "Crise", "Estável")

    fun loadSessionData(patientId: Long) {
        viewModelScope.launch {
            try {
                val anotacoes = anotacaoRepository.getByPatientId(patientId).first()
                _nextSessionNumber.value = anotacoes.size + 1

                val appointments = appointmentRepository.getAppointmentsByPatient(patientId).first()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todayAppointment = appointments.find { app ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(app.date) == todayStr &&
                    app.status == AppointmentStatus.CONFIRMADO
                }
                todayAppointment?.let {
                    _todayAppointmentId.value = it.id
                    _sessionTimeStr.value = it.startTime
                } ?: run {
                    _sessionTimeStr.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                }
            } catch (_: Exception) {
                _nextSessionNumber.value = 1
                _sessionTimeStr.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            }
        }
    }

    fun updateNotes(text: String) {
        _notes.value = text
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500)
            _saveError.value = null
        }
    }

    fun toggleTag(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun endSession(
        patientId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _saveError.value = null
            stopTimer()
            try {
                val numeroSessao = _nextSessionNumber.value
                val tagsStr = _selectedTags.value.joinToString(", ")
                val durationMin = (_elapsedSeconds.value / 60).coerceAtLeast(1)
                val observacoesFull = buildString {
                    append(_notes.value.trim())
                    if (tagsStr.isNotEmpty()) {
                        if (isNotEmpty()) append("\n\n")
                        append("Tags: $tagsStr")
                    }
                    append("\nDuração: ${durationMin} min")
                }

                val backendId = _backendSessionId.value
                val anotacao = AnotacaoSessao(
                    patientId = patientId,
                    numeroSessao = numeroSessao,
                    dataHora = Date(),
                    assuntos = "",
                    estadoEmocional = tagsStr,
                    intervencoes = "",
                    tarefas = "",
                    evolucao = "",
                    observacoes = observacoesFull.trim(),
                    metaTerapeutica = "",
                    proximoAgendamento = "",
                    backendId = backendId
                )
                anotacaoRepository.insert(anotacao)

                _todayAppointmentId.value?.let { appointmentId ->
                    appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.REALIZADO)
                }
                _showSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _saveError.value = e.message ?: "Erro ao salvar"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSuccess() {
        _showSuccess.value = false
    }

    fun startRecording() {
        if (_isRecording.value) return
        recorder = VoiceRecorder(context)
        val result = recorder!!.startRecording()
        result.onSuccess {
            _isRecording.value = true
            _saveError.value = null
        }.onFailure {
            _saveError.value = "Erro ao iniciar gravação: ${it.message}"
        }
    }

    fun stopAndTranscribe(patientId: Long) {
        if (!_isRecording.value) return
        val rec = recorder ?: return
        val file = rec.stopRecording()
        recorder = null
        _isRecording.value = false
        if (file == null || !file.exists()) {
            _saveError.value = "Erro ao salvar áudio"
            return
        }
        viewModelScope.launch {
            _isTranscribing.value = true
            _saveError.value = null
            var transcript = ""
            try {
                val requestFile = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val resp = withContext(Dispatchers.IO) { backendApi.transcribe(part) }
                if (resp.isSuccessful) {
                    val body = resp.body()
                    transcript = body?.transcript?.trim() ?: ""
                    if (transcript.isNotEmpty()) {
                        val current = _notes.value
                        _notes.value = if (current.isBlank()) transcript else "$current\n\n$transcript"
                    }
                } else {
                    val msg = resp.errorBody()?.string() ?: "Erro ${resp.code()}"
                    _saveError.value = "Transcrição falhou: $msg"
                }
            } catch (e: HttpException) {
                _saveError.value = when (e.code()) {
                    401 -> "Faça login no backend para usar transcrição de voz."
                    else -> "Erro ${e.code()}: ${e.message()}"
                }
            } catch (e: Exception) {
                _saveError.value = "Erro ao transcrever: ${e.message}"
            } finally {
                _isTranscribing.value = false
                try { file.delete() } catch (_: Exception) {}
            }

            // Gerar insights da IA se transcript disponível e backend conectado
            if (transcript.isNotEmpty() && backendAuth.isBackendAuthenticated()) {
                fetchInsights(patientId, transcript)
            }
        }
    }

    private suspend fun fetchInsights(patientId: Long, transcript: String) {
        val patientUuid = withContext(Dispatchers.IO) {
            patientRepository.getPatientById(patientId)?.uuid
        }
        if (patientUuid.isNullOrBlank()) return

        _isGeneratingInsights.value = true
        _insights.value = null
        _backendSessionId.value = null

        try {
            val dateIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())
            val duration = (_elapsedSeconds.value / 60).coerceAtLeast(1)

            val createResp = withContext(Dispatchers.IO) {
                backendApi.createSession(
                    CreateSessionRequest(
                        patientId = patientUuid,
                        date = dateIso,
                        duration = duration,
                        status = "realizada",
                        notes = transcript,
                        source = "app"
                    )
                )
            }
            if (!createResp.isSuccessful || createResp.body() == null) {
                return@fetchInsights
            }

            val sessionId = createResp.body()!!.id
            val voiceResp = withContext(Dispatchers.IO) {
                backendApi.voiceNote(VoiceNoteRequest(sessionId = sessionId, transcript = transcript))
            }
            if (voiceResp.isSuccessful) {
                val session = voiceResp.body()
                if (session != null) {
                    _backendSessionId.value = sessionId
                    _insights.value = SessionInsights(
                        summary = session.summary.orEmpty(),
                        themes = session.themes.orEmpty(),
                        emotions = session.emotions.orEmpty()
                    )
                }
            }
        } catch (_: Exception) {
            // Offline ou erro: transcript já está em notes, salvar normalmente
        } finally {
            _isGeneratingInsights.value = false
        }
    }
}
