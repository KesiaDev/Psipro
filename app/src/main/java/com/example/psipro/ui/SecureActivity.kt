package com.example.psipro.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.example.psipro.MainActivity
import com.example.psipro.ui.BiometricHelper

abstract class SecureActivity : AppCompatActivity() {
    private val logoutDelayMillis = 5 * 60 * 1000L // 5 minutos
    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable { doLogout() }
    private var isAuthenticated = false

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetLogoutTimer()
    }

    override fun onResume() {
        super.onResume()
        if (!isAuthenticated && BiometricHelper.isBiometricAvailable(this)) {
            BiometricHelper.authenticate(
                activity = this,
                onSuccess = {
                    isAuthenticated = true
                },
                onError = { errMsg ->
                    finish()
                }
            )
        } else {
            resetLogoutTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(logoutRunnable)
    }

    private fun resetLogoutTimer() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, logoutDelayMillis)
    }

    private fun doLogout() {
        // Limpe dados de sessão, se necessário
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 