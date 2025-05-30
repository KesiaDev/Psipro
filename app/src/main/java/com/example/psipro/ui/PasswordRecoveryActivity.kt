package com.example.psipro.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.psipro.auth.PasswordRecoveryService
import com.example.psipro.databinding.ActivityPasswordRecoveryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.psipro.R

@AndroidEntryPoint
class PasswordRecoveryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPasswordRecoveryBinding
    
    @Inject
    lateinit var passwordRecoveryService: PasswordRecoveryService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordRecoveryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.password_recovery)
    }
    
    private fun setupListeners() {
        binding.recoverButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isBlank()) {
                binding.emailEditText.error = getString(R.string.fill_email)
                return@setOnClickListener
            }
            
            requestPasswordRecovery(email)
        }
    }
    
    private fun requestPasswordRecovery(email: String) {
        lifecycleScope.launch {
            binding.progressBar.show()
            binding.recoverButton.isEnabled = false
            
            passwordRecoveryService.requestPasswordRecovery(email)
                .onSuccess {
                    Toast.makeText(
                        this@PasswordRecoveryActivity,
                        R.string.recovery_email_sent,
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@PasswordRecoveryActivity,
                        error.message ?: getString(R.string.error_recovery),
                        Toast.LENGTH_LONG
                    ).show()
                }
            
            binding.progressBar.hide()
            binding.recoverButton.isEnabled = true
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 