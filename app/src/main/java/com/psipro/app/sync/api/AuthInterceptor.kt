package com.psipro.app.sync.api

import com.psipro.app.sync.BackendSessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionStore: BackendSessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // Endpoints públicos (não enviam Authorization)
        if (path.endsWith("/auth/login") || path.endsWith("/auth/register") ||
            path.endsWith("/auth/refresh") || path.endsWith("/auth/logout") ||
            path.endsWith("/system-health")) {
            return chain.proceed(request)
        }

        val token = sessionStore.getAccessToken()
        val clinicId = sessionStore.getClinicId()

        val newRequest = request.newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            if (!clinicId.isNullOrBlank()) {
                addHeader("X-Clinic-Id", clinicId)
            }
        }.build()
        return chain.proceed(newRequest)
    }
}

