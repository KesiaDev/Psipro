package com.psipro.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import android.provider.OpenableColumns
import androidx.recyclerview.widget.LinearLayoutManager
import com.psipro.app.adapters.AttachmentAdapter
import com.psipro.app.adapters.AttachmentItem
import com.psipro.app.adapters.AttachmentType
import com.psipro.app.data.entities.PatientNote
import com.psipro.app.databinding.ActivityNoteEditBinding
import com.psipro.app.ui.viewmodels.PatientNoteViewModel
import com.psipro.app.utils.AttachmentManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import android.app.AlertDialog
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

@AndroidEntryPoint
class NoteEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var viewModel: PatientNoteViewModel
    private lateinit var attachmentManager: AttachmentManager
    private lateinit var attachmentAdapter: AttachmentAdapter
    
    private var patientId: Long = 0
    private var noteId: Long = 0
    private var isEditing = false
    private var isFavorite = false
    
    private var currentImageAttachments = ""
    private var currentAudioAttachments = ""
    
    // Activity result launchers
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                addImageAttachment(uri)
            }
        }
    }
    
    private val audioPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                addAudioAttachment(uri)
            }
        }
    }
    
    private val transcribeAudioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                transcribeAudioFromUri(uri)
            }
        }
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permissão necessária para acessar imagens", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter dados da intent
        patientId = intent.getLongExtra("patient_id", 0)
        noteId = intent.getLongExtra("note_id", 0)
        isEditing = noteId != 0L
        
        if (patientId == 0L) {
            finish()
            return
        }

        // Inicializar ViewModel e AttachmentManager
        viewModel = ViewModelProvider(this)[PatientNoteViewModel::class.java]
        attachmentManager = AttachmentManager(this)
        
        setupUI()
        setupClickListeners()
        
        if (isEditing) {
            loadNote()
        }
    }
    
    private fun setupUI() {
        binding.toolbar.title = if (isEditing) "Editar Anotação" else "Nova Anotação"
        
        // Configurar RecyclerView de anexos
        attachmentAdapter = AttachmentAdapter(
            onRemoveClick = { attachmentItem ->
                removeAttachment(attachmentItem)
            },
            onItemClick = { attachmentItem ->
                openAttachment(attachmentItem)
            }
        )
        
        binding.recyclerAttachments.apply {
            layoutManager = LinearLayoutManager(this@NoteEditActivity)
            adapter = attachmentAdapter
        }
        
        // Configurar layout de botões baseado no tamanho da tela
        setupButtonLayout()
    }
    
    private fun setupButtonLayout() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        
        // Se a tela for menor que 360dp, usar layout compacto
        if (screenWidth < 360) {
            binding.btnAddImage.visibility = View.GONE
            binding.btnAddAudio.visibility = View.GONE
            binding.btnTranscribeAudio.visibility = View.GONE
            
            binding.compactButtonsLayout.visibility = View.VISIBLE
            
            // Configurar click listeners para botões compactos
            binding.btnAddImageCompact.setOnClickListener {
                animateButtonPress(binding.btnAddImageCompact) {
                    checkImagePermissionAndPick()
                }
            }
            
            binding.btnAddAudioCompact.setOnClickListener {
                animateButtonPress(binding.btnAddAudioCompact) {
                    openAudioPicker()
                }
            }
            
            binding.btnTranscribeAudioCompact.setOnClickListener {
                animateButtonPress(binding.btnTranscribeAudioCompact) {
                    showTranscribeChoiceDialog()
                }
            }
        } else {
            // Usar layout normal
            binding.btnAddImage.visibility = View.VISIBLE
            binding.btnAddAudio.visibility = View.VISIBLE
            binding.btnTranscribeAudio.visibility = View.VISIBLE
            
            binding.compactButtonsLayout.visibility = View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
        
        binding.btnSave.setOnClickListener {
            saveNote()
        }
        
        binding.btnAddImage.setOnClickListener {
            animateButtonPress(binding.btnAddImage) {
                checkImagePermissionAndPick()
            }
        }
        
        binding.btnAddAudio.setOnClickListener {
            animateButtonPress(binding.btnAddAudio) {
                openAudioPicker()
            }
        }
        
        binding.btnTranscribeAudio.setOnClickListener {
            animateButtonPress(binding.btnTranscribeAudio) {
                showTranscribeChoiceDialog()
            }
        }
    }
    
    private fun animateButtonPress(button: View, action: () -> Unit) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.button_press)
        button.startAnimation(animation)
        
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                action()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        binding.btnFavorite.alpha = if (isFavorite) 1.0f else 0.3f
    }
    
    private fun checkImagePermissionAndPick() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        audioPickerLauncher.launch(intent)
    }
    
    private fun addImageAttachment(uri: Uri) {
        val imagePath = attachmentManager.saveImage(uri, patientId, noteId)
        if (imagePath != null) {
            currentImageAttachments = attachmentManager.addImageAttachment(currentImageAttachments, imagePath)
            updateAttachmentsUI()
            Toast.makeText(this, "Imagem anexada com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Erro ao anexar imagem", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addAudioAttachment(uri: Uri) {
        val audioPath = attachmentManager.saveAudio(uri, patientId, noteId)
        if (audioPath != null) {
            currentAudioAttachments = attachmentManager.addAudioAttachment(currentAudioAttachments, audioPath)
            updateAttachmentsUI()
            Toast.makeText(this, "Áudio anexado com sucesso!", Toast.LENGTH_SHORT).show()

            // Upload automático para Firebase Storage
            val dataSessao = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(Date())
            uploadAudioToFirebase(
                localPath = audioPath,
                pacienteId = patientId,
                dataSessao = dataSessao,
                onSuccess = { url ->
                    Toast.makeText(this, "Áudio enviado para o Firebase!", Toast.LENGTH_SHORT).show()
                },
                onError = { e ->
                    Toast.makeText(this, "Erro ao enviar áudio: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(this, "Erro ao anexar áudio", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removeAttachment(attachmentItem: AttachmentItem) {
        when (attachmentItem.type) {
            AttachmentType.IMAGE -> {
                currentImageAttachments = attachmentManager.removeImageAttachment(currentImageAttachments, attachmentItem.path)
            }
            AttachmentType.AUDIO -> {
                currentAudioAttachments = attachmentManager.removeAudioAttachment(currentAudioAttachments, attachmentItem.path)
            }
        }
        updateAttachmentsUI()
        Toast.makeText(this, "Anexo removido", Toast.LENGTH_SHORT).show()
    }
    
    private fun openAttachment(attachmentItem: AttachmentItem) {
        val file = File(attachmentItem.path)
        if (file.exists()) {
            when (attachmentItem.type) {
                AttachmentType.IMAGE -> {
                    // Abrir imagem em visualizador
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.fromFile(file), "image/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Erro ao abrir imagem", Toast.LENGTH_SHORT).show()
                    }
                }
                AttachmentType.AUDIO -> {
                    // Abrir áudio em player
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.fromFile(file), "audio/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Erro ao abrir áudio", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateAttachmentsUI() {
        val images = attachmentManager.getImageAttachments(currentImageAttachments)
        val audios = attachmentManager.getAudioAttachments(currentAudioAttachments)
        
        val attachmentItems = mutableListOf<AttachmentItem>()
        
        // Adicionar imagens
        images.forEach { path ->
            val file = File(path)
            attachmentItems.add(
                AttachmentItem(
                    path = path,
                    type = AttachmentType.IMAGE,
                    fileName = file.name
                )
            )
        }
        
        // Adicionar áudios
        audios.forEach { path ->
            val file = File(path)
            attachmentItems.add(
                AttachmentItem(
                    path = path,
                    type = AttachmentType.AUDIO,
                    fileName = file.name
                )
            )
        }
        
        attachmentAdapter.submitList(attachmentItems)
    }
    
    private fun loadNote() {
        viewModel.getNoteById(noteId) { note ->
            if (note != null) {
                binding.editTextNote.setText(note.content)
                isFavorite = note.isFavorite
                binding.btnFavorite.alpha = if (isFavorite) 1.0f else 0.3f
                currentImageAttachments = note.imageAttachments
                currentAudioAttachments = note.audioAttachments
                updateAttachmentsUI()
            } else {
                Toast.makeText(this, "Erro ao carregar anotação", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun saveNote() {
        val content = binding.editTextNote.text.toString().trim()
        
        if (content.isEmpty()) {
            Toast.makeText(this, "Digite o conteúdo da anotação", Toast.LENGTH_SHORT).show()
            return
        }
        
        val note = PatientNote(
            id = if (isEditing) noteId else 0,
            patientId = patientId,
            content = content,
            createdAt = if (isEditing) Date() else Date(),
            updatedAt = Date(),
            isFavorite = isFavorite,
            imageAttachments = currentImageAttachments,
            audioAttachments = currentAudioAttachments
        )
        
        if (isEditing) {
            viewModel.updateNote(
                note = note,
                onSuccess = {
                    Toast.makeText(this, "Anotação atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { exception ->
                    Toast.makeText(this, "Erro ao atualizar: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            viewModel.insertNote(
                note = note,
                onSuccess = { id ->
                    noteId = id
                    Toast.makeText(this, "Anotação salva com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { exception ->
                    Toast.makeText(this, "Erro ao salvar: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun uploadAudioToFirebase(
        localPath: String,
        pacienteId: Long,
        dataSessao: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val file = java.io.File(localPath)
        val uri = Uri.fromFile(file)
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "${pacienteId}_${dataSessao}_${UUID.randomUUID()}.m4a"
        val audioRef = storageRef.child("gravacoes/$fileName")

        val uploadTask = audioRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            audioRef.downloadUrl.addOnSuccessListener { downloadUri ->
                // Salvar metadados no Firestore
                val firestore = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "pacienteId" to pacienteId,
                    "dataSessao" to dataSessao,
                    "urlAudio" to downloadUri.toString(),
                    "status" to "aguardando_transcricao"
                )
                firestore.collection("transcricoes")
                    .add(data)
                    .addOnSuccessListener { onSuccess(downloadUri.toString()) }
                    .addOnFailureListener { onError(it) }
            }
        }.addOnFailureListener {
            onError(it)
        }
    }

    private fun transcribeAudioFromUri(uri: Uri) {
        lifecycleScope.launch {
            transcribeAudioFromUriInternal(uri)
        }
    }

    private suspend fun transcribeAudioFromUriInternal(uri: Uri) {
        withContext(Dispatchers.Main) { binding.editTextNote.setText("Transcrevendo áudio...") }
        try {
            val tempFile = withContext(Dispatchers.IO) {
                val stream = contentResolver.openInputStream(uri) ?: return@withContext null
                val ext = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx >= 0) {
                        val name = cursor.getString(nameIdx) ?: ""
                        when {
                            name.endsWith(".m4a", true) -> ".m4a"
                            name.endsWith(".mp3", true) -> ".mp3"
                            name.endsWith(".wav", true) -> ".wav"
                            name.endsWith(".webm", true) -> ".webm"
                            name.endsWith(".ogg", true) -> ".ogg"
                            name.endsWith(".flac", true) -> ".flac"
                            name.endsWith(".mp4", true) -> ".mp4"
                            else -> ".m4a"
                        }
                    } else ".m4a"
                } ?: ".m4a"
                val f = File(cacheDir, "audio_transcribe_${System.currentTimeMillis()}$ext")
                stream.use { input ->
                    FileOutputStream(f).use { output ->
                        input.copyTo(output)
                    }
                }
                f
            }
            if (tempFile == null) {
                withContext(Dispatchers.Main) { Toast.makeText(this@NoteEditActivity, "Não foi possível abrir o arquivo", Toast.LENGTH_LONG).show() }
                return
            }
            transcribeViaBackend(tempFile)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTranscribeChoiceDialog() {
        val options = arrayOf("Transcrever áudio anexado", "Selecionar novo arquivo")
        AlertDialog.Builder(this)
            .setTitle("Transcrever áudio")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showAnexosAudioDialog()
                    1 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                        transcribeAudioLauncher.launch(intent)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAnexosAudioDialog() {
        val audios = attachmentManager.getAudioAttachments(currentAudioAttachments)
        if (audios.isEmpty()) {
            Toast.makeText(this, "Nenhum áudio anexado.", Toast.LENGTH_SHORT).show()
            return
        }
        val fileNames = audios.map { File(it).name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Escolha o áudio para transcrever")
            .setItems(fileNames) { dialog, which ->
                val audioPath = audios[which]
                transcribeAudioFromFile(audioPath)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun transcribeAudioFromFile(path: String) {
        lifecycleScope.launch {
            binding.editTextNote.setText("Transcrevendo áudio...")
            val file = File(path)
            if (file.exists()) {
                transcribeViaBackend(file)
            } else {
                runOnUiThread { Toast.makeText(this@NoteEditActivity, "Arquivo não encontrado", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private suspend fun transcribeViaBackend(file: File) {
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SyncEntryPoint::class.java)
        if (!entryPoint.backendAuthManager().isBackendAuthenticated()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditActivity, "Faça login no backend para usar transcrição de voz.", Toast.LENGTH_LONG).show()
            }
            return
        }
        try {
            val ext = when {
                file.name.endsWith(".m4a", ignoreCase = true) -> "audio/mp4"
                file.name.endsWith(".mp3", ignoreCase = true) -> "audio/mpeg"
                file.name.endsWith(".wav", ignoreCase = true) -> "audio/wav"
                else -> "audio/mp4"
            }
            val requestFile = file.asRequestBody(ext.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val resp = withContext(Dispatchers.IO) {
                entryPoint.backendApiService().transcribe(part)
            }
            withContext(Dispatchers.Main) {
                if (resp.isSuccessful) {
                    val transcript = resp.body()?.transcript?.trim() ?: ""
                    val current = binding.editTextNote.text.toString()
                    binding.editTextNote.setText(if (current.isBlank()) transcript else "$current\n\n$transcript")
                    Toast.makeText(this@NoteEditActivity, "Transcrição concluída!", Toast.LENGTH_SHORT).show()
                } else {
                    val msg = resp.errorBody()?.string() ?: "Erro ${resp.code()}"
                    Toast.makeText(this@NoteEditActivity, "Transcrição falhou: $msg", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: retrofit2.HttpException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditActivity, when (e.code()) {
                    401 -> "Faça login no backend para usar transcrição."
                    else -> "Erro ${e.code()}: ${e.message()}"
                }, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@NoteEditActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            try { if (file.absolutePath.contains("audio_transcribe")) file.delete() } catch (_: Exception) {}
        }
    }
} 



