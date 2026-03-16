package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.psipro.app.BuildConfig
import com.psipro.app.sync.api.HandoffCreateRequest
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ponto central para abrir a plataforma Web do PsiPro via Chrome Custom Tabs.
 * SSO: App chama POST /auth/handoff com JWT, recebe redirectUrl com handoff token (30s, single-use).
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

        CoroutineScope(Dispatchers.Main).launch {
            val url = withContext(Dispatchers.IO) {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        context.applicationContext,
                        SyncEntryPoint::class.java
                    )
                    val token = entryPoint.sessionStore().getAccessToken()
                    if (token.isNullOrBlank()) {
                        return@withContext null
                    }
                    val api = entryPoint.backendApiService()
                    val resp = api.handoffCreate(HandoffCreateRequest(token = token, returnUrl = returnPath))
                    if (resp.isSuccessful) resp.body()?.redirectUrl else null
                } catch (_: Exception) {
                    null
                }
            }
            val finalUrl = url ?: loginUrl
            if (url == null) {
                Toast.makeText(context, "Faça login no app para abrir automaticamente na plataforma web.", Toast.LENGTH_LONG).show()
            }
            openUrl(context, finalUrl)
        }
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
