package com.psipro.app.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurações"

        // Usar getSharedPreferences ao invés de PreferenceManager
        prefs = getSharedPreferences("settings", MODE_PRIVATE)

        // Carregar texto padrão salvo
        val textoPadrao = prefs.getString("whatsapp_reminder_text", "Olá, lembramos que você tem uma consulta agendada para o dia {data} às {hora}.")
        binding.editTextWhatsappReminder.setText(textoPadrao)

        binding.buttonSalvar.setOnClickListener {
            val novoTexto = binding.editTextWhatsappReminder.text.toString()
            prefs.edit().putString("whatsapp_reminder_text", novoTexto).apply()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
} 



