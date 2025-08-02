package com.psipro.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.config.AppConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.psipro.app.R
import com.psipro.app.MainActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var appConfig: AppConfig
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                // Reduzir delay para evitar ANR
                delay(1000) // 1 segundo
                val isFirstRun = withContext(Dispatchers.IO) { appConfig.isFirstRun }
                val intent = if (isFirstRun) {
                    Intent(this@SplashActivity, OnboardingActivity::class.java)
                } else {
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                // Em caso de erro, ir direto para MainActivity
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
} 



