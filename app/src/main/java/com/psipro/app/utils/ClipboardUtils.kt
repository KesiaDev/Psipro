package com.psipro.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtils {
    fun copyToClipboard(context: Context, text: String, label: String = "PIX") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(context, "Copiado para a área de transferência", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao copiar", Toast.LENGTH_SHORT).show()
        }
    }
}
