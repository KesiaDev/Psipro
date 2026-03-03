package com.psipro.app.sync

import android.util.Log
import com.psipro.app.sync.api.BackendApiService
import com.psipro.app.sync.api.BackendLoginRequest
import com.psipro.app.sync.api.BackendLogoutRequest
import com.psipro.app.sync.api.BackendRefreshRequest
import com.psipro.app.sync.api.BackendSwitchClinicRequest
import com.psipro.app.sync.api.BackendRegisterRequest
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
                store.setAccessToken(body.effectiveAccessToken())
                body.refreshToken?.let { store.setRefreshToken(it) }
                Log.i(TAG, "Backend login ok")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend login exception", e)
            false
        }
    }

    suspend fun register(email: String, password: String, fullName: String): Boolean {
        return try {
            val resp = api.register(BackendRegisterRequest(email = email, password = password, fullName = fullName))
            if (!resp.isSuccessful || resp.body() == null) {
                Log.w(TAG, "Backend register failed: http=${resp.code()}")
                false
            } else {
                val body = resp.body()!!
                store.setAccessToken(body.effectiveAccessToken())
                body.refreshToken?.let { store.setRefreshToken(it) }
                Log.i(TAG, "Backend register ok")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backend register exception", e)
            false
        }
    }

    suspend fun refreshToken(): Boolean {
        val refresh = store.getRefreshToken()
        if (refresh.isNullOrBlank()) {
            Log.w(TAG, "No refresh token")
            return false
        }
        return try {
            val resp = api.refresh(BackendRefreshRequest(refreshToken = refresh))
            if (!resp.isSuccessful || resp.body() == null) {
                Log.w(TAG, "Refresh failed: http=${resp.code()}")
                false
            } else {
                val body = resp.body()!!
                store.setAccessToken(body.effectiveAccessToken())
                body.refreshToken?.let { store.setRefreshToken(it) }
                Log.i(TAG, "Refresh ok")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Refresh exception", e)
            false
        }
    }

    suspend fun logout() {
        val refresh = store.getRefreshToken()
        if (!refresh.isNullOrBlank()) {
            try {
                api.logout(BackendLogoutRequest(refreshToken = refresh))
            } catch (e: Exception) {
                Log.w(TAG, "Backend logout failed (proceeding with local clear)", e)
            }
        }
        store.clear()
        Log.i(TAG, "Logout completed")
    }

    suspend fun switchClinic(clinicId: String): Boolean {
        return try {
            val resp = api.switchClinic(BackendSwitchClinicRequest(clinicId = clinicId))
            if (!resp.isSuccessful || resp.body() == null) {
                Log.w(TAG, "Switch clinic failed: http=${resp.code()}")
                false
            } else {
                val body = resp.body()!!
                store.setAccessToken(body.effectiveAccessToken())
                store.setClinicId(body.clinicId ?: clinicId)
                Log.i(TAG, "Switch clinic ok: $clinicId")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Switch clinic exception", e)
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

