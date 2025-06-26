package com.example.psipro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.psipro.data.entities.AnamneseCampo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.util.Base64
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.toArgb
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@Composable
fun FormularioAnamneseScreen(
    campos: List<AnamneseCampo>,
    onSalvar: (Map<Long, String>, List<String>, String?, List<Uri>) -> Unit, // respostas, tags, assinatura, anexos
    onCancelar: () -> Unit
) {
    val respostas = remember { mutableStateMapOf<Long, String>() }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var assinatura by remember { mutableStateOf<String?>(null) } // base64 ou url
    var erro by remember { mutableStateOf<String?>(null) }
    var anexos by remember { mutableStateOf(listOf<Uri>()) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(campos) { campo ->
            val obrigatorio = campo.obrigatorio
            val labelText = if (obrigatorio) "${campo.label} *" else campo.label
            when (campo.tipo) {
                "TEXTO_CURTO" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "TEXTO_LONGO" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 4.dp),
                        maxLines = 5
                    )
                }
                "DATA" -> {
                    OutlinedTextField(
                        value = respostas[campo.id] ?: "",
                        onValueChange = { respostas[campo.id] = it },
                        label = { Text(labelText) },
                        isError = obrigatorio && (respostas[campo.id]?.isBlank() != false),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
                "SELECAO_UNICA" -> {
                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                    var selecionado by remember { mutableStateOf(respostas[campo.id] ?: "") }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(labelText)
                        opcoes.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                RadioButton(
                                    selected = selecionado == opcao,
                                    onClick = {
                                        selecionado = opcao
                                        respostas[campo.id] = opcao
                                    }
                                )
                                Text(opcao, Modifier.padding(start = 8.dp))
                            }
                        }
                        if (obrigatorio && (respostas[campo.id]?.isBlank() != false)) {
                            Text("Campo obrigatório", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                "MULTIPLA_ESCOLHA" -> {
                    val opcoes = campo.opcoes?.split(",") ?: emptyList()
                    val selecionados = remember { mutableStateListOf<String>() }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(labelText)
                        opcoes.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = selecionados.contains(opcao),
                                    onCheckedChange = { checked ->
                                        if (checked) selecionados.add(opcao) else selecionados.remove(opcao)
                                        respostas[campo.id] = selecionados.joinToString(",")
                                    }
                                )
                                Text(opcao, Modifier.padding(start = 8.dp))
                            }
                        }
                        if (obrigatorio && (respostas[campo.id]?.isBlank() != false)) {
                            Text("Selecione pelo menos uma opção", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                "ESCALA" -> {
                    // Exemplo: escala de 1 a 10
                    var valor by remember { mutableStateOf(respostas[campo.id]?.toIntOrNull() ?: 5) }
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text(labelText)
                        Slider(
                            value = valor.toFloat(),
                            onValueChange = {
                                valor = it.toInt()
                                respostas[campo.id] = valor.toString()
                            },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                        Text("Valor: $valor", style = MaterialTheme.typography.bodySmall)
                    }
                }
                "TITULO" -> {
                    Text(
                        text = campo.label,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        item {
            // Anexos, tags e assinatura (esqueleto)
            Spacer(Modifier.height(16.dp))
            Text("Tags (ex: trauma, TDAH, ansiedade)", style = MaterialTheme.typography.titleSmall)
            // Campo de tags (simples)
            OutlinedTextField(
                value = tags.joinToString(", "),
                onValueChange = { tags = it.split(",").map { t -> t.trim() }.filter { t -> t.isNotBlank() } },
                label = { Text("Tags separadas por vírgula") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Anexos", style = MaterialTheme.typography.titleSmall)
            AnexosField(anexos = anexos, onAnexosChange = { anexos = it })
            Spacer(Modifier.height(16.dp))
            Text("Assinatura Digital do Paciente", style = MaterialTheme.typography.titleSmall)
            AssinaturaDigitalField(
                onAssinaturaChange = { assinatura = it }
            )
            Spacer(Modifier.height(16.dp))
            // Anexos e assinatura: placeholder
            Text("Anexos e assinatura digital serão implementados aqui.", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            erro?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        val obrigatoriosNaoPreenchidos = campos.filter { it.obrigatorio && (respostas[it.id]?.isBlank() != false) }
                        if (obrigatoriosNaoPreenchidos.isNotEmpty()) {
                            erro = "Preencha todos os campos obrigatórios marcados com *"
                        } else {
                            erro = null
                            onSalvar(respostas, tags, assinatura, anexos)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Salvar Anamnese")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
fun AssinaturaDigitalField(
    onAssinaturaChange: (String?) -> Unit
) {
    val path = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val strokeWidth = 3.dp
    val color = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val view = LocalView.current

    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .padding(4.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = listOf(offset)
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                path.add(currentPath)
                                currentPath = emptyList()
                            }
                            // Exportar para base64
                            onAssinaturaChange(exportSignatureToBase64(path, strokeWidth, color))
                        },
                        onDragCancel = {
                            currentPath = emptyList()
                        },
                        onDrag = { change, _ ->
                            currentPath = currentPath + change.position
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                path.forEach { points ->
                    if (points.size > 1) {
                        val drawPath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = drawPath,
                            color = color,
                            style = Stroke(width = with(density) { strokeWidth.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
                // Desenhar o traço atual
                if (currentPath.size > 1) {
                    val drawPath = Path().apply {
                        moveTo(currentPath.first().x, currentPath.first().y)
                        currentPath.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = drawPath,
                        color = color,
                        style = Stroke(width = with(density) { strokeWidth.toPx() }, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(onClick = {
                path.clear()
                currentPath = emptyList()
                onAssinaturaChange(null)
            }) {
                Text("Limpar Assinatura")
            }
        }
    }
}

fun exportSignatureToBase64(
    path: List<List<Offset>>,
    strokeWidth: Dp,
    color: Color
): String? {
    if (path.isEmpty()) return null
    val width = 800
    val height = 300
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)
    val paint = Paint().apply {
        this.color = color.toArgb()
        this.strokeWidth = strokeWidth.value * 2 // Ajuste para densidade
        this.style = Paint.Style.STROKE
        this.isAntiAlias = true
        this.strokeCap = Paint.Cap.ROUND
        this.strokeJoin = Paint.Join.ROUND
    }
    path.forEach { points ->
        if (points.size > 1) {
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
            }
        }
    }
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
fun AnexosField(anexos: List<Uri>, onAnexosChange: (List<Uri>) -> Unit) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onAnexosChange(anexos + it) }
    }
    // Placeholder para áudio
    // Em produção, use MediaRecorder ou ActivityResult para áudio
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Anexar Imagem")
            }
            Button(onClick = { /* TODO: gravar áudio */ }) {
                Text("Gravar Áudio")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (anexos.isNotEmpty()) {
            Text("Arquivos anexados:", style = MaterialTheme.typography.bodyMedium)
            Column {
                anexos.forEach { uri ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Miniatura se for imagem
                        if (uri.toString().endsWith(".jpg") || uri.toString().endsWith(".png") || uri.toString().startsWith("content://")) {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Text(uri.lastPathSegment ?: uri.toString(), Modifier.weight(1f).padding(start = 8.dp))
                        IconButton(onClick = { onAnexosChange(anexos - uri) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover anexo", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
} 