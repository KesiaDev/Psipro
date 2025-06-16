package com.example.psipro.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.psipro.config.AppConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.psipro.R
import com.example.psipro.MainActivity
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
            delay(2000) // 2 segundos
            val isFirstRun = withContext(Dispatchers.IO) { appConfig.isFirstRun }
            val intent = if (isFirstRun) {
                Intent(this@SplashActivity, OnboardingActivity::class.java)
            } else {
                Intent(this@SplashActivity, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
} 