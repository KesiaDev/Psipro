package com.example.apppisc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.apppisc.data.entities.Patient
import com.example.apppisc.databinding.ActivityDadosPessoaisBinding
import com.example.apppisc.viewmodel.DadosPessoaisViewModel
import java.util.Date
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import android.text.Editable
import android.text.TextWatcher
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DadosPessoaisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDadosPessoaisBinding
    private val viewModel: DadosPessoaisViewModel by viewModels()
    private var patientId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDadosPessoaisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCurrencyMask()

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
            binding.txtAtencaoCobranca.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Clique na seta de voltar
        val headerLayout = (binding.root as android.widget.ScrollView).getChildAt(0) as android.widget.LinearLayout
        val header = headerLayout.getChildAt(0) as android.widget.LinearLayout
        val backArrow = header.getChildAt(0)
        backArrow.setOnClickListener { finish() }
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
        binding.edtValorSessao.setText(patient.sessionValue.toString())
        binding.edtDiaCobranca.setText(patient.diaCobranca.toString())
        binding.switchLembreteCobranca.isChecked = patient.lembreteCobranca
    }

    private fun coletarDadosDosCampos(): Patient {
        return Patient(
            id = patientId,
            name = binding.edtNome.text.toString(),
            cpf = binding.edtCpf.text.toString(),
            birthDate = Date(), // Ajuste conforme necessário
            phone = binding.edtTelefone.text.toString(),
            email = binding.edtEmail.text.toString(),
            cep = binding.edtCep.text.toString(),
            endereco = binding.edtEndereco.text.toString(),
            numero = binding.edtNumero.text.toString(),
            bairro = binding.edtBairro.text.toString(),
            cidade = binding.edtCidade.text.toString(),
            estado = binding.edtEstado.text.toString(),
            complemento = binding.edtComplemento.text.toString(),
            sessionValue = binding.edtValorSessao.text.toString().toDoubleOrNull() ?: 0.0,
            diaCobranca = binding.edtDiaCobranca.text.toString().toIntOrNull() ?: 1,
            lembreteCobranca = binding.switchLembreteCobranca.isChecked,
            clinicalHistory = null,
            medications = null,
            allergies = null
        )
    }
} 