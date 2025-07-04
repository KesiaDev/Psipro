package com.example.psipro.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.psipro.data.entities.CobrancaSessao
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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

    fun gerarMensagemLembretePaciente(paciente: com.example.psipro.data.entities.Patient): String {
        return "Olá, ${paciente.name}! Este é um lembrete amigável para o pagamento da sua sessão. Caso já tenha efetuado, desconsidere. Obrigado!"
    }

    fun intentWhatsApp(context: Context, telefone: String, mensagem: String): Intent {
        val telefoneFormatado = formatarTelefone(telefone)
        val uri = android.net.Uri.parse("https://wa.me/$telefoneFormatado?text=" + android.net.Uri.encode(mensagem))
        return Intent(Intent.ACTION_VIEW, uri)
    }
} 