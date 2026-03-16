package com.psipro.app.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.psipro.app.data.entities.StatusPagamento
import com.psipro.app.data.entities.AppointmentStatus
import androidx.compose.foundation.layout.padding
import com.psipro.app.ui.compose.StatusColors

@Composable
fun StatusChip(status: StatusPagamento, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, text) = when (status) {
        StatusPagamento.PAGO -> Triple(StatusColors.Success, Color.White, "Pago")
        StatusPagamento.A_RECEBER -> Triple(StatusColors.Warning, Color.White, "A Receber")
        StatusPagamento.VENCIDO -> Triple(StatusColors.Error, Color.White, "Vencido")
        StatusPagamento.CANCELADO -> Triple(StatusColors.Neutral, Color.White, "Cancelado")
    }
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AppointmentStatusChip(status: AppointmentStatus, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, text) = when (status) {
        AppointmentStatus.CONFIRMADO -> Triple(StatusColors.Success, Color.White, "Confirmado")
        AppointmentStatus.REALIZADO -> Triple(Color(0xFF2196F3), Color.White, "Realizado")
        AppointmentStatus.FALTOU -> Triple(StatusColors.Warning, Color.White, "Faltou")
        AppointmentStatus.CANCELOU -> Triple(StatusColors.Error, Color.White, "Cancelou")
    }
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
} 



