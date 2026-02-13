package com.psipro.app.sync

import android.util.Log
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.BackendLoginRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendAuthManager @Inject constructor(
    private val api: BackendApiService,
    private val store: BackendSessionStore
) {
    suspend fun login(email: String, password: String): Boolean {
        return try {
            val resp = api.login(BackendLoginRequest(email = email, password = password))
            if (!resp.isSuccessful || resp.body() == null) {
                Log.w(TAG, "Backend login failed: http=${resp.code()}")
                false
            } else {
                val body = resp.body()!!
                store.setAccessToken(body.accessToken)
                Log.i(TAG, "Backend login ok")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend login exception", e)
            false
        }
    }

    suspend fun ensureClinicId(): String? {
        val cached = store.getClinicId()
        if (!cached.isNullOrBlank()) return cached

        val token = store.getAccessToken()
        if (token.isNullOrBlank()) return null

        return try {
            val resp = api.me()
            if (!resp.isSuccessful || resp.body() == null) {
                Log.w(TAG, "Backend /auth/me failed: http=${resp.code()}")
                null
            } else {
                val me = resp.body()!!
                store.setClinicId(me.clinicId)
                me.clinicId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend /auth/me exception", e)
            null
        }
    }

    fun isBackendAuthenticated(): Boolean = !store.getAccessToken().isNullOrBlank()

    companion object {
        private const val TAG = "BackendAuthManager"
    }
}

