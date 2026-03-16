package com.psipro.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.psipro.app.DashboardActivity
import com.psipro.app.databinding.ActivityBackendLoginBinding
import com.psipro.app.viewmodel.BackendLoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Tela de login do backend. LAUNCHER - app inicia aqui.
 * Fluxo: BackendLoginActivity → Dashboard.
 * Sem ANR: login e I/O em viewModelScope.launch(Dispatchers.IO).
 */
@AndroidEntryPoint
class BackendLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBackendLoginBinding
    private val viewModel: BackendLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackendLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        collectState()
    }

    private fun setupUi() {
        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text?.toString()?.trim().orEmpty()
            val password = binding.editPassword.text?.toString().orEmpty()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is BackendLoginViewModel.LoginState.Idle -> {
                        binding.loadingProgress.isVisible = false
                        binding.btnLogin.isEnabled = true
                    }
                    is BackendLoginViewModel.LoginState.Loading -> {
                        binding.loadingProgress.isVisible = true
                        binding.btnLogin.isEnabled = false
                    }
                    is BackendLoginViewModel.LoginState.Success -> {
                        binding.loadingProgress.isVisible = false
                        binding.btnLogin.isEnabled = true
                        startDashboard()
                    }
                    is BackendLoginViewModel.LoginState.Error -> {
                        binding.loadingProgress.isVisible = false
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(
                            this@BackendLoginActivity,
                            state.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun startDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
