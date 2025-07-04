package com.example.psipro.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.psipro.data.entities.StatusPagamento
import androidx.compose.foundation.layout.padding

@Composable
fun StatusChip(status: StatusPagamento, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor, text) = when (status) {
        StatusPagamento.PAGO -> Triple(Color(0xFF4CAF50), Color.White, "Pago")
        StatusPagamento.A_RECEBER -> Triple(Color(0xFFFF9800), Color.White, "A Receber")
        StatusPagamento.VENCIDO -> Triple(Color(0xFFF44336), Color.White, "Vencido")
        StatusPagamento.CANCELADO -> Triple(Color(0xFF9E9E9E), Color.White, "Cancelado")
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