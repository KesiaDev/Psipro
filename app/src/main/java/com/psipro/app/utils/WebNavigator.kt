package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Ponto central para abrir a plataforma Web do PsiPro via navegador externo (SSO).
 * Usa accessToken do backend (JWT) para handoff.
 */
object WebNavigator {

    private const val BASE_URL = "https://triumphant-perception-production-8792.up.railway.app"

    fun openDashboard(context: Context) {
        openWithSso(context, "/dashboard")
    }

    private fun openWithSso(context: Context, returnPath: String) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SyncEntryPoint::class.java
            )
            val token = entryPoint.sessionStore().getAccessToken()
            if (token.isNullOrBlank()) {
                openUrl(context, "$BASE_URL/login")
                return
            }

            val handoffUrl =
                "$BASE_URL/handoff?token=${Uri.encode(token)}&returnUrl=${Uri.encode(returnPath)}"
            openUrl(context, handoffUrl)
        } catch (e: Exception) {
            openUrl(context, "$BASE_URL/login")
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
