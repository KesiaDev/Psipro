package com.psipro.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.ui.components.PsiproCard
import com.psipro.app.ui.theme.psipro.PsiproSpacing
import com.psipro.app.ui.viewmodels.QuickSessionViewModel
import com.psipro.app.ui.viewmodels.SessionInsights
import com.psipro.app.utils.VoiceReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSessionScreen(
    patientId: Long,
    patientName: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: QuickSessionViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val sessionTimeStr by viewModel.sessionTimeStr.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val isLoading by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()
    val showSuccess by viewModel.showSuccess.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val isGeneratingInsights by viewModel.isGeneratingInsights.collectAsState()
    val insights by viewModel.insights.collectAsState()

    val context = LocalContext.current
    val voiceReader = remember { VoiceReader(context.applicationContext) }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose { voiceReader.shutdown() }
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) viewModel.startRecording()
    }

    LaunchedEffect(patientId) {
        viewModel.loadSessionData(patientId)
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar(
                message = "Sessão registrada com sucesso.",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccess()
            delay(1500)
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Sessão") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(PsiproSpacing.screenPadding)
        ) {
            // 1. Cabeçalho do paciente
            PsiproCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarWithInitials(patientName = patientName)
                    Spacer(Modifier.width(PsiproSpacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = patientName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Horário da sessão: ${sessionTimeStr.ifEmpty { "---" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(PsiproSpacing.md))

            // 2. Cronômetro
            PsiproCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Cronômetro da sessão: ${formatElapsed(elapsedSeconds)}. ${if (isTimerRunning) "Rodando" else "Parado"}" }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatElapsed(elapsedSeconds),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(PsiproSpacing.sm))
                    Button(
                        onClick = {
                            if (isTimerRunning) viewModel.stopTimer()
                            else viewModel.startTimer()
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Parar cronômetro" else "Iniciar sessão",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isTimerRunning) "Parar" else "Iniciar sessão")
                    }
                }
            }
            Spacer(Modifier.height(PsiproSpacing.md))

            // 3. Campo de notas + Gravação
            PsiproCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Observações da sessão. Campo de texto para anotações. Botão para gravar áudio." }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Observações",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(
                            onClick = {
                                if (isRecording) {
                                    viewModel.stopAndTranscribe(patientId)
                                } else if (hasAudioPermission) {
                                    viewModel.startRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            enabled = !isTranscribing && !isLoading && !isGeneratingInsights,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = if (isRecording) "Parar e transcrever gravação" else "Gravar sessão",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isRecording) "Parar e transcrever" else "Gravar sessão",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Spacer(Modifier.height(PsiproSpacing.sm))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        placeholder = {
                            Text("Escreva observações rápidas da sessão...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .semantics { contentDescription = "Notas da sessão" },
                        minLines = 5,
                        maxLines = 8,
                        shape = MaterialTheme.shapes.medium,
                        readOnly = isTranscribing
                    )
                    if (isTranscribing) {
                        Spacer(Modifier.height(PsiproSpacing.sm))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Transcrevendo sessão...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isGeneratingInsights) {
                        Spacer(Modifier.height(PsiproSpacing.sm))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Gerando análise da sessão...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(PsiproSpacing.md))

            // 3b. Card Resumo da IA (insights) - LiveRegion para anunciar ao TalkBack
            insights?.let { ins ->
                val insightDesc = if (ins.summary.isNotEmpty()) ins.summary
                    else "Temas: ${ins.themes.joinToString()}. Emoções: ${ins.emotions.joinToString()}"
                PsiproCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Resumo da sessão gerado pela inteligência artificial. $insightDesc"
                        }
                ) {
                    Column {
                        Text(
                            text = "Resumo da IA",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = PsiproSpacing.sm)
                        )
                        if (ins.summary.isNotEmpty()) {
                            Text(
                                text = ins.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = PsiproSpacing.sm)
                            )
                        }
                        if (ins.themes.isNotEmpty()) {
                            Text(
                                text = "Temas principais",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PsiproSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ins.themes.take(6).forEach { theme ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(theme, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(PsiproSpacing.sm))
                        }
                        if (ins.emotions.isNotEmpty()) {
                            Text(
                                text = "Emoções",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PsiproSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ins.emotions.take(6).forEach { emotion ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(emotion, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(PsiproSpacing.sm))
                        Button(
                            onClick = { voiceReader.speak(buildInsightSpokenText(ins)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Ouvir resumo da sessão em voz alta"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ouvir resumo da sessão")
                        }
                    }
                }
                Spacer(Modifier.height(PsiproSpacing.md))
            }

            // 4. Tags emocionais
            PsiproCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "Estado emocional",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = PsiproSpacing.sm)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(PsiproSpacing.sm)
                    ) {
                        listOf(
                            viewModel.emotionalTags.take(3),
                            viewModel.emotionalTags.drop(3)
                        ).forEach { rowTags ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(PsiproSpacing.sm)
                            ) {
                                rowTags.forEach { tag ->
                                    FilterChip(
                                        selected = tag in selectedTags,
                                        onClick = { viewModel.toggleTag(tag) },
                                        label = { Text(tag) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(PsiproSpacing.md))

            // Erro
            saveError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = PsiproSpacing.sm)
                )
            }

            // 5. Botão encerrar sessão
            Button(
                onClick = {
                    viewModel.endSession(patientId = patientId, onSuccess = { })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Encerrar sessão e salvar" },
                enabled = !isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Encerrar sessão")
                }
            }
        }
    }
}

@Composable
private fun AvatarWithInitials(patientName: String) {
    val initials = remember(patientName) {
        patientName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .take(2)
            .ifEmpty { "?" }
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun buildInsightSpokenText(ins: SessionInsights): String {
    val parts = buildList {
        add("Resumo da sessão.")
        if (ins.summary.isNotEmpty()) {
            add(ins.summary)
        }
        if (ins.themes.isNotEmpty()) {
            add("Temas principais: ${ins.themes.joinToString(", ")}.")
        }
        if (ins.emotions.isNotEmpty()) {
            add("Emoções predominantes: ${ins.emotions.joinToString(" e ")}.")
        }
    }
    return parts.joinToString(" ")
}

private fun formatElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
