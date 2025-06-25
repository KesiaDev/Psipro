package com.example.psipro

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psipro.adapters.AttachmentAdapter
import com.example.psipro.adapters.AttachmentItem
import com.example.psipro.adapters.AttachmentType
import com.example.psipro.data.entities.PatientNote
import com.example.psipro.databinding.ActivityNoteEditBinding
import com.example.psipro.ui.viewmodels.PatientNoteViewModel
import com.example.psipro.utils.AttachmentManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Date
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

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
            checkImagePermissionAndPick()
        }
        
        binding.btnAddAudio.setOnClickListener {
            openAudioPicker()
        }
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
} 