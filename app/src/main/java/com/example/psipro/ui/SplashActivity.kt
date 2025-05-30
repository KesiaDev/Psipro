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

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    
    @Inject
    lateinit var appConfig: AppConfig
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (appConfig.isFirstRun) {
                Intent(this, OnboardingActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000) // 2 segundos de delay
    }
} 