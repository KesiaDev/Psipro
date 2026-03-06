package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.psipro.app.BuildConfig
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Ponto central para abrir a plataforma Web do PsiPro via navegador externo (SSO).
 * Usa accessToken do backend (JWT) para handoff.
 * URL configurável em BuildConfig.PSIPRO_WEB_BASE_URL (build.gradle).
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
        openWithSso(context, "/financial")
    }

    private fun openWithSso(context: Context, returnPath: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SyncEntryPoint::class.java
            )
            val token = entryPoint.sessionStore().getAccessToken()
            if (token.isNullOrBlank()) {
                openUrl(context, "${BASE_URL.trim().trimEnd('/')}/login")
                return
            }

            // Usar /login com token e returnUrl (a rota /handoff retorna 404 em produção)
            val base = BASE_URL.trim().trimEnd('/')
            val handoffUrl =
                "$base/login?token=${Uri.encode(token)}&returnUrl=${Uri.encode(returnPath)}"
            openUrl(context, handoffUrl)
        } catch (e: Exception) {
            openUrl(context, "${BASE_URL.trim().trimEnd('/')}/login")
        }
    }

    private fun openUrl(context: Context, url: String) {
        Toast.makeText(context, "Abrindo a plataforma web…", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
