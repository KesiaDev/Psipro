package com.example.apppisc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.utils.MaskUtils.addCpfMask
import com.example.apppisc.utils.MaskUtils.addPhoneMask
import com.example.apppisc.utils.MaskUtils.addDateMask
import com.example.apppisc.ui.viewmodels.PatientViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class CadastroPacienteActivity : AppCompatActivity() {
    private lateinit var viewModel: PatientViewModel
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_paciente)

        viewModel = ViewModelProvider(this)[PatientViewModel::class.java]

        val nomeEditText = findViewById<TextInputEditText>(R.id.nomeEditText)
        val cpfEditText = findViewById<TextInputEditText>(R.id.cpfEditText)
        val dataNascimentoEditText = findViewById<TextInputEditText>(R.id.dataNascimentoEditText)
        val telefoneEditText = findViewById<TextInputEditText>(R.id.telefoneEditText)
        val historicoEditText = findViewById<TextInputEditText>(R.id.historicoEditText)
        val medicamentosEditText = findViewById<TextInputEditText>(R.id.medicamentosEditText)
        val alergiasEditText = findViewById<TextInputEditText>(R.id.alergiasEditText)
        val salvarButton = findViewById<MaterialButton>(R.id.salvarButton)
        val voltarButton = findViewById<MaterialButton>(R.id.voltarButton)

        // Adicionar máscaras aos campos
        cpfEditText.addCpfMask()
        telefoneEditText.addPhoneMask()
        dataNascimentoEditText.addDateMask()

        // Adapters para campos de seleção
        val grupoAutoComplete = findViewById<android.widget.AutoCompleteTextView>(R.id.grupoAutoComplete)
        val grupos = listOf("Adolescente", "Adultos", "Casal", "Crianças", "Famílias", "Idosos")
        val grupoAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, grupos)
        grupoAutoComplete.setAdapter(grupoAdapter)

        val generoAutoComplete = findViewById<android.widget.AutoCompleteTextView>(R.id.generoAutoComplete)
        val generos = listOf("Feminino", "Masculino", "Não-binário", "Outro", "Prefere não informar")
        val generoAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, generos)
        generoAutoComplete.setAdapter(generoAdapter)

        val escolaridadeAutoComplete = findViewById<android.widget.AutoCompleteTextView>(R.id.escolaridadeAutoComplete)
        val escolaridades = listOf(
            "Analfabeto", "Ensino Fundamental Incompleto", "Ensino Fundamental Completo",
            "Ensino Médio Incompleto", "Ensino Médio Completo",
            "Superior Incompleto", "Superior Completo", "Pós-graduação", "Mestrado", "Doutorado"
        )
        val escolaridadeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, escolaridades)
        escolaridadeAutoComplete.setAdapter(escolaridadeAdapter)

        val estadoCivilAutoComplete = findViewById<android.widget.AutoCompleteTextView>(R.id.estadoCivilAutoComplete)
        val estadosCivis = listOf("Solteiro(a)", "Casado(a)", "Divorciado(a)", "Viúvo(a)", "União Estável", "Separado(a)")
        val estadoCivilAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, estadosCivis)
        estadoCivilAutoComplete.setAdapter(estadoCivilAdapter)

        salvarButton.setOnClickListener {
            val nome = nomeEditText.text?.toString()?.trim() ?: ""
            val cpf = cpfEditText.text?.toString()?.trim() ?: ""
            val dataNascimentoStr = dataNascimentoEditText.text?.toString()?.trim() ?: ""
            val telefone = telefoneEditText.text?.toString()?.trim() ?: ""
            val historico = historicoEditText.text?.toString()?.trim()
            val medicamentos = medicamentosEditText.text?.toString()?.trim()
            val alergias = alergiasEditText.text?.toString()?.trim()

            if (validarCampos(nome, cpf, dataNascimentoStr, telefone)) {
                lifecycleScope.launch {
                    val pacienteExistente = viewModel.getPatientByCpf(cpf)
                    if (pacienteExistente != null) {
                        Toast.makeText(this@CadastroPacienteActivity, "Já existe um paciente com este CPF.", Toast.LENGTH_LONG).show()
                        return@launch
                    }
                    try {
                        val dataNascimento = dateFormat.parse(dataNascimentoStr) ?: Date()
                        val paciente = Patient(
                            name = nome,
                            cpf = cpf,
                            birthDate = dataNascimento,
                            phone = telefone,
                            clinicalHistory = historico,
                            medications = medicamentos,
                            allergies = alergias
                        )
                        viewModel.savePatient(paciente)
                        Toast.makeText(this@CadastroPacienteActivity, getString(R.string.patient_saved), Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@CadastroPacienteActivity, getString(R.string.invalid_birth_date), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        voltarButton.setOnClickListener { finish() }
    }

    private fun validarCampos(
        nome: String,
        cpf: String,
        dataNascimento: String,
        telefone: String
    ): Boolean {
        if (nome.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_name), Toast.LENGTH_SHORT).show()
            return false
        }
        if (cpf.isEmpty() || cpf.length < 14) {
            Toast.makeText(this, getString(R.string.fill_cpf), Toast.LENGTH_SHORT).show()
            return false
        }
        if (dataNascimento.isEmpty() || dataNascimento.length < 10) {
            Toast.makeText(this, getString(R.string.fill_birth_date), Toast.LENGTH_SHORT).show()
            return false
        }
        if (telefone.isEmpty() || telefone.length < 14) {
            Toast.makeText(this, getString(R.string.fill_phone), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
} 