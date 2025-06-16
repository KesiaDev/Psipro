package com.example.psipro.ui

import de.hdodenhof.circleimageview.CircleImageView
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.psipro.databinding.ActivityEditProfileBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import android.content.res.ColorStateList
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.CompoundButton
import com.example.psipro.R
import android.widget.CheckBox
import android.widget.EditText
import android.view.View
import android.app.AlertDialog
import android.graphics.Bitmap
import java.io.File
import androidx.core.content.ContextCompat
import android.graphics.Color
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import android.text.Editable
import android.text.TextWatcher
import android.app.TimePickerDialog
import android.view.LayoutInflater
import java.util.Locale
import android.view.Menu
import android.view.MenuItem
import com.yalantis.ucrop.UCrop
import android.util.Log
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.core.content.FileProvider
import java.net.URL
import android.widget.ImageView

data class PsicologoProfile(
    val nome: String = "",
    val titulo: String = "",
    val crp: String = "",
    val especialidades: List<String> = emptyList(),
    val sobre: String = "",
    val modalidades: List<String> = emptyList(),
    val endereco: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val redesSociais: String = "",
    val horarios: String = "",
    val idiomas: List<String> = emptyList(),
    val fotoUrl: String = ""
)

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var profileImageUri: Uri? = null
    private var fotoUrlAtual: String = ""
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val PICK_IMAGE_REQUEST = 1001
    private val UCROP_REQUEST_CODE = 69

    // Lista completa de especialidades reconhecidas pelo CFP
    private val specialtiesList = listOf(
        "Psicologia Clínica",
        "Psicologia Escolar e Educacional",
        "Psicologia Organizacional e do Trabalho",
        "Psicologia de Trânsito",
        "Psicologia Jurídica",
        "Psicologia Hospitalar",
        "Psicologia Social",
        "Psicologia do Esporte",
        "Psicologia da Saúde",
        "Psicopedagogia",
        "Neuropsicologia",
        "Psicologia Comunitária",
        "Psicologia do Desenvolvimento",
        "Psicologia do Luto e Cuidados Paliativos",
        "Outro"
    )
    private val languagesList = listOf("Português", "Inglês", "Espanhol")
    private val weekDays = listOf(
        "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado", "domingo"
    )

    // Novo launcher para o cropper
    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent
            uri?.let {
                binding.profileImageView.setImageURI(it)
                profileImageUri = it
            }
        } else {
            val error = result.error
            if (error != null) {
                Toast.makeText(this, "Erro ao recortar imagem: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa o Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "YOUR_API_KEY") // Substitua YOUR_API_KEY pela sua chave da API do Google
        }
        
        // Garante barras escuras no modo dark
        val backgroundColor = ContextCompat.getColor(this, R.color.background_black)
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preencher campos com dados das SharedPreferences, se existirem
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val nome = prefs.getString("profile_name", null)
        val crp = prefs.getString("profile_crp", null)
        val fotoPath = prefs.getString("profile_photo_path", null)
        if (!nome.isNullOrEmpty()) binding.editTextName.setText(nome)
        if (!crp.isNullOrEmpty()) binding.editTextCRP.setText(crp)
        if (!fotoPath.isNullOrEmpty()) {
            val file = File(fotoPath)
            if (file.exists()) {
                binding.profileImageView.setImageURI(android.net.Uri.fromFile(file))
                profileImageUri = android.net.Uri.fromFile(file)
            }
        }

        setupPhotoPicker()
        setupMultiSelectFields()
        setupAddressField()
        setupWhatsAppField()
        setupScheduleField()
        setupCRPField()
        carregarPerfilFirestore { profile ->
            if (profile != null) preencherCampos(profile)
        }

        binding.btnSave.setOnClickListener { salvarPerfilComFoto() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnExportCard.setOnClickListener {
            exportarCartaoDeVisitas()
        }

        // Configurar Toolbar se existir no layout
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_card -> {
                // TODO: Chamar função para gerar cartão de visitas
                exportarCartaoDeVisitas()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupPhotoPicker() {
        binding.btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Escolha uma imagem"), PICK_IMAGE_REQUEST)
        }
    }

    private fun setupMultiSelectFields() {
        // Especialidades: seleção múltipla com diálogo
        binding.editTextSpecialties.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_specialties_justify, null)
            val specialtiesContainer = dialogView.findViewById<LinearLayout>(R.id.specialtiesContainer)
            val outroLabel = dialogView.findViewById<TextView>(R.id.outroJustificativaLabel)
            val outroInputLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.outroJustificativaInputLayout)
            val outroInput = outroInputLayout.editText
            val selectedSpecialties = mutableListOf<String>()

            // Preencher com especialidades já selecionadas
            val currentSpecialties = binding.editTextSpecialties.text.toString().split(",").map { it.trim() }
            selectedSpecialties.addAll(currentSpecialties)

            // Criar checkboxes para cada especialidade
            specialtiesList.forEach { specialty ->
                val checkBox = CheckBox(this).apply {
                    text = specialty
                    isChecked = currentSpecialties.contains(specialty)
                    setTextColor(resources.getColor(R.color.bronze_gold, theme))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedSpecialties.add(specialty)
                            if (specialty == "Outro") {
                                outroLabel.visibility = View.VISIBLE
                                outroInputLayout.visibility = View.VISIBLE
                            }
                        } else {
                            selectedSpecialties.remove(specialty)
                            if (specialty == "Outro") {
                                outroLabel.visibility = View.GONE
                                outroInputLayout.visibility = View.GONE
                                outroInput?.setText("")
                            }
                        }
                    }
                }
                specialtiesContainer.addView(checkBox)
            }

            // Mostrar campos de "Outro" se já estiver selecionado
            if (currentSpecialties.contains("Outro")) {
                outroLabel.visibility = View.VISIBLE
                outroInputLayout.visibility = View.VISIBLE
                // Preencher com a justificativa existente
                val outroText = currentSpecialties.find { it != "Outro" && !specialtiesList.contains(it) }
                if (outroText != null) {
                    outroInput?.setText(outroText)
                }
            }

            AlertDialog.Builder(this)
                .setTitle("Selecione especialidades")
                .setView(dialogView)
                .setPositiveButton("Confirmar") { _, _ ->
                    var result = selectedSpecialties.filter { it != "Outro" }.joinToString(", ")
                    if (selectedSpecialties.contains("Outro")) {
                        val outroJustificativa = outroInput?.text.toString().trim()
                        if (outroJustificativa.isNotEmpty()) {
                            result = if (result.isEmpty()) outroJustificativa else "$result, $outroJustificativa"
                        }
                    }
                    binding.editTextSpecialties.setText(result)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        // Impede o dropdown padrão
        binding.editTextSpecialties.setAdapter(null)

        // --- Seleção múltipla de idiomas ---
        val selectedLanguages = mutableListOf<String>()
        binding.editTextLanguages.setOnClickListener {
            selectedLanguages.clear()
            val current = binding.editTextLanguages.text?.toString()?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            if (current.isEmpty()) {
                selectedLanguages.add("Português")
            } else {
                selectedLanguages.addAll(current)
            }
            val dialogView = layoutInflater.inflate(R.layout.dialog_languages_selection, null)
            val languagesContainer = dialogView.findViewById<LinearLayout>(R.id.languagesContainer)
            
            // Limpa o container e adiciona os switches
            languagesContainer.removeAllViews()
            languagesList.forEach { language ->
                val switchView = layoutInflater.inflate(R.layout.item_language_switch, languagesContainer, false)
                val switch = switchView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.languageSwitch)
                val textView = switchView.findViewById<TextView>(R.id.languageText)
                
                textView.text = language
                switch.isChecked = selectedLanguages.contains(language)
                
                // Aplica estilo do tema
                switch.trackTintList = ColorStateList.valueOf(getColor(R.color.bronze_gold))
                switch.thumbTintList = ColorStateList.valueOf(getColor(R.color.bronze_gold))
                
                // Adiciona padding vertical
                switchView.setPadding(0, 12, 0, 12)
                
                switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                    if (isChecked) {
                        if (!selectedLanguages.contains(language)) selectedLanguages.add(language)
                    } else {
                        selectedLanguages.remove(language)
                    }
                }
                
                languagesContainer.addView(switchView)
            }

            // Cria o diálogo com AlertDialog.Builder
            AlertDialog.Builder(this)
                .setTitle("Selecione idiomas")
                .setView(dialogView)
                .setPositiveButton("Confirmar") { _, _ ->
                    binding.editTextLanguages.setText(selectedLanguages.joinToString(", "))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        // Impede o dropdown padrão
        binding.editTextLanguages.setAdapter(null)
    }

    private fun setupAddressField() {
        binding.editTextAddress.setOnClickListener {
            // Abre o Google Maps
            val gmmIntentUri = Uri.parse("geo:0,0?q=")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
                Toast.makeText(this, "Selecione o local no Maps e copie o endereço", Toast.LENGTH_LONG).show()
            } else {
                // Se o Google Maps não estiver instalado, abre no navegador
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
                startActivity(browserIntent)
                Toast.makeText(this, "Selecione o local no Maps e copie o endereço", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupWhatsAppField() {
        binding.editTextWhatsapp.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val text = s.toString().replace(Regex("[^0-9]"), "")
                if (text.length > 11) return

                isUpdating = true
                val formatted = when {
                    text.length <= 2 -> text
                    text.length <= 7 -> "(${text.substring(0, 2)}) ${text.substring(2)}"
                    else -> "(${text.substring(0, 2)}) ${text.substring(2, 7)}-${text.substring(7)}"
                }
                binding.editTextWhatsapp.setText(formatted)
                binding.editTextWhatsapp.setSelection(formatted.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupScheduleField() {
        binding.editTextSchedule.setOnClickListener {
            showWeekScheduleDialog()
        }
    }

    private fun setupCRPField() {
        binding.editTextCRP.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val text = s.toString().replace(Regex("[^0-9]"), "")
                if (text.length > 7) return

                isUpdating = true
                val formatted = when {
                    text.length <= 2 -> text
                    else -> "${text.substring(0, 2)}/${text.substring(2)}"
                }
                binding.editTextCRP.setText(formatted)
                binding.editTextCRP.setSelection(formatted.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showWeekScheduleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_week_schedule, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.scheduleContainer)
        val horarios = mutableMapOf<String, Pair<String, String>>()
        val fechado = mutableSetOf<String>()
        val current = binding.editTextSchedule.text?.toString() ?: ""

        weekDays.forEach { dia ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_week_schedule, container, false)
            val textDay = itemView.findViewById<TextView>(R.id.textDay)
            val textHour = itemView.findViewById<TextView>(R.id.textHour)
            val checkClosed = itemView.findViewById<CheckBox>(R.id.checkClosed)
            textDay.text = dia.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            textHour.text = "08:00–20:00"
            checkClosed.isChecked = false
            container.addView(itemView)

            // Editar horário ao clicar no texto do horário
            textHour.setOnClickListener {
                if (checkClosed.isChecked) return@setOnClickListener
                val parts = textHour.text.split("–")
                val hIni = if (parts.size > 0) parts[0].trim() else "08:00"
                val hFim = if (parts.size > 1) parts[1].trim() else "20:00"
                TimePickerDialog(this, { _, hourOfDay, minute ->
                    val ini = String.format("%02d:%02d", hourOfDay, minute)
                    TimePickerDialog(this, { _, hourOfDay2, minute2 ->
                        val fim = String.format("%02d:%02d", hourOfDay2, minute2)
                        textHour.text = "$ini–$fim"
                        horarios[dia] = Pair(ini, fim)
                    }, hFim.substring(0,2).toInt(), hFim.substring(3,5).toInt(), true).show()
                }, hIni.substring(0,2).toInt(), hIni.substring(3,5).toInt(), true).show()
            }

            // Checkbox "Fechado"
            checkClosed.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    fechado.add(dia)
                    textHour.text = "Fechado"
                } else {
                    fechado.remove(dia)
                    textHour.text = horarios[dia]?.let { "${it.first}–${it.second}" } ?: "08:00–20:00"
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Horário de funcionamento")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val builder = StringBuilder()
                for (i in 0 until container.childCount) {
                    val itemView = container.getChildAt(i)
                    val textDay = itemView.findViewById<TextView>(R.id.textDay)
                    val textHour = itemView.findViewById<TextView>(R.id.textHour)
                    builder.append(textDay.text).append(" ").append(textHour.text).append("\n")
                }
                binding.editTextSchedule.setText(builder.toString().trim())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    data?.data?.let { uri ->
                        limparArquivosAntigosProfilePictures()
                        // Salva o arquivo cropped em um diretório persistente
                        val externalDir = getExternalFilesDir("ProfilePictures")
                        if (externalDir != null && externalDir.exists().not()) externalDir.mkdirs()
                        val destinationFile = File(externalDir, "profile_cropped_${System.currentTimeMillis()}.jpg")
                        val destinationUri = Uri.fromFile(destinationFile)
                        val uCrop = UCrop.of(uri, destinationUri)
                            .withAspectRatio(1f, 1f)
                            .withMaxResultSize(800, 800)
                            .withOptions(getUCropOptions())
                        uCrop.start(this)
                    }
                }
                UCROP_REQUEST_CODE -> {
                    val resultUri = UCrop.getOutput(data!!)
                    if (resultUri != null) {
                        binding.profileImageView.setImageURI(resultUri)
                        profileImageUri = resultUri
                    }
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Erro ao recortar imagem: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUCropOptions(): UCrop.Options {
        val options = UCrop.Options()
        // Cores do tema
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        options.setToolbarColor(if (isDark) ContextCompat.getColor(this, R.color.background_black) else ContextCompat.getColor(this, R.color.background_card))
        options.setStatusBarColor(if (isDark) ContextCompat.getColor(this, R.color.background_black) else ContextCompat.getColor(this, R.color.background_card))
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.bronze_gold))
        options.setToolbarWidgetColor(if (isDark) Color.WHITE else Color.BLACK)
        options.setRootViewBackgroundColor(if (isDark) ContextCompat.getColor(this, R.color.background_black) else ContextCompat.getColor(this, R.color.background_card))
        options.setHideBottomControls(false)
        options.setFreeStyleCropEnabled(false)
        options.setCircleDimmedLayer(true)
        return options
    }

    private fun preencherCampos(profile: PsicologoProfile) {
        binding.editTextName.setText(profile.nome)
        binding.editTextTitle.setText(profile.titulo)
        binding.editTextCRP.setText(profile.crp)
        binding.editTextSpecialties.setText(profile.especialidades.joinToString(", "))
        binding.editTextAbout.setText(profile.sobre)
        binding.editTextAddress.setText(profile.endereco)
        binding.editTextWhatsapp.setText(profile.whatsapp)
        binding.editTextEmail.setText(profile.email)
        binding.editTextSocial.setText(profile.redesSociais)
        binding.editTextSchedule.setText(profile.horarios)
        binding.editTextLanguages.setText(profile.idiomas.joinToString(", "))
        for (i in 0 until binding.chipGroupModalities.childCount) {
            val chip = binding.chipGroupModalities.getChildAt(i) as Chip
            chip.isChecked = profile.modalidades.contains(chip.text.toString())
        }
        fotoUrlAtual = profile.fotoUrl
        if (profile.fotoUrl.isNotEmpty()) {
            Glide.with(this).load(profile.fotoUrl).into(binding.profileImageView)
        }
    }

    private fun salvarPerfilComFoto() {
        val nome = binding.editTextName.text.toString()
        val crp = binding.editTextCRP.text.toString()

        if (nome.isEmpty() || crp.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        // Criar objeto do perfil
        val profile = montarPerfil(fotoUrlAtual)

        // Salvar no Firestore
        salvarPerfilFirestore(profile) { sucesso, erro ->
            if (sucesso) {
                // Salvar nas SharedPreferences
                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                prefs.edit().apply {
                    putString("profile_name", nome)
                    putString("profile_crp", crp)
                    profileImageUri?.let { uri ->
                        val file = File(cacheDir, "profile_photo.jpg")
                        contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        putString("profile_photo_path", file.absolutePath)
                    }
                    apply()
                }

                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao salvar perfil: $erro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun montarPerfil(fotoUrl: String): PsicologoProfile {
        val modalidades = mutableListOf<String>()
        for (i in 0 until binding.chipGroupModalities.childCount) {
            val chip = binding.chipGroupModalities.getChildAt(i) as Chip
            if (chip.isChecked) modalidades.add(chip.text.toString())
        }
        return PsicologoProfile(
            nome = binding.editTextName.text.toString().trim(),
            titulo = binding.editTextTitle.text.toString().trim(),
            crp = binding.editTextCRP.text.toString().trim(),
            especialidades = binding.editTextSpecialties.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            sobre = binding.editTextAbout.text.toString().trim(),
            modalidades = modalidades,
            endereco = binding.editTextAddress.text.toString().trim(),
            whatsapp = binding.editTextWhatsapp.text.toString().trim(),
            email = binding.editTextEmail.text.toString().trim(),
            redesSociais = binding.editTextSocial.text.toString().trim(),
            horarios = binding.editTextSchedule.text.toString().trim(),
            idiomas = binding.editTextLanguages.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            fotoUrl = fotoUrl
        )
    }

    private fun mostrarResultado(sucesso: Boolean, erro: String?) {
        if (sucesso) {
            Toast.makeText(this, "Perfil salvo!", Toast.LENGTH_SHORT).show()
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val nome = binding.editTextName.text.toString()
            val crp = binding.editTextCRP.text.toString()
            val foto = profileImageUri?.path ?: fotoUrlAtual
            if (foto != null && foto.startsWith("http")) {
                // Se for URL, baixar e salvar localmente
                CoroutineScope(Dispatchers.IO).launch {
                    val localPath = baixarImagemParaLocal(foto)
                    withContext(Dispatchers.Main) {
                        prefs.edit()
                            .putString("profile_name", nome)
                            .putString("profile_crp", crp)
                            .putString("profile_photo_path", localPath)
                            .apply()
                    }
                }
            } else {
                prefs.edit()
                    .putString("profile_name", nome)
                    .putString("profile_crp", crp)
                    .putString("profile_photo_path", foto)
                    .apply()
            }
        } else {
            Toast.makeText(this, "Erro: $erro", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun baixarImagemParaLocal(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val input: InputStream = URL(url).openStream()
                val file = File(cacheDir, "profile_downloaded_${System.currentTimeMillis()}.jpg")
                val output: OutputStream = FileOutputStream(file)
                input.copyTo(output)
                output.close()
                input.close()
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun salvarPerfilFirestore(profile: PsicologoProfile, onResult: (Boolean, String?) -> Unit) {
        Log.d("SALVAR_PERFIL", "Tentando salvar no Firestore: $profile")
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.e("SALVAR_PERFIL", "Usuário não autenticado ao salvar no Firestore!")
            onResult(false, "Usuário não autenticado")
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("psicologos").document(uid)
            .set(profile)
            .addOnSuccessListener {
                Log.d("SALVAR_PERFIL", "Perfil salvo com sucesso no Firestore!")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("SALVAR_PERFIL", "Erro ao salvar no Firestore: ${e.message}")
                onResult(false, e.message)
            }
    }

    private fun carregarPerfilFirestore(onResult: (PsicologoProfile?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("psicologos").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = doc.toObject(PsicologoProfile::class.java)
                    onResult(profile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    private fun uploadFotoPerfil(uri: Uri, userId: String, onResult: (String?) -> Unit) {
        Toast.makeText(this, "Enviando para o Firebase Storage...", Toast.LENGTH_SHORT).show()
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_photos/$userId/${UUID.randomUUID()}.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "Upload concluído! Obtendo URL...", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Toast.makeText(this, "URL obtida!", Toast.LENGTH_SHORT).show()
                    onResult(downloadUri.toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "Falha ao obter URL da foto", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha no upload da foto", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
    }

    private fun exportarCartaoDeVisitas() {
        // Inflar o layout do cartão de visitas corretamente
        val cardView = layoutInflater.inflate(R.layout.card_visit, null, false)
        
        // Preencher os dados do cartão
        val profile = getSharedPreferences("settings", MODE_PRIVATE)
        
        // Configurar a imagem do perfil
        val profileImage = cardView.findViewById<CircleImageView>(R.id.cardProfileImage)
        val photoPath = profile.getString("profile_photo_path", null)
        if (photoPath != null) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                profileImage.setImageBitmap(bitmap)
            }
        }

        // Preencher os dados do perfil
        cardView.findViewById<TextView>(R.id.cardName).text = profile.getString("profile_name", "Seu Nome")
        cardView.findViewById<TextView>(R.id.cardCrp).text = profile.getString("profile_crp", "CRP não informado")
        cardView.findViewById<TextView>(R.id.cardSpecialties).text = profile.getString("profile_specialties", "Especialidades")
        cardView.findViewById<TextView>(R.id.cardAbout).text = profile.getString("profile_about", "Sobre o psicólogo")
        val modalidades = profile.getString("profile_modalities", "Modalidades")
        val horarios = profile.getString("profile_schedule", "Horários")
        cardView.findViewById<TextView>(R.id.cardModalities).text = "$modalidades - $horarios"
        cardView.findViewById<TextView>(R.id.cardWhatsapp).text = profile.getString("profile_whatsapp", "(00)")
        cardView.findViewById<TextView>(R.id.cardEmail).text = profile.getString("profile_email", "email@exemplo.com")
        cardView.findViewById<TextView>(R.id.cardAddress).text = profile.getString("profile_address", "Endereço")
        cardView.findViewById<TextView>(R.id.cardLanguages).text = profile.getString("profile_languages", "Português")
        cardView.findViewById<TextView>(R.id.cardSocial).text = profile.getString("profile_social", "Redes sociais")
        cardView.findViewById<TextView>(R.id.cardDescription).text = profile.getString("profile_description", "Descrição do atendimento")

        // Medir e layout do card
        cardView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        cardView.layout(0, 0, cardView.measuredWidth, cardView.measuredHeight)

        // Criar bitmap do card
        val bitmap = Bitmap.createBitmap(
            cardView.measuredWidth,
            cardView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        cardView.draw(canvas)

        // Exibir prévia antes de compartilhar
        mostrarDialogPreviewCartao(bitmap)
    }

    private fun mostrarDialogPreviewCartao(bitmap: Bitmap) {
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        val dialog = AlertDialog.Builder(this)
            .setTitle("Prévia do Cartão de Visitas")
            .setView(imageView)
            .setPositiveButton("Compartilhar") { _, _ ->
                compartilharCartaoDeVisitas(bitmap)
            }
            .setNegativeButton("Fechar", null)
            .create()
        dialog.show()
    }

    private fun compartilharCartaoDeVisitas(bitmap: Bitmap) {
        // Salvar a imagem
        val filename = "cartao_visitas_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(null), filename)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            // Compartilhar a imagem
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Compartilhar cartão de visitas"))
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar cartão de visitas: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Limpa arquivos antigos do diretório de fotos de perfil
    private fun limparArquivosAntigosProfilePictures() {
        val dir = getExternalFilesDir("ProfilePictures")
        dir?.listFiles()?.forEach { file ->
            // Remove arquivos com nome "profile_cropped_" e mais de 1 hora de idade
            if (file.name.startsWith("profile_cropped_") &&
                System.currentTimeMillis() - file.lastModified() > 60 * 60 * 1000) {
                file.delete()
            }
        }
    }
} 
