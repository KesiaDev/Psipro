package com.psipro.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psipro.app.data.entities.Arquivo
import com.psipro.app.data.entities.CategoriaArquivo
import com.psipro.app.data.entities.TipoArquivo
import com.psipro.app.ui.viewmodels.ArquivoViewModel
import com.psipro.app.utils.ArquivoManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArquivosScreen(
    patientId: Long,
    patientName: String,
    onBack: () -> Unit,
    viewModel: ArquivoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val arquivos by viewModel.arquivos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategoria by viewModel.selectedCategoria.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Arquivo?>(null) }
    var showCategoriaFilter by remember { mutableStateOf(false) }
    
    val arquivoManager = remember { ArquivoManager(context) }
    
    // Launcher para selecionar arquivos
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            arquivoManager.processarArquivoSelecionado(
                selectedUri,
                patientId,
                onSuccess = { arquivo ->
                    viewModel.adicionarArquivo(arquivo)
                },
                onError = { errorMessage ->
                    Log.e("ArquivosScreen", "Erro ao processar arquivo: $errorMessage")
                }
            )
        }
    }
    
    // Launcher para câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            arquivoManager.processarFotoCapturada(
                patientId,
                onSuccess = { arquivo ->
                    viewModel.adicionarArquivo(arquivo)
                },
                onError = { errorMessage ->
                    Log.e("ArquivosScreen", "Erro ao processar foto: $errorMessage")
                }
            )
        }
    }
    
    // Launcher para gravação de áudio
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            arquivoManager.processarAudioSelecionado(
                selectedUri,
                patientId,
                onSuccess = { arquivo ->
                    viewModel.adicionarArquivo(arquivo)
                },
                onError = { errorMessage ->
                    Log.e("ArquivosScreen", "Erro ao processar áudio: $errorMessage")
                }
            )
        }
    }
    
    LaunchedEffect(patientId) {
        viewModel.carregarArquivos(patientId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arquivos - $patientName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showCategoriaFilter = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar arquivo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    viewModel.buscarArquivos(patientId, query)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar arquivos...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Filtro de categoria ativo
            selectedCategoria?.let { categoria ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtro: ${viewModel.getCategoriaString(categoria)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.limparFiltros(patientId) }) {
                            Text("Limpar")
                        }
                    }
                }
            }
            
            // Lista de arquivos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (arquivos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum arquivo encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque no + para adicionar arquivos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(arquivos) { arquivo ->
                        ArquivoCard(
                            arquivo = arquivo,
                            onView = { arquivoManager.abrirArquivo(context, arquivo) },
                            onShare = { arquivoManager.compartilharArquivo(context, arquivo) },
                            onDelete = { showDeleteDialog = arquivo },
                            formatarTamanho = { viewModel.formatarTamanhoArquivo(it) },
                            getCategoriaString = { viewModel.getCategoriaString(it) },
                            getTipoString = { viewModel.getTipoArquivoString(it) }
                        )
                    }
                }
            }
        }
    }
    
    // Dialog para adicionar arquivo
    if (showAddDialog) {
        AdicionarArquivoDialog(
            onDismiss = { showAddDialog = false },
            onSelectFile = { fileLauncher.launch("*/*") },
            onTakePhoto = { arquivoManager.iniciarCapturaFoto(cameraLauncher) },
            onRecordAudio = { audioLauncher.launch("audio/*") }
        )
    }
    
    // Dialog de confirmação de exclusão
    showDeleteDialog?.let { arquivo ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir arquivo") },
            text = { Text("Tem certeza que deseja excluir \"${arquivo.nome}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletarArquivo(arquivo)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Dialog de filtro por categoria
    if (showCategoriaFilter) {
        FiltrarCategoriaDialog(
            categorias = viewModel.getCategorias(),
            selectedCategoria = selectedCategoria,
            onCategoriaSelected = { categoria ->
                viewModel.filtrarPorCategoria(patientId, categoria)
                showCategoriaFilter = false
            },
            onDismiss = { showCategoriaFilter = false },
            getCategoriaString = { viewModel.getCategoriaString(it) }
        )
    }
    
    // Exibir erro se houver
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Aqui você pode mostrar um Snackbar ou Toast
            Log.e("ArquivosScreen", errorMessage)
        }
    }
}

@Composable
fun ArquivoCard(
    arquivo: Arquivo,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    formatarTamanho: (Long) -> String,
    getCategoriaString: (CategoriaArquivo) -> String,
    getTipoString: (TipoArquivo) -> String
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone baseado no tipo
                val icon = when (arquivo.tipoArquivo) {
                    TipoArquivo.PDF -> Icons.Default.PictureAsPdf
                    TipoArquivo.IMAGEM -> Icons.Default.Image
                    TipoArquivo.VIDEO -> Icons.Default.VideoLibrary
                    TipoArquivo.AUDIO -> Icons.Default.AudioFile
                    TipoArquivo.DOCUMENTO -> Icons.Default.Description
                    TipoArquivo.PLANILHA -> Icons.Default.TableChart
                    TipoArquivo.OUTRO -> Icons.Default.InsertDriveFile
                }
                
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = arquivo.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${getTipoString(arquivo.tipoArquivo)} • ${formatarTamanho(arquivo.tamanhoBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = getCategoriaString(arquivo.categoriaArquivo),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = dateFormat.format(arquivo.dataUpload),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            arquivo.descricao?.let { descricao ->
                if (descricao.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Visualizar")
                }
                
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartilhar")
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AdicionarArquivoDialog(
    onDismiss: () -> Unit,
    onSelectFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onRecordAudio: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Arquivo") },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onSelectFile()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Selecionar arquivo")
                }
                
                TextButton(
                    onClick = {
                        onTakePhoto()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tirar foto")
                }
                
                TextButton(
                    onClick = {
                        onRecordAudio()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gravar áudio")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun FiltrarCategoriaDialog(
    categorias: List<CategoriaArquivo>,
    selectedCategoria: CategoriaArquivo?,
    onCategoriaSelected: (CategoriaArquivo?) -> Unit,
    onDismiss: () -> Unit,
    getCategoriaString: (CategoriaArquivo) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar por Categoria") },
        text = {
            Column {
                TextButton(
                    onClick = {
                        onCategoriaSelected(null)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Todas as categorias")
                }
                
                categorias.forEach { categoria ->
                    TextButton(
                        onClick = {
                            onCategoriaSelected(categoria)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(getCategoriaString(categoria))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 



