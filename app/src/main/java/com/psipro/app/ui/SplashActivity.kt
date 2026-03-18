package com.psipro.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.psipro.app.DashboardActivity
import com.psipro.app.MainActivity
import com.psipro.app.R
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Rodar em IO para não bloquear a main thread (EncryptedSharedPreferences, rede)
        lifecycleScope.launch {
            delay(300)
            withContext(Dispatchers.IO) {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    SyncEntryPoint::class.java
                )
                val authManager = entryPoint.backendAuthManager()

                if (!authManager.isBackendAuthenticated()) {
                    runOnUiThread { goToLogin() }
                    return@withContext
                }

                var meOk = false
                var meResponse: retrofit2.Response<com.psipro.app.sync.api.BackendMeResponse>? = null
                try {
                    val resp = entryPoint.backendApiService().me()
                    meOk = resp.isSuccessful
                    meResponse = resp
                } catch (_: Exception) { }

                if (!meOk) {
                    val refreshOk = authManager.refreshToken()
                    if (refreshOk) {
                        meResponse = try { entryPoint.backendApiService().me() } catch (_: Exception) { null }
                    } else {
                        authManager.logout()
                        runOnUiThread { goToLogin() }
                        return@withContext
                    }
                }

                if (meResponse?.isSuccessful == true) {
                    authManager.ensureClinicId()
                    val me = meResponse.body()
                    if (me?.lgpdAcceptedAt != null) {
                        authManager.setLgpdConsentLocal(true)
                    }
                    if (authManager.hasLgpdConsent()) {
                        runOnUiThread { goToDashboard() }
                    } else {
                        runOnUiThread { goToLgpdConsent() }
                    }
                } else {
                    authManager.logout()
                    runOnUiThread { goToLogin() }
                }
            }
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun goToLgpdConsent() {
        startActivity(Intent(this, LgpdConsentActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
