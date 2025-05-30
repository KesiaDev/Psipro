package com.example.psipro.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.psipro.databinding.ActivitySettingsBinding
import android.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurações"

        prefs = PreferenceManager.getDefaultSharedPreferences(this)

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