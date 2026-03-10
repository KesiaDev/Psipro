package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.psipro.app.BuildConfig
import com.psipro.app.R
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Ponto central para abrir a plataforma Web do PsiPro via Chrome Custom Tabs.
 * Usa accessToken do backend (JWT) para handoff.
 * Custom Tabs evita 404 na primeira abertura (pre-warm) e oferece experiência mais estável.
 */
object WebNavigator {

    private val BASE_URL: String get() = BuildConfig.PSIPRO_WEB_BASE_URL

    fun openDashboard(context: Context) {
        openWithSso(context, "/dashboard")
    }

    /** Abre a ficha do paciente no dashboard web. */
    fun openPatientOnWeb(context: Context, patientId: Long) {
        openWithSso(context, "/patients/$patientId")
    }

    fun openFinancialOnWeb(context: Context) {
        openWithSso(context, "/financeiro")
    }

    /** Abre a página de relatórios na plataforma web. */
    fun openRelatoriosOnWeb(context: Context) {
        openWithSso(context, "/relatorios")
    }

    private fun openWithSso(context: Context, returnPath: String) {
        val base = BASE_URL.trim().trimEnd('/')
        val loginUrl = "$base/login"
        val url: String = try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SyncEntryPoint::class.java
            )
            val token = entryPoint.sessionStore().getAccessToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(context, "Faça login no app para abrir automaticamente na plataforma web.", Toast.LENGTH_LONG).show()
                loginUrl
            } else {
                "$loginUrl?token=${Uri.encode(token)}&returnUrl=${Uri.encode(returnPath)}"
            }
        } catch (e: Exception) {
            loginUrl
        }
        openUrl(context, url)
    }

    private fun openUrl(context: Context, url: String) {
        Toast.makeText(context, "Abrindo a plataforma web…", Toast.LENGTH_SHORT).show()
        val uri = Uri.parse(url)
        try {
            val builder = CustomTabsIntent.Builder().apply {
                setShowTitle(true)
                try {
                    setToolbarColor(android.graphics.Color.parseColor("#C89B3C"))
                } catch (_: Exception) { }
            }
            builder.build().launchUrl(context, uri)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
