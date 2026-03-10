package com.psipro.app.utils

/**
 * Mapeamento entre valores do backend e rótulos em português para tipos de profissional.
 * Compatibilidade: null ou valor desconhecido retorna "Psicólogo".
 */
object ProfessionalTypeHelper {
    val BACKEND_VALUES = listOf(
        "psychologist", "therapist", "psychoanalyst", "counselor", "coach", "other"
    )

    val DISPLAY_LABELS = mapOf(
        "psychologist" to "Psicólogo",
        "therapist" to "Terapeuta",
        "psychoanalyst" to "Psicanalista",
        "counselor" to "Conselheiro",
        "coach" to "Coach",
        "other" to "Outro"
    )

    val DISPLAY_TO_BACKEND = mapOf(
        "Psicólogo" to "psychologist",
        "Terapeuta" to "therapist",
        "Psicanalista" to "psychoanalyst",
        "Conselheiro" to "counselor",
        "Coach" to "coach",
        "Outro" to "other"
    )

    fun toDisplayLabel(backendValue: String?): String =
        if (backendValue != null && DISPLAY_LABELS.containsKey(backendValue))
            DISPLAY_LABELS[backendValue]!!
        else "Psicólogo"

    fun toBackendValue(displayLabel: String): String =
        DISPLAY_TO_BACKEND[displayLabel] ?: "psychologist"

    fun getDropdownOptions(): List<String> = DISPLAY_LABELS.values.toList()
}
