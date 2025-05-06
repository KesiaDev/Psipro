package com.example.apppisc

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apppisc.data.entities.PatientNote
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.databinding.ActivityDetalhesPacienteBinding
import com.example.apppisc.ui.viewmodels.PatientViewModel
import com.example.apppisc.ui.viewmodels.PatientNoteViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.snackbar.Snackbar
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import com.example.apppisc.adapters.PatientMessageAdapter
import com.example.apppisc.data.entities.PatientMessage
import com.example.apppisc.ui.viewmodels.PatientMessageViewModel
import java.net.URLEncoder
import kotlinx.coroutines.flow.first
import com.google.android.material.button.MaterialButton
import com.example.apppisc.utils.MessageTemplateManager

@AndroidEntryPoint
class DetalhePacienteActivity : com.example.apppisc.ui.SecureActivity() {
    private lateinit var binding: ActivityDetalhesPacienteBinding
    private val patientViewModel: PatientViewModel by viewModels()
    private val noteViewModel: PatientNoteViewModel by viewModels()
    private val messageViewModel: PatientMessageViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    
    private lateinit var nomeEditText: TextInputEditText
    private lateinit var cpfEditText: TextInputEditText
    private lateinit var dataNascimentoEditText: TextInputEditText
    private lateinit var telefoneEditText: TextInputEditText
    private lateinit var anotacaoEditText: TextInputEditText
    
    private var currentPatientId: Long = -1
    private var fotoUri: Uri? = null
    private lateinit var fotoImageView: ImageView
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            fotoUri = it
            fotoImageView.setImageURI(it)
        }
    }
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            fotoImageView.setImageBitmap(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalhesPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener do botão de voltar
        binding.botaoVoltar.setOnClickListener {
            finish()
        }

        // Initialize views
        nomeEditText = binding.nomePacienteText
        cpfEditText = binding.cpfPacienteText
        dataNascimentoEditText = binding.dataNascimentoText
        telefoneEditText = binding.telefoneText
        anotacaoEditText = binding.anotacaoEditText
        fotoImageView = findViewById(R.id.fotoPacienteImageView)

        // Get patient ID from intent
        currentPatientId = intent.getLongExtra("PATIENT_ID", -1)
        if (currentPatientId == -1L) {
            Toast.makeText(this, getString(R.string.error_loading_patient), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        binding.anotacoesRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = AnotacoesAdapter()
        binding.anotacoesRecyclerView.adapter = adapter

        // Setup RecyclerView de mensagens
        val messageAdapter = PatientMessageAdapter()
        binding.mensagensRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.mensagensRecyclerView.adapter = messageAdapter

        // Carregar dados do paciente
        lifecycleScope.launch {
            patientViewModel.loadPatient(currentPatientId)
            patientViewModel.currentPatient.collect { patient ->
                patient?.let { displayPatientData(it) }
            }
        }

        // Observar anotações
        lifecycleScope.launch {
            noteViewModel.getNotesForPatient(currentPatientId).collect { anotacoes ->
                adapter.submitList(anotacoes)
            }
        }

        // Observar mensagens
        lifecycleScope.launch {
            messageViewModel.getMessagesForPatient(currentPatientId).collect { mensagens ->
                messageAdapter.submitList(mensagens)
            }
        }

        // Setup save annotation button
        binding.salvarAnotacaoButton.setOnClickListener {
            val anotacaoText = anotacaoEditText.text.toString().trim()
            if (anotacaoText.isNotEmpty()) {
                val anotacao = PatientNote(
                    patientId = currentPatientId,
                    texto = anotacaoText,
                    data = Date()
                )
                noteViewModel.insertNote(
                    note = anotacao,
                    onSuccess = { _ ->
                        anotacaoEditText.text?.clear()
                        Toast.makeText(this@DetalhePacienteActivity, getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
                    },
                    onError = { e ->
                        Toast.makeText(this@DetalhePacienteActivity, getString(R.string.error_saving_note, e.message), Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, getString(R.string.enter_note), Toast.LENGTH_SHORT).show()
            }
        }

        // Botões de editar e excluir
        binding.editarPacienteButton.setOnClickListener {
            habilitarEdicaoCampos(true)
            binding.editarPacienteButton.visibility = View.GONE
            binding.excluirPacienteButton.visibility = View.GONE
            binding.salvarPacienteButton.visibility = View.VISIBLE
            binding.cancelarEdicaoButton.visibility = View.VISIBLE
            Snackbar.make(binding.root, "Modo de edição ativado.", Snackbar.LENGTH_SHORT).show()
        }
        binding.excluirPacienteButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir Paciente")
                .setMessage("Tem certeza que deseja excluir este paciente?")
                .setPositiveButton("Excluir") { _, _ ->
                    patientViewModel.currentPatient.value?.let { paciente ->
                        patientViewModel.deletePatient(paciente)
                        Snackbar.make(binding.root, "Paciente excluído com sucesso.", Snackbar.LENGTH_LONG).show()
                        finish()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        binding.salvarPacienteButton.setOnClickListener {
            val pacienteEditado = obterPacienteEditado()
            if (!validarCamposEdicao()) return@setOnClickListener
            lifecycleScope.launch {
                val pacienteComMesmoCpf = patientViewModel.getPatientByCpf(pacienteEditado.cpf)
                if (pacienteComMesmoCpf != null && pacienteComMesmoCpf.id != pacienteEditado.id) {
                    Snackbar.make(binding.root, "Já existe outro paciente com este CPF.", Snackbar.LENGTH_LONG).show()
                    return@launch
                }
                try {
                    patientViewModel.savePatient(pacienteEditado)
                    Snackbar.make(binding.root, "Paciente atualizado com sucesso.", Snackbar.LENGTH_SHORT).show()
                    habilitarEdicaoCampos(false)
                    binding.editarPacienteButton.visibility = View.VISIBLE
                    binding.excluirPacienteButton.visibility = View.VISIBLE
                    binding.salvarPacienteButton.visibility = View.GONE
                    binding.cancelarEdicaoButton.visibility = View.GONE
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Erro ao atualizar paciente: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        binding.cancelarEdicaoButton.setOnClickListener {
            patientViewModel.currentPatient.value?.let { displayPatientData(it) }
            habilitarEdicaoCampos(false)
            binding.editarPacienteButton.visibility = View.VISIBLE
            binding.excluirPacienteButton.visibility = View.VISIBLE
            binding.salvarPacienteButton.visibility = View.GONE
            binding.cancelarEdicaoButton.visibility = View.GONE
            Snackbar.make(binding.root, "Edição cancelada.", Snackbar.LENGTH_SHORT).show()
        }

        binding.compartilharPacienteButton.setOnClickListener {
            val paciente = patientViewModel.currentPatient.value
            if (paciente != null) {
                // Diálogo de escolha
                val formatos = arrayOf("CSV", "PDF")
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Exportar dados do paciente")
                    .setItems(formatos) { _, which ->
                        when (which) {
                            0 -> exportarPacienteCsv(paciente)
                            1 -> exportarPacientePdf(paciente)
                        }
                    }
                    .show()
            } else {
                Snackbar.make(binding.root, "Não foi possível obter os dados do paciente.", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.anexarFotoButton.setOnClickListener {
            val options = arrayOf("Escolher da Galeria", "Tirar Foto")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Anexar Foto")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> pickImageLauncher.launch("image/*")
                        1 -> takePictureLauncher.launch(null)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Botão de seleção de template
        binding.selecionarTemplateButton.setOnClickListener {
            val context = this
            val templates = MessageTemplateManager.getTemplates(context)
            val patient = patientViewModel.currentPatient.value
            val nome = patient?.name ?: ""
            val data = dateFormat.format(patient?.birthDate ?: Date())
            val hora = "" // Preencher se houver informação de horário
            val variables = mapOf(
                "nome" to nome,
                "data" to data,
                "hora" to hora
            )
            val items = templates.map { MessageTemplateManager.fillTemplate(it, variables) }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Selecionar modelo de mensagem")
                .setItems(items) { _, which ->
                    binding.mensagemWhatsappEditText.setText(items[which])
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Botão WhatsApp
        binding.enviarWhatsappButton.setOnClickListener {
            val mensagem = binding.mensagemWhatsappEditText.text.toString().trim()
            val telefone = telefoneEditText.text.toString().replace("[^\\d]".toRegex(), "")
            if (mensagem.isNotEmpty() && telefone.isNotEmpty()) {
                val msg = PatientMessage(
                    patientId = currentPatientId,
                    texto = mensagem
                )
                messageViewModel.insertMessage(
                    message = msg,
                    onSuccess = { _ ->
                        binding.mensagemWhatsappEditText.text?.clear()
                        // Abrir WhatsApp
                        val url = "https://wa.me/55$telefone?text=" + URLEncoder.encode(mensagem, "UTF-8")
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = android.net.Uri.parse(url)
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "WhatsApp não encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { e ->
                        Toast.makeText(this, "Erro ao salvar mensagem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "Digite a mensagem e verifique o telefone.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão de acesso aos laudos
        binding.btnLaudos.setOnClickListener {
            val intent = Intent(this, com.example.apppisc.ui.PatientReportsActivity::class.java)
            intent.putExtra("patientId", currentPatientId)
            startActivity(intent)
        }
    }

    private fun displayPatientData(patient: Patient) {
        nomeEditText.setText(patient.name)
        cpfEditText.setText(patient.cpf)
        dataNascimentoEditText.setText(dateFormat.format(patient.birthDate))
        telefoneEditText.setText(patient.phone)
    }

    private fun habilitarEdicaoCampos(habilitar: Boolean) {
        binding.nomePacienteText.isEnabled = habilitar
        binding.cpfPacienteText.isEnabled = false // CPF não editável
        binding.dataNascimentoText.isEnabled = habilitar
        binding.telefoneText.isEnabled = habilitar
    }

    private fun obterPacienteEditado(): Patient {
        val paciente = patientViewModel.currentPatient.value!!
        return paciente.copy(
            name = binding.nomePacienteText.text.toString(),
            birthDate = dateFormat.parse(binding.dataNascimentoText.text.toString()) ?: paciente.birthDate,
            phone = binding.telefoneText.text.toString()
        )
    }

    private fun validarCamposEdicao(): Boolean {
        val nome = binding.nomePacienteText.text?.toString()?.trim() ?: ""
        val cpf = binding.cpfPacienteText.text?.toString()?.trim() ?: ""
        val dataNascimento = binding.dataNascimentoText.text?.toString()?.trim() ?: ""
        val telefone = binding.telefoneText.text?.toString()?.trim() ?: ""
        if (nome.isEmpty()) {
            Snackbar.make(binding.root, "Preencha o nome.", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (cpf.isEmpty() || cpf.length < 14) {
            Snackbar.make(binding.root, "Preencha o CPF.", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (dataNascimento.isEmpty() || dataNascimento.length < 10) {
            Snackbar.make(binding.root, "Preencha a data de nascimento.", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (telefone.isEmpty() || telefone.length < 14) {
            Snackbar.make(binding.root, "Preencha o telefone.", Snackbar.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun exportarPacienteCsv(paciente: Patient) {
        lifecycleScope.launch {
            val anotacoes = noteViewModel.getNotesForPatient(currentPatientId).first()
            val mensagens = messageViewModel.getMessagesForPatient(currentPatientId).first()
            val sb = StringBuilder()
            sb.appendLine("Nome,CPF,Nascimento,Telefone,Histórico,Medicações,Alergias")
            sb.appendLine("\"${paciente.name}\",\"${paciente.cpf}\",\"${dateFormat.format(paciente.birthDate)}\",\"${paciente.phone}\",\"${paciente.clinicalHistory.orEmpty()}\",\"${paciente.medications.orEmpty()}\",\"${paciente.allergies.orEmpty()}\")")
            sb.appendLine()
            sb.appendLine("Anotações:")
            sb.appendLine("Data,Texto")
            for (a in anotacoes) {
                sb.appendLine("\"${dateFormat.format(a.data)}\",\"${a.texto.replace("\"", "'")}\"")
            }
            sb.appendLine()
            sb.appendLine("Mensagens:")
            sb.appendLine("Data,Texto")
            for (m in mensagens) {
                sb.appendLine("\"${dateFormat.format(m.data)}\",\"${m.texto.replace("\"", "'")}\"")
            }
            val fileName = "paciente_${paciente.cpf}_${System.currentTimeMillis()}.csv"
            val file = java.io.File(cacheDir, fileName)
            file.writeText(sb.toString())
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this@DetalhePacienteActivity,
                "${packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar dados do paciente (CSV)"))
        }
    }

    private fun exportarPacientePdf(paciente: Patient) {
        lifecycleScope.launch {
            val anotacoes = noteViewModel.getNotesForPatient(currentPatientId).first()
            val mensagens = messageViewModel.getMessagesForPatient(currentPatientId).first()
            val fileName = "paciente_${paciente.cpf}_${System.currentTimeMillis()}.pdf"
            val file = java.io.File(cacheDir, fileName)
            val pdf = android.graphics.pdf.PdfDocument()
            val paint = android.graphics.Paint()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas
            var y = 40
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Dados do Paciente", 40f, y.toFloat(), paint)
            y += 30
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Nome: ${paciente.name}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("CPF: ${paciente.cpf}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("Nascimento: ${dateFormat.format(paciente.birthDate)}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("Telefone: ${paciente.phone}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("Histórico: ${paciente.clinicalHistory.orEmpty()}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("Medicações: ${paciente.medications.orEmpty()}", 40f, y.toFloat(), paint); y += 18
            canvas.drawText("Alergias: ${paciente.allergies.orEmpty()}", 40f, y.toFloat(), paint); y += 24
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Anotações:", 40f, y.toFloat(), paint); y += 20
            paint.textSize = 12f
            paint.isFakeBoldText = false
            for (a in anotacoes) {
                canvas.drawText("- ${dateFormat.format(a.data)}: ${a.texto}", 50f, y.toFloat(), paint); y += 16
                if (y > 800) break
            }
            y += 10
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("Mensagens:", 40f, y.toFloat(), paint); y += 20
            paint.textSize = 12f
            paint.isFakeBoldText = false
            for (m in mensagens) {
                canvas.drawText("- ${dateFormat.format(m.data)}: ${m.texto}", 50f, y.toFloat(), paint); y += 16
                if (y > 800) break
            }
            pdf.finishPage(page)
            file.outputStream().use { pdf.writeTo(it) }
            pdf.close()
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this@DetalhePacienteActivity,
                "${packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar dados do paciente (PDF)"))
        }
    }
} 