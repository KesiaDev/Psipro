package com.psipro.app.sync.api

import android.content.Intent
import com.psipro.app.App
import com.psipro.app.MainActivity
import com.psipro.app.sync.BackendAuthManager
import com.psipro.app.sync.BackendSessionStore
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private const val HEADER_IS_RETRY = "X-Psipro-Retry"

/**
 * Interceptor que trata 401: tenta refresh, retenta a requisição uma única vez,
 * ou limpa tokens e redireciona para LoginActivity.
 * Evita múltiplos refreshes simultâneos com lock.
 * Usa Lazy para quebrar ciclo BackendAuthManager <-> BackendApiService.
 */
class RefreshInterceptor @Inject constructor(
    private val sessionStore: BackendSessionStore,
    private val authManager: Lazy<BackendAuthManager>
) : Interceptor {

    private val lock = Object()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val isRetry = request.header(HEADER_IS_RETRY) == "true"

        val response = chain.proceed(request)

        if (response.code != 401) return response

        val auth = authManager.get()
        if (path.endsWith("/auth/refresh")) {
            runBlocking { auth.logout() }
            redirectToLogin()
            return response
        }

        if (isRetry) {
            runBlocking { auth.logout() }
            redirectToLogin()
            return response
        }

        synchronized(lock) {
            val success = runBlocking { auth.refreshToken() }
            if (!success) {
                runBlocking { auth.logout() }
                redirectToLogin()
                return response
            }
            val token = sessionStore.getAccessToken()
            if (token.isNullOrBlank()) {
                runBlocking { auth.logout() }
                redirectToLogin()
                return response
            }
            val newRequest = request.newBuilder()
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer $token")
                .header(HEADER_IS_RETRY, "true")
                .build()
            return chain.proceed(newRequest)
        }
    }

    private fun redirectToLogin() {
        try {
            val intent = Intent(App.instance, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            App.instance.startActivity(intent)
        } catch (_: Exception) { }
    }
}
