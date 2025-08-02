package com.psipro.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.data.entities.Patient
import com.psipro.app.databinding.ActivityDadosPessoaisBinding
import com.psipro.app.viewmodel.DadosPessoaisViewModel
import java.util.Date
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import android.text.Editable
import android.text.TextWatcher
import java.text.NumberFormat
import java.util.Locale
import android.widget.ArrayAdapter
import com.psipro.app.data.entities.AnamneseGroup

@AndroidEntryPoint
class DadosPessoaisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDadosPessoaisBinding
    private val viewModel: DadosPessoaisViewModel by viewModels()
    private var patientId: Long = -1
    private var birthDate: Date? = null
    private var selectedAnamneseGroup: AnamneseGroup = AnamneseGroup.ADULTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosPessoaisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCurrencyMask()
        setupListeners()
        setupAnamneseGroupSpinner()

        patientId = intent.getLongExtra("PATIENT_ID", -1)
        if (patientId == -1L) {
            Toast.makeText(this, "Paciente não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            viewModel.patient.collectLatest { patient ->
                patient?.let { preencherCampos(it) }
            }
        }
        viewModel.getPatient(patientId)

        binding.btnSalvarDados.setOnClickListener {
            val pacienteAtualizado = coletarDadosDosCampos()
            viewModel.salvarPaciente(pacienteAtualizado)
            Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.switchLembreteCobranca.setOnCheckedChangeListener { _, isChecked ->
            // Lógica para mostrar/esconder aviso de cobrança automática
            if (isChecked) {
                // Mostrar aviso de cobrança automática - implementar quando necessário
                Toast.makeText(this, "Lembrete de cobrança ativado", Toast.LENGTH_SHORT).show()
            } else {
                // Esconder aviso
                Toast.makeText(this, "Lembrete de cobrança desativado", Toast.LENGTH_SHORT).show()
            }
        }

        // Clique na seta de voltar
        binding.btnVoltar.setOnClickListener { finish() }
    }

    private fun setupListeners() {
        // Configurar listeners para os novos campos
        binding.edtVideoCall.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Validar link de video chamada se necessário
            }
        })
    }

    private fun setupAnamneseGroupSpinner() {
        val tiposAnamnese = listOf(
            "Adulto",
            "Crianças", 
            "Adolescentes",
            "Idosos"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposAnamnese)
        binding.spinnerTipoAnamnese.setAdapter(adapter)
        
        // Configurar para mostrar dropdown ao clicar
        binding.spinnerTipoAnamnese.setOnClickListener {
            binding.spinnerTipoAnamnese.showDropDown()
        }
        
        binding.spinnerTipoAnamnese.setOnItemClickListener { _, _, position, _ ->
            selectedAnamneseGroup = when (position) {
                0 -> AnamneseGroup.ADULTO
                1 -> AnamneseGroup.CRIANCAS
                2 -> AnamneseGroup.ADOLESCENTES
                3 -> AnamneseGroup.IDOSOS
                else -> AnamneseGroup.ADULTO
            }
            // Fechar o dropdown após seleção
            binding.spinnerTipoAnamnese.dismissDropDown()
        }
        
        // Configurar para mostrar dropdown ao tocar
        binding.spinnerTipoAnamnese.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                binding.spinnerTipoAnamnese.showDropDown()
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun setupCurrencyMask() {
        val editText = binding.edtValorSessao
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed / 100)
                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } else {
                        current = ""
                        editText.setText("")
                    }
                    editText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun preencherCampos(patient: Patient) {
        binding.edtNome.setText(patient.name)
        binding.edtCpf.setText(patient.cpf)
        binding.edtTelefone.setText(patient.phone)
        binding.edtEmail.setText(patient.email)
        binding.edtCep.setText(patient.cep)
        binding.edtEndereco.setText(patient.endereco)
        binding.edtNumero.setText(patient.numero)
        binding.edtBairro.setText(patient.bairro)
        binding.edtCidade.setText(patient.cidade)
        binding.edtEstado.setText(patient.estado)
        binding.edtComplemento.setText(patient.complemento)
        binding.edtValorSessao.setText(if (patient.sessionValue > 0) NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(patient.sessionValue) else "")
        binding.edtDiaCobranca.setText(patient.diaCobranca.toString())
        binding.switchLembreteCobranca.isChecked = patient.lembreteCobranca
        birthDate = patient.birthDate
        
        // Preencher grupo de anamnese
        selectedAnamneseGroup = patient.anamneseGroup
        val tipoAnamneseText = when (patient.anamneseGroup) {
            AnamneseGroup.ADULTO -> "Adulto"
            AnamneseGroup.CRIANCAS -> "Crianças"
            AnamneseGroup.ADOLESCENTES -> "Adolescentes"
            AnamneseGroup.IDOSOS -> "Idosos"
        }
        binding.spinnerTipoAnamnese.setText(tipoAnamneseText, false)
        
        // Preencher campos adicionais se existirem
        // binding.edtVideoCall.setText(patient.videoCallLink ?: "")
        // binding.edtRg.setText(patient.rg ?: "")
    }

    private fun coletarDadosDosCampos(): Patient {
        val dataNascimentoFinal = birthDate ?: Date()
        
        return Patient(
            id = patientId,
            name = binding.edtNome.text.toString(),
            cpf = binding.edtCpf.text.toString(),
            birthDate = dataNascimentoFinal,
            phone = binding.edtTelefone.text.toString(),
            email = binding.edtEmail.text.toString(),
            cep = binding.edtCep.text.toString(),
            endereco = binding.edtEndereco.text.toString(),
            numero = binding.edtNumero.text.toString(),
            bairro = binding.edtBairro.text.toString(),
            cidade = binding.edtCidade.text.toString(),
            estado = binding.edtEstado.text.toString(),
            complemento = binding.edtComplemento.text.toString(),
            sessionValue = binding.edtValorSessao.text.toString()
                .replace("[^\\d,.]".toRegex(), "")
                .replace(",", ".")
                .toDoubleOrNull() ?: 0.0,
            diaCobranca = binding.edtDiaCobranca.text.toString().toIntOrNull() ?: 1,
            lembreteCobranca = binding.switchLembreteCobranca.isChecked,
            clinicalHistory = null,
            medications = null,
            allergies = null,
            anamneseGroup = selectedAnamneseGroup
            // videoCallLink = binding.edtVideoCall.text.toString(),
            // rg = binding.edtRg.text.toString()
        )
    }
} 



