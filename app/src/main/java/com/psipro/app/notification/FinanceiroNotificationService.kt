package com.psipro.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.psipro.app.R
import com.psipro.app.ui.screens.FinanceiroDashboardActivity
import javax.inject.Inject

class FinanceiroNotificationService @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "financeiro_channel"
    private val channelName = "Controle Financeiro"
    private val channelDescription = "Notificações de controle financeiro"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacaoPagamentosPendentes(count: Int, valorTotal: Double) {
        val intent = Intent(context, FinanceiroDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("💰 Pagamentos Pendentes")
            .setContentText("$count pagamentos pendentes - Total: R$ ${String.format("%.2f", valorTotal)}")
            .setSmallIcon(R.drawable.ic_notification_money)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1001, notification)
    }

    fun mostrarNotificacaoRelatorioDiario(
        totalRecebido: Double,
        totalPendente: Double,
        countRecebidos: Int,
        countPendentes: Int
    ) {
        val intent = Intent(context, FinanceiroDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("📊 Relatório Diário")
            .setContentText("Recebido: R$ ${String.format("%.2f", totalRecebido)} | Pendente: R$ ${String.format("%.2f", totalPendente)}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("""
                    📈 Resumo do Dia:
                    ✅ Recebidos: $countRecebidos (R$ ${String.format("%.2f", totalRecebido)})
                    ⏳ Pendentes: $countPendentes (R$ ${String.format("%.2f", totalPendente)})
                    
                    Toque para ver detalhes
                """.trimIndent()))
            .setSmallIcon(R.drawable.ic_notification_report)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1002, notification)
    }

    fun mostrarNotificacaoPagamentoVencido(pacienteNome: String, valor: Double) {
        val intent = Intent(context, FinanceiroDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("⚠️ Pagamento Vencido")
            .setContentText("$pacienteNome - R$ ${String.format("%.2f", valor)}")
            .setSmallIcon(R.drawable.ic_notification_warning)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1003, notification)
    }
} 