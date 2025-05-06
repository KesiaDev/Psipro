package com.example.apppisc.utils

import android.content.Context
import android.content.SharedPreferences

object MessageTemplateManager {
    private const val PREFS_NAME = "message_templates"
    private const val KEY_TEMPLATES = "templates"

    fun getTemplates(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_TEMPLATES, null)
        return set?.toList() ?: defaultTemplates()
    }

    fun saveTemplates(context: Context, templates: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_TEMPLATES, templates.toSet()).apply()
    }

    private fun defaultTemplates(): List<String> = listOf(
        "Olá {nome}, lembrete da sua consulta amanhã às {hora}.",
        "Bom dia {nome}! Só confirmando sua presença na consulta de {data}.",
        "Se precisar remarcar, me avise com antecedência."
    )

    fun fillTemplate(template: String, variables: Map<String, String>): String {
        var result = template
        for ((key, value) in variables) {
            result = result.replace("{$key}", value)
        }
        return result
    }
} 