package com.example.psipro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.psipro.databinding.ActivityCadastroPacienteBinding
import com.example.psipro.utils.ValidationUtils
import com.example.psipro.viewmodel.PatientViewModel
import com.example.psipro.data.entities.Patient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class CadastroPacienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroPacienteBinding
    private val viewModel: PatientViewModel by viewModels()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", java.util.Locale("pt", "BR"))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                binding.salvarButton.isEnabled = !isLoading
                binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@CadastroPacienteActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.salvarButton.setOnClickListener {
            if (validateAndSavePatient()) {
                finish()
            }
        }
    }
    
    private fun validateAndSavePatient(): Boolean {
        val nome = binding.nomeEditText.text?.toString()?.trim() ?: ""
        val cpf = binding.cpfEditText.text?.toString()?.trim() ?: ""
        val dataNascimentoStr = binding.dataNascimentoEditText.text?.toString()?.trim() ?: ""
        val telefone = binding.telefoneEditText.text?.toString()?.trim() ?: ""
        val historico = binding.historicoEditText.text?.toString()?.trim()
        val valorSessao = binding.valorSessaoEditText.text?.toString()?.trim()
        val diaCobranca = binding.diaCobrancaEditText.text?.toString()?.trim()
        val lembreteCobranca = binding.lembreteCobrancaSwitch.isChecked
        val contatoNome = binding.contatoNomeEditText.text?.toString()?.trim()
        val contatoWhatsapp = binding.contatoWhatsappEditText.text?.toString()?.trim()
        
        // Validações
        when {
            nome.isEmpty() -> {
                showError("Preencha o nome completo")
                return false
            }
            !ValidationUtils.isValidCPF(cpf) -> {
                showError("CPF inválido")
                return false
            }
            !ValidationUtils.isValidBirthDate(dataNascimentoStr) -> {
                showError("Data de nascimento inválida")
                return false
            }
            !ValidationUtils.isValidPhone(telefone) -> {
                showError("Telefone inválido")
                return false
            }
            valorSessao.isNullOrEmpty() || !ValidationUtils.isValidSessionValue(valorSessao?.toDoubleOrNull() ?: 0.0) -> {
                showError("Valor da sessão inválido")
                return false
            }
            diaCobranca.isNullOrEmpty() || diaCobranca?.toIntOrNull() !in 1..31 -> {
                showError("Dia da cobrança inválido")
                return false
            }
            contatoWhatsapp?.isNotEmpty() == true && !ValidationUtils.isValidPhone(contatoWhatsapp) -> {
                showError("WhatsApp do contato inválido")
                return false
            }
        }
        
        val birthDate: Date? = try {
            dateFormat.parse(dataNascimentoStr)
        } catch (e: Exception) {
            null
        }
        
        if (birthDate == null) {
            showError("Data de nascimento inválida")
            return false
        }
        
        val patient = Patient(
            name = nome,
            cpf = cpf,
            birthDate = birthDate,
            phone = telefone,
            email = "", // Adapte se houver campo de email
            cep = "", // Adapte se houver campo de cep
            endereco = "", // Adapte se houver campo de endereço
            numero = "", // Adapte se houver campo de número
            bairro = "", // Adapte se houver campo de bairro
            cidade = "", // Adapte se houver campo de cidade
            estado = "", // Adapte se houver campo de estado
            complemento = "", // Adapte se houver campo de complemento
            sessionValue = valorSessao?.toDoubleOrNull() ?: 0.0,
            diaCobranca = diaCobranca?.toIntOrNull() ?: 1,
            lembreteCobranca = lembreteCobranca,
            clinicalHistory = historico,
            medications = null, // Adapte se houver campo de medicamentos
            allergies = null, // Adapte se houver campo de alergias
            isEncrypted = false
        )
        
        viewModel.savePatient(patient)
        return true
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}