package com.example.psipro.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.psipro.databinding.ActivityPrivacyPolicyBinding
import com.example.psipro.R

class PrivacyPolicyActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPrivacyPolicyBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadPrivacyPolicy()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.privacy_policy)
    }
    
    private fun loadPrivacyPolicy() {
        val inputStream = resources.openRawResource(R.raw.privacy_policy)
        val htmlContent = inputStream.bufferedReader().use { it.readText() }
        binding.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 