package com.example.apppisc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.apppisc.auth.AuthManager
import com.example.apppisc.databinding.ActivityMainBinding
import com.example.apppisc.ui.AppointmentScheduleActivity
import com.example.apppisc.viewmodel.AuthViewModel
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.HtmlCompat

class MainActivity : AppCompatActivity(), AuthManager.AuthStateListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Registrar para receber callbacks de autenticação
        AuthManager.getInstance().addAuthStateListener(this)

        // Verificar se já está logado
        if (viewModel.isLoggedIn()) {
            startDashboard()
            return
        }

        // Exibir termo LGPD se ainda não aceito
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val aceitouLgpd = prefs.getBoolean("aceitou_lgpd", false)
        if (!aceitouLgpd) {
            val termo = "Este aplicativo coleta e armazena dados pessoais de pacientes para fins de gestão clínica, em conformidade com a LGPD (Lei Geral de Proteção de Dados). Ao prosseguir, você concorda com o tratamento dos dados conforme descrito na nossa política de privacidade. Você pode solicitar a exclusão ou exportação dos seus dados a qualquer momento."
            androidx.appcompat.app.AlertDialog.Builder(ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert))
                .setTitle("Consentimento LGPD")
                .setMessage(HtmlCompat.fromHtml(termo, HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setCancelable(false)
                .setPositiveButton("Aceito") { _, _ ->
                    prefs.edit().putBoolean("aceitou_lgpd", true).apply()
                }
                .show()
        }

        setupViews()
    }

    private fun setupViews() {
        val emailEditText = binding.emailEditText
        val passwordEditText = binding.passwordEditText
        val loginButton = binding.loginButton
        val forgotPasswordButton = binding.forgotPasswordButton

        // Preencher campos de teste
        emailEditText.setText("admin@teste.com")
        passwordEditText.setText("123456")

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password) { success ->
                    if (success) {
                        startDashboard()
                    } else {
                        Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, informe seu e-mail", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Instruções enviadas para seu e-mail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onAuthStateChanged(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            startDashboard()
        }
    }

    override fun onTokenExpired() {
        Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
        viewModel.logout()
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthManager.getInstance().removeAuthStateListener(this)
    }
} 