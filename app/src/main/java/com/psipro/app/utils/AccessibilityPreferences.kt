package com.psipro.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit

/**
 * Preferências de acessibilidade para usuários com baixa visão ou cegos.
 * Aplicadas em toda a interface (Compose e XML).
 */
object AccessibilityPreferences {

    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_LARGE_FONT = "large_font"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    private const val KEY_LARGER_BUTTONS = "larger_buttons"
    private const val KEY_INCREASED_SPACING = "increased_spacing"

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLargeFont(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_LARGE_FONT, false)

    fun setLargeFont(context: Context, value: Boolean) {
        getPrefs(context).edit(commit = true) { putBoolean(KEY_LARGE_FONT, value) }
    }

    fun getHighContrast(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_HIGH_CONTRAST, false)

    fun setHighContrast(context: Context, value: Boolean) {
        getPrefs(context).edit(commit = true) { putBoolean(KEY_HIGH_CONTRAST, value) }
    }

    fun getLargerButtons(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_LARGER_BUTTONS, false)

    fun setLargerButtons(context: Context, value: Boolean) {
        getPrefs(context).edit(commit = true) { putBoolean(KEY_LARGER_BUTTONS, value) }
    }

    fun getIncreasedSpacing(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_INCREASED_SPACING, false)

    fun setIncreasedSpacing(context: Context, value: Boolean) {
        getPrefs(context).edit(commit = true) { putBoolean(KEY_INCREASED_SPACING, value) }
    }

    /** Escala de fonte para modo baixa visão (1.0 = normal, 1.3 = ampliado). */
    fun getFontScaleMultiplier(context: Context): Float =
        when {
            getLargeFont(context) -> 1.3f
            else -> 1f
        }

    /** Retorna true se pelo menos uma opção de acessibilidade está ativa. */
    fun hasAnyEnabled(context: Context): Boolean =
        getLargeFont(context) || getHighContrast(context) ||
            getLargerButtons(context) || getIncreasedSpacing(context)
}
