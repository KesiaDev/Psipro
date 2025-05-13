package com.example.apppisc.ui.fragments

import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.apppisc.R
import com.example.apppisc.databinding.FragmentBirthdayBinding
import com.example.apppisc.viewmodel.PatientViewModel
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
    private val messages = listOf(
        "Olá, [Nome]!\nHoje é um dia especial e não poderia deixar passar em branco. Desejo que seu novo ciclo seja cheio de significado, saúde emocional e crescimento pessoal.\nUm forte abraço,\nPsicóloga Marian Martins",
        "Feliz aniversário, [Nome]!\nQue este novo ano traga leveza, coragem e muitas alegrias para sua vida. Estarei sempre por aqui, torcendo pelo seu bem-estar!\nCom carinho,\nMarian Martins – Psicóloga",
        "Parabéns pelo seu dia, [Nome].\nQue você celebre não apenas mais um ano de vida, mas tudo o que construiu até aqui. Que venha um novo ciclo repleto de conquistas e autoconhecimento.\nConte comigo nessa jornada.\nPsicóloga Marian Martins",
        "Olá, [Nome]!\nParabéns! Que hoje seja um dia cheio de afeto, boas memórias e sorrisos sinceros. Você merece tudo de melhor nesse novo ano!\nUm abraço caloroso,\nMarian Martins",
        "Feliz aniversário, [Nome]!\nQue neste novo ciclo você continue cuidando da sua saúde emocional com o carinho e atenção que merece. Que a paz e o equilíbrio sejam seus maiores presentes!\nEstarei aqui sempre que precisar.\nPsicóloga Marian Martins"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBirthdayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            patientViewModel.patients.collect { lista ->
                val today = Calendar.getInstance()
                val pacientesHoje = lista.filter { paciente ->
                    val cal = Calendar.getInstance().apply { time = paciente.birthDate }
                    cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH) &&
                    cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                }
                if (pacientesHoje.isEmpty()) {
                    binding.birthdayList.text = "Nenhum aniversariante hoje."
                    binding.messageLayout.visibility = View.GONE
                } else {
                    val nomes = pacientesHoje.joinToString("\n") { paciente ->
                        val idade = calcularIdade(paciente.birthDate)
                        "🎂 ${paciente.name} (${idade} anos)"
                    }
                    binding.birthdayList.text = nomes
                    binding.messageLayout.visibility = View.VISIBLE
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, messages.map { it.replace("[Nome]", pacientesHoje[0].name) })
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.messageSpinner.adapter = adapter
                    binding.copyButton.setOnClickListener {
                        val msg = binding.messageSpinner.selectedItem.toString()
                        val clipboard = ContextCompat.getSystemService(requireContext(), android.content.ClipboardManager::class.java)
                        val clip = android.content.ClipData.newPlainText("Mensagem de aniversário", msg)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "Mensagem copiada!", Toast.LENGTH_SHORT).show()
                    }
                    binding.whatsappButton.setOnClickListener {
                        val msg = binding.messageSpinner.selectedItem.toString()
                        val paciente = pacientesHoje[0]
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
                    }
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
