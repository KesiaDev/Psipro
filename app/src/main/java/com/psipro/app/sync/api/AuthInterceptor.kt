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

        // Endpoints públicos
        if (path.endsWith("/auth/login")) {
            return chain.proceed(request)
        }

        val token = sessionStore.getAccessToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticated = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authenticated)
    }
}

