package com.example.psipro.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.psipro.R
import com.example.psipro.DashboardActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditProfileDialog : DialogFragment() {
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null
    private var imageProfile: CircleImageView? = null
    private var editName: EditText? = null

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                imageProfile?.setImageBitmap(bitmap)
                selectedImageUri = Uri.fromFile(File(path))
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageProfile?.setImageBitmap(bitmap)
                selectedImageUri = uri
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null)
        imageProfile = view.findViewById(R.id.imageProfile)
        editName = view.findViewById(R.id.editName)
        val btnChangePhoto = view.findViewById<Button>(R.id.btnChangePhoto)
        val btnSaveProfile = view.findViewById<Button>(R.id.btnSaveProfile)

        // Carregar dados atuais
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentName = prefs.getString("profile_name", "")
        val photoPath = prefs.getString("profile_photo_path", null)

        editName?.setText(currentName)
        if (photoPath != null) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageProfile?.setImageBitmap(bitmap)
                selectedImageUri = Uri.fromFile(file)
            }
        }

        btnChangePhoto.setOnClickListener {
            showImageSourceDialog()
        }

        btnSaveProfile.setOnClickListener {
            val name = editName?.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(context, "Por favor, insira um nome", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Salvar nome
            prefs.edit().putString("profile_name", name).apply()

            // Salvar foto se houver uma nova
            selectedImageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val photoFile = createImageFile()
                    FileOutputStream(photoFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    prefs.edit().putString("profile_photo_path", photoFile.absolutePath).apply()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao salvar foto", Toast.LENGTH_SHORT).show()
                }
            }

            // Atualizar drawer header
            (activity as? DashboardActivity)?.updateDrawerHeader()
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Editar Perfil")
            .create()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Câmera", "Galeria")
        AlertDialog.Builder(requireContext())
            .setTitle("Escolher fonte da imagem")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> pickImage()
                }
            }
            .show()
    }

    private fun takePicture() {
        try {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            currentPhotoPath = photoFile.absolutePath
            takePictureLauncher.launch(photoURI)
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao acessar câmera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir("ProfilePictures")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
} 