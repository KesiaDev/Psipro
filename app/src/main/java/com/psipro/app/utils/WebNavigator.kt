package com.psipro.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

/**
 * Ponto central para abrir a plataforma Web do PsiPro via navegador externo (SSO).
 *
 * Regras:
 * - Nunca usar WebView
 * - Usar Intent.ACTION_VIEW
 * - Se houver usuário logado: abrir /handoff com token + returnUrl
 * - Se não houver token/usuário: abrir /login
 */
object WebNavigator {

    // Domínio público do Web (Railway)
    private const val BASE_URL = "https://triumphant-perception-production-8792.up.railway.app"

    fun openDashboard(context: Context) {
        openWithSso(context, "/dashboard")
    }

    private fun openWithSso(context: Context, returnPath: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            openUrl(context, "$BASE_URL/login")
            return
        }

        // Buscar token do usuário logado (Firebase ID Token).
        // Não altera o fluxo de login; apenas usa o token para handoff no web.
        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val token = result.token
                if (token.isNullOrBlank()) {
                    openUrl(context, "$BASE_URL/login")
                    return@addOnSuccessListener
                }

                val handoffUrl =
                    "$BASE_URL/handoff?token=${Uri.encode(token)}&returnUrl=${Uri.encode(returnPath)}"
                openUrl(context, handoffUrl)
            }
            .addOnFailureListener {
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

