package com.example.psipro.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.psipro.databinding.FragmentSuporteBinding

class SuporteFragment : Fragment() {
    private var _binding: FragmentSuporteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuporteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:apppsipro@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Suporte Psipro")
            }
            startActivity(intent)
        }
        binding.whatsappButton.setOnClickListener {
            val phone = "54992448888" // DDD + número sem espaços ou traços
            val url = "https://wa.me/$phone"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(url)
            }
            startActivity(intent)
        }
        binding.faqButton.setOnClickListener {
            val faqs = arrayOf(
                "Como agendar uma nova consulta?",
                "Como remarcar ou cancelar um atendimento?",
                "Como acessar o prontuário do paciente?",
                "Como gerar relatórios?",
                "Como recebo notificações de consultas?",
                "Como entrar em contato com o suporte?",
                "Como meus dados estão protegidos?"
            )
            val respostas = arrayOf(
                "Acesse a aba de agendamentos e clique em 'Nova consulta'. Preencha os dados e confirme.",
                "Selecione a consulta desejada na agenda e utilize as opções de remarcar ou cancelar.",
                "No menu lateral, acesse 'Prontuários' e selecione o paciente para visualizar ou editar o registro.",
                "Vá até a seção de relatórios e escolha o tipo de relatório que deseja gerar. Os relatórios podem ser exportados ou visualizados no app.",
                "O aplicativo envia lembretes automáticos para consultas agendadas, tanto para o profissional quanto para o paciente.",
                "Você pode usar o botão de e-mail ou WhatsApp na aba de Suporte para falar diretamente com nossa equipe.",
                "Todos os dados são criptografados e protegidos por autenticação. Apenas usuários autorizados têm acesso às informações."
            )
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Perguntas Frequentes")
            builder.setItems(faqs) { _, which ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(faqs[which])
                    .setMessage(respostas[which])
                    .setPositiveButton("OK", null)
                    .show()
            }
            builder.setNegativeButton("Fechar", null)
            builder.show()
        }
        binding.feedbackButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:apppsipro@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback Psipro")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 