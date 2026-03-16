package com.psipro.app.ui.fragments

import com.psipro.app.ui.viewmodels.PatientViewModel
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.psipro.app.R
import com.psipro.app.databinding.FragmentBirthdayBinding
import java.text.SimpleDateFormat
import java.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class BirthdayFragment : Fragment() {
    private var _binding: FragmentBirthdayBinding? = null
    private val binding get() = _binding!!
    private val patientViewModel: PatientViewModel by viewModels()
    private var selectedPatient: com.psipro.app.data.entities.Patient? = null
    private var aniversariantesMes: List<com.psipro.app.data.entities.Patient> = emptyList()
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var psicologoNome: String = ""
    
    private val baseMessages = listOf(
        "Olá, [Nome]!\nHoje é um dia especial e não poderia deixar passar em branco. Desejo que seu novo ciclo seja cheio de significado, saúde emocional e crescimento pessoal.\nUm forte abraço,\n[Psicologo]",
        "Feliz aniversário, [Nome]!\nQue este novo ano traga leveza, coragem e muitas alegrias para sua vida. Estarei sempre por aqui, torcendo pelo seu bem-estar!\nCom carinho,\n[Psicologo]",
        "Parabéns pelo seu dia, [Nome].\nQue você celebre não apenas mais um ano de vida, mas tudo o que construiu até aqui. Que venha um novo ciclo repleto de conquistas e autoconhecimento.\nConte comigo nessa jornada.\n[Psicologo]",
        "Olá, [Nome]!\nParabéns! Que hoje seja um dia cheio de afeto, boas memórias e sorrisos sinceros. Você merece tudo de melhor nesse novo ano!\nUm abraço caloroso,\n[Psicologo]",
        "Feliz aniversário, [Nome]!\nQue neste novo ciclo você continue cuidando da sua saúde emocional com o carinho e atenção que merece. Que a paz e o equilíbrio sejam seus maiores presentes!\nEstarei aqui sempre que precisar.\n[Psicologo]"
    )

    private val monthNames = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBirthdayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarNomePsicologo()
        setupListeners()
        setupMonthNavigation()
        loadBirthdays()
    }

    private fun carregarNomePsicologo() {
        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        psicologoNome = prefs.getString("profile_name", "Profissional") ?: "Profissional"
    }

    private fun getMessagesWithPsicologoName(): List<String> {
        return baseMessages.map { message ->
            message.replace("[Psicologo]", "Profissional $psicologoNome")
        }
    }

    private fun setupListeners() {
        binding.copyButton.setOnClickListener {
            val msg = getFinalMessage()
            val clipboard = ContextCompat.getSystemService(requireContext(), android.content.ClipboardManager::class.java)
            val clip = android.content.ClipData.newPlainText("Mensagem de aniversário", msg)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Mensagem copiada!", Toast.LENGTH_SHORT).show()
        }

        binding.whatsappButton.setOnClickListener {
            selectedPatient?.let { paciente ->
                val msg = getFinalMessage()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.setPackage("com.whatsapp")
                intent.putExtra(Intent.EXTRA_TEXT, msg)
                intent.putExtra("jid", "55${paciente.phone}@s.whatsapp.net")
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "WhatsApp não instalado.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "Selecione um paciente primeiro", Toast.LENGTH_SHORT).show()
            }
        }

        binding.prevMonthButton.setOnClickListener {
            if (currentMonth == 0) {
                currentMonth = 11
                currentYear--
            } else {
                currentMonth--
            }
            updateMonthDisplay()
            loadBirthdays()
        }

        binding.nextMonthButton.setOnClickListener {
            if (currentMonth == 11) {
                currentMonth = 0
                currentYear++
            } else {
                currentMonth++
            }
            updateMonthDisplay()
            loadBirthdays()
        }

        // Listener para quando a mensagem selecionada mudar
        binding.messageSpinner.setOnItemClickListener { _, _, position, _ ->
            val messagesWithPsicologo = getMessagesWithPsicologoName()
            selectedPatient?.let { paciente ->
                val selectedMessage = messagesWithPsicologo[position].replace("[Nome]", paciente.name)
                binding.messageSpinner.setText(selectedMessage, false)
                // Atualizar o campo de edição com a mensagem selecionada
                binding.editMessageText.setText(selectedMessage)
            }
        }

        // Listener para quando o paciente mudar
        binding.patientSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedPatient = aniversariantesMes[position]
            updateMessageWithSelectedPatient()
        }
    }

    private fun setupMonthNavigation() {
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        binding.monthYearText.text = "${monthNames[currentMonth]} $currentYear"
    }

    private fun loadBirthdays() {
        viewLifecycleOwner.lifecycleScope.launch {
            patientViewModel.patients.collect { lista ->
                val today = Calendar.getInstance()
                val currentDay = today.get(Calendar.DAY_OF_MONTH)
                val currentMonthToday = today.get(Calendar.MONTH)
                val currentYearToday = today.get(Calendar.YEAR)
                
                // Filtrar aniversariantes do mês selecionado
                aniversariantesMes = lista.filter { paciente ->
                    val cal = Calendar.getInstance().apply { time = paciente.birthDate }
                    cal.get(Calendar.MONTH) == currentMonth
                }.sortedBy { paciente ->
                    val cal = Calendar.getInstance().apply { time = paciente.birthDate }
                    cal.get(Calendar.DAY_OF_MONTH)
                }

                if (aniversariantesMes.isEmpty()) {
                    binding.birthdayList.text = "Nenhum aniversariante em ${monthNames[currentMonth]}."
                    binding.messageLayout.visibility = View.GONE
                    binding.patientSpinner.visibility = View.GONE
                } else {
                    // Separar aniversariantes de hoje e do mês
                    val aniversariantesHoje = aniversariantesMes.filter { paciente ->
                        val cal = Calendar.getInstance().apply { time = paciente.birthDate }
                        cal.get(Calendar.DAY_OF_MONTH) == currentDay &&
                        currentMonth == currentMonthToday &&
                        currentYear == currentYearToday
                    }
                    
                    val aniversariantesOutrosDias = aniversariantesMes.filter { paciente ->
                        val cal = Calendar.getInstance().apply { time = paciente.birthDate }
                        !(cal.get(Calendar.DAY_OF_MONTH) == currentDay &&
                        currentMonth == currentMonthToday &&
                        currentYear == currentYearToday)
                    }

                    // Construir texto com todos os aniversariantes
                    val nomes = buildString {
                        if (aniversariantesHoje.isNotEmpty()) {
                            appendLine("🎂 ANIVERSARIANTES DE HOJE:")
                            aniversariantesHoje.forEach { paciente ->
                                val idade = calcularIdade(paciente.birthDate)
                                val dataFormatada = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(paciente.birthDate)
                                appendLine("• ${paciente.name} (${idade} anos) - ${dataFormatada}")
                            }
                            appendLine()
                        }
                        
                        if (aniversariantesOutrosDias.isNotEmpty()) {
                            appendLine("📅 ANIVERSARIANTES DE ${monthNames[currentMonth].uppercase()}:")
                            aniversariantesOutrosDias.forEach { paciente ->
                                val idade = calcularIdade(paciente.birthDate)
                                val dataFormatada = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(paciente.birthDate)
                                appendLine("• ${paciente.name} (${idade} anos) - ${dataFormatada}")
                            }
                        }
                    }
                    
                    binding.birthdayList.text = nomes
                    binding.messageLayout.visibility = View.VISIBLE
                    binding.patientSpinner.visibility = View.VISIBLE
                    
                    // Configurar AutoCompleteTextView de pacientes
                    val patientNames = aniversariantesMes.map { "${it.name} (${SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(it.birthDate)})" }
                    val patientAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, patientNames)
                    binding.patientSpinner.setAdapter(patientAdapter)
                    
                    // Configurar AutoCompleteTextView de mensagens com nome do psicólogo
                    val messagesWithPsicologo = getMessagesWithPsicologoName()
                    val messageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, messagesWithPsicologo)
                    binding.messageSpinner.setAdapter(messageAdapter)
                    
                    // Selecionar primeiro paciente por padrão
                    if (aniversariantesMes.isNotEmpty()) {
                        selectedPatient = aniversariantesMes[0]
                        binding.patientSpinner.setText(patientNames[0], false)
                        updateMessageWithSelectedPatient()
                    }
                    
                    // Listener para mudança de paciente
                    binding.patientSpinner.setOnItemClickListener { _, _, position, _ ->
                        selectedPatient = aniversariantesMes[position]
                        updateMessageWithSelectedPatient()
                    }
                    
                    // Listener para mudança de mensagem
                    binding.messageSpinner.setOnItemClickListener { _, _, position, _ ->
                        // A mensagem já está personalizada, só precisa atualizar o texto
                        val messagesWithPsicologo = getMessagesWithPsicologoName()
                        selectedPatient?.let { paciente ->
                            val selectedMessage = messagesWithPsicologo[position].replace("[Nome]", paciente.name)
                            binding.messageSpinner.setText(selectedMessage, false)
                        }
                    }
                    
                    // Habilitar dropdown para ambos os campos
                    binding.patientSpinner.setOnClickListener {
                        binding.patientSpinner.showDropDown()
                    }
                    
                    binding.messageSpinner.setOnClickListener {
                        binding.messageSpinner.showDropDown()
                    }
                }
            }
        }
    }

    private fun updateMessageWithSelectedPatient() {
        selectedPatient?.let { paciente ->
            val messagesWithPsicologo = getMessagesWithPsicologoName()
            val updatedMessages = messagesWithPsicologo.map { it.replace("[Nome]", paciente.name) }
            val messageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, updatedMessages)
            binding.messageSpinner.setAdapter(messageAdapter)
            if (updatedMessages.isNotEmpty()) {
                binding.messageSpinner.setText(updatedMessages[0], false)
                // Atualizar também o campo de edição
                binding.editMessageText.setText(updatedMessages[0])
            }
        }
    }

    private fun calcularIdade(dataNascimento: Date): Int {
        val hoje = Calendar.getInstance()
        val nascimento = Calendar.getInstance().apply { time = dataNascimento }
        var idade = hoje.get(Calendar.YEAR) - nascimento.get(Calendar.YEAR)
        if (hoje.get(Calendar.DAY_OF_YEAR) < nascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--
        }
        return idade
    }

    private fun getFinalMessage(): String {
        // Se o campo de edição tem texto, usar ele. Senão, usar a mensagem selecionada
        val editedMessage = binding.editMessageText.text.toString().trim()
        return if (editedMessage.isNotEmpty()) {
            editedMessage
        } else {
            binding.messageSpinner.text.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




