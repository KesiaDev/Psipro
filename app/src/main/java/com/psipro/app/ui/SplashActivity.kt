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

                val meOk = try {
                    val resp = entryPoint.backendApiService().me()
                    resp.isSuccessful
                } catch (_: Exception) { false }

                if (meOk) {
                    authManager.ensureClinicId()
                    runOnUiThread { goToDashboard() }
                    return@withContext
                }

                val refreshOk = authManager.refreshToken()
                if (refreshOk) {
                    authManager.ensureClinicId()
                    runOnUiThread { goToDashboard() }
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

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
