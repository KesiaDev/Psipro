package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.psipro.app.data.entities.CobrancaSessao
import com.psipro.app.data.entities.Patient
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class TipoMensagemWhatsApp {
    CONFIRMACAO_CONSULTA,
    LEMBRETE_CONSULTA,
    SOLICITACAO_DOCUMENTOS,
    ENVIO_RELATORIO,
    REENGAJAMENTO,
    ENCAMINHAMENTO,
    ENVIO_EXERCICIO,
    ATENDIMENTO_EMERGENCIA,
    MENSAGEM_PERSONALIZADA
}

object WhatsAppUtils {
    
    fun enviarCobrancaWhatsApp(context: Context, cobranca: CobrancaSessao, telefone: String) {
        val mensagem = gerarMensagemCobranca(cobranca)
        enviarMensagemWhatsApp(context, telefone, mensagem)
    }
    
    fun enviarMensagemWhatsApp(context: Context, telefone: String, mensagem: String) {
        try {
            // Formatar telefone (remover caracteres especiais e adicionar código do país se necessário)
            val telefoneFormatado = formatarTelefone(telefone)
            
            // Criar URI para WhatsApp
            val uri = Uri.parse("https://wa.me/$telefoneFormatado?text=${Uri.encode(mensagem)}")
            
            // Intent para abrir WhatsApp
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Verificar se WhatsApp está instalado
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // WhatsApp não instalado, abrir no navegador
                val intentBrowser = Intent(Intent.ACTION_VIEW, uri)
                intentBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intentBrowser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun gerarMensagemCobranca(cobranca: CobrancaSessao): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        
        val mensagem = StringBuilder()
        mensagem.append("Olá! Aqui está o lembrete de pagamento da sessão ${cobranca.numeroSessao}.\n\n")
        mensagem.append("📅 Data da sessão: ${dateFormatter.format(cobranca.dataSessao)}\n")
        mensagem.append("💰 Valor: ${formatter.format(cobranca.valor)}\n")
        mensagem.append("📅 Vencimento: ${dateFormatter.format(cobranca.dataVencimento)}\n\n")
        
        if (cobranca.pixCopiaCola.isNotEmpty()) {
            mensagem.append("💳 PIX Copia e Cola:\n")
            mensagem.append("${cobranca.pixCopiaCola}\n\n")
        }
        
        mensagem.append("Obrigado!")
        
        return mensagem.toString()
    }
    
    fun gerarMensagemLembrete(cobranca: CobrancaSessao): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        
        val mensagem = StringBuilder()
        mensagem.append("Olá! Lembrete amigável sobre o pagamento da sessão ${cobranca.numeroSessao}.\n\n")
        mensagem.append("📅 Data da sessão: ${dateFormatter.format(cobranca.dataSessao)}\n")
        mensagem.append("💰 Valor: ${formatter.format(cobranca.valor)}\n")
        mensagem.append("📅 Vencimento: ${dateFormatter.format(cobranca.dataVencimento)}\n\n")
        
        if (cobranca.pixCopiaCola.isNotEmpty()) {
            mensagem.append("💳 PIX Copia e Cola:\n")
            mensagem.append("${cobranca.pixCopiaCola}\n\n")
        }
        
        mensagem.append("Agradeço a atenção!")
        
        return mensagem.toString()
    }
    
    fun gerarMensagemVencida(cobranca: CobrancaSessao): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        
        val mensagem = StringBuilder()
        mensagem.append("Olá! O pagamento da sessão ${cobranca.numeroSessao} está vencido.\n\n")
        mensagem.append("📅 Data da sessão: ${dateFormatter.format(cobranca.dataSessao)}\n")
        mensagem.append("💰 Valor: ${formatter.format(cobranca.valor)}\n")
        mensagem.append("📅 Vencimento: ${dateFormatter.format(cobranca.dataVencimento)}\n\n")
        
        if (cobranca.pixCopiaCola.isNotEmpty()) {
            mensagem.append("💳 PIX Copia e Cola:\n")
            mensagem.append("${cobranca.pixCopiaCola}\n\n")
        }
        
        mensagem.append("Por favor, regularize o pagamento. Obrigado!")
        
        return mensagem.toString()
    }
    
    private fun formatarTelefone(telefone: String): String {
        // Remove todos os caracteres não numéricos
        val numeros = telefone.replace(Regex("[^0-9]"), "")
        
        // Adiciona código do Brasil se não tiver
        return if (numeros.startsWith("55")) {
            numeros
        } else if (numeros.startsWith("0")) {
            "55${numeros.substring(1)}"
        } else {
            "55$numeros"
        }
    }
    
    fun copiarParaClipboard(context: Context, texto: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("PIX", texto)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "PIX copiado para área de transferência", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao copiar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun gerarMensagemLembretePaciente(paciente: com.psipro.app.data.entities.Patient): String {
        return "Olá, ${paciente.name}! Este é um lembrete amigável para o pagamento da sua sessão. Caso já tenha efetuado, desconsidere. Obrigado!"
    }

    fun intentWhatsApp(context: Context, telefone: String, mensagem: String): Intent {
        val telefoneFormatado = formatarTelefone(telefone)
        val uri = android.net.Uri.parse("https://wa.me/$telefoneFormatado?text=" + android.net.Uri.encode(mensagem))
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /**
     * Gera mensagem baseada no tipo selecionado
     */
    fun gerarMensagemPorTipo(tipo: TipoMensagemWhatsApp, paciente: Patient, psicologoNome: String, dadosExtras: Map<String, String> = emptyMap()): String {
        return when (tipo) {
            TipoMensagemWhatsApp.CONFIRMACAO_CONSULTA -> {
                val data = dadosExtras["data"] ?: "data agendada"
                val hora = dadosExtras["hora"] ?: "horário agendado"
                "Olá, ${paciente.name}! Tudo bem? Só passando para confirmar nossa consulta marcada para $data às $hora. Qualquer dúvida, fico à disposição.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.LEMBRETE_CONSULTA -> {
                val hora = dadosExtras["hora"] ?: "horário agendado"
                "Oi, ${paciente.name}! Passando para lembrar da nossa sessão hoje às $hora, conforme agendado.\nAté logo 😊\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.SOLICITACAO_DOCUMENTOS -> {
                "Oi, ${paciente.name}. Poderia, por gentileza, enviar os documentos pendentes combinados para o nosso acompanhamento?\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.ENVIO_RELATORIO -> {
                "Olá, ${paciente.name}. Estou te enviando o material da nossa última sessão (ou plano terapêutico). Qualquer dúvida, estou por aqui.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.REENGAJAMENTO -> {
                "Oi, ${paciente.name}. Notei sua ausência nas últimas sessões e gostaria de saber como você está. Se precisar conversar ou remarcar, estou à disposição.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.ENCAMINHAMENTO -> {
                "Oi, ${paciente.name}. Conforme conversamos, estou te encaminhando o contato do(a) profissional especializado(a) para sua necessidade. Qualquer coisa, sigo à disposição.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.ENVIO_EXERCICIO -> {
                val link = dadosExtras["link"] ?: "material solicitado"
                "Olá, ${paciente.name}. Segue o link que comentei na sessão:\n$link\nPode realizar com calma e me contar como foi.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.ATENDIMENTO_EMERGENCIA -> {
                "Oi, ${paciente.name}. Senti que você pode estar precisando de um apoio extra. Caso queira conversar ou agendar uma sessão, me avise. Estou por aqui.\n\n– $psicologoNome"
            }
            
            TipoMensagemWhatsApp.MENSAGEM_PERSONALIZADA -> {
                "Olá, ${paciente.name}! Aqui é $psicologoNome, estou entrando em contato através do PsiPro."
            }
        }
    }

    /**
     * Obtém a lista de tipos de mensagem disponíveis
     */
    fun getTiposMensagem(): List<TipoMensagemWhatsApp> {
        return TipoMensagemWhatsApp.values().toList()
    }

    /**
     * Obtém o título amigável para cada tipo de mensagem
     */
    fun getTituloTipoMensagem(tipo: TipoMensagemWhatsApp): String {
        return when (tipo) {
            TipoMensagemWhatsApp.CONFIRMACAO_CONSULTA -> "📅 Confirmação de Consulta"
            TipoMensagemWhatsApp.LEMBRETE_CONSULTA -> "📌 Lembrete de Consulta"
            TipoMensagemWhatsApp.SOLICITACAO_DOCUMENTOS -> "📥 Solicitação de Documentos"
            TipoMensagemWhatsApp.ENVIO_RELATORIO -> "�� Envio de Relatório/Plano"
            TipoMensagemWhatsApp.REENGAJAMENTO -> "🙋‍♀️ Reengajamento de Paciente"
            TipoMensagemWhatsApp.ENCAMINHAMENTO -> "💬 Encaminhamento para Profissional"
            TipoMensagemWhatsApp.ENVIO_EXERCICIO -> "🎓 Envio de Exercício/Link"
            TipoMensagemWhatsApp.ATENDIMENTO_EMERGENCIA -> "📞 Atendimento Emergencial"
            TipoMensagemWhatsApp.MENSAGEM_PERSONALIZADA -> "✏️ Mensagem Personalizada"
        }
    }

    /**
     * Abre o WhatsApp com uma mensagem personalizada para o paciente
     * 
     * @param context Contexto da aplicação
     * @param paciente Paciente para quem enviar a mensagem
     * @param psicologoNome Nome do psicólogo (pode ser fixo ou vir de configurações)
     */
    fun abrirWhatsapp(context: Context, paciente: Patient, psicologoNome: String) {
        // Verificar se o telefone é válido
        val telefone = paciente.phone?.trim()
        if (telefone.isNullOrBlank() || telefone.length < 10) {
            Toast.makeText(context, "Telefone inválido ou não informado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Formatar o número (remover caracteres especiais e adicionar código do país se necessário)
        val numeroFormatado = formatarNumeroTelefone(telefone)
        
        // Criar a mensagem personalizada
        val mensagem = "Olá, ${paciente.name}! Aqui é $psicologoNome, estou entrando em contato através do PsiPro."
        
        // Codificar a mensagem para URL
        val mensagemCodificada = URLEncoder.encode(mensagem, StandardCharsets.UTF_8.toString())
        
        // Criar o link do WhatsApp
        val urlWhatsApp = "https://wa.me/$numeroFormatado?text=$mensagemCodificada"
        
        // Tentar abrir o WhatsApp diretamente
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWhatsApp))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se falhar, tentar abrir no navegador
            try {
                val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(urlWhatsApp))
                context.startActivity(intentBrowser)
            } catch (e2: Exception) {
                Toast.makeText(context, "Erro ao abrir WhatsApp: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Abre o WhatsApp com uma mensagem específica
     */
    fun abrirWhatsappComMensagem(context: Context, paciente: Patient, psicologoNome: String, tipo: TipoMensagemWhatsApp, dadosExtras: Map<String, String> = emptyMap()) {
        // Verificar se o telefone é válido
        val telefone = paciente.phone?.trim()
        if (telefone.isNullOrBlank() || telefone.length < 10) {
            Toast.makeText(context, "Telefone inválido ou não informado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Formatar o número
        val numeroFormatado = formatarNumeroTelefone(telefone)
        
        // Gerar mensagem baseada no tipo
        val mensagem = gerarMensagemPorTipo(tipo, paciente, psicologoNome, dadosExtras)
        
        // Codificar a mensagem para URL
        val mensagemCodificada = URLEncoder.encode(mensagem, StandardCharsets.UTF_8.toString())
        
        // Criar o link do WhatsApp
        val urlWhatsApp = "https://wa.me/$numeroFormatado?text=$mensagemCodificada"
        
        // Tentar abrir o WhatsApp
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWhatsApp))
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intentBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(urlWhatsApp))
                context.startActivity(intentBrowser)
            } catch (e2: Exception) {
                Toast.makeText(context, "Erro ao abrir WhatsApp: ${e2.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Formata o número de telefone para o padrão internacional
     */
    private fun formatarNumeroTelefone(telefone: String): String {
        // Remover todos os caracteres não numéricos
        var numero = telefone.replace(Regex("[^0-9]"), "")
        
        // Se não começar com 55 (código do Brasil), adicionar
        if (!numero.startsWith("55")) {
            numero = "55$numero"
        }
        
        return numero
    }
    
    /**
     * Verifica se o WhatsApp está instalado no dispositivo
     */
    private fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            // Verificar se o WhatsApp está instalado
            val packageInfo = context.packageManager.getPackageInfo("com.whatsapp", 0)
            packageInfo != null
        } catch (e: PackageManager.NameNotFoundException) {
            // WhatsApp não encontrado
            false
        } catch (e: Exception) {
            // Outro erro, mas vamos tentar abrir mesmo assim
            true
        }
    }
    
    /**
     * Obtém o nome do psicólogo das configurações
     */
    fun obterNomePsicologo(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("profile_name", "Psicólogo") ?: "Psicólogo"
    }
} 



