package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.auth.AuthManager
import com.psipro.app.databinding.ActivityMainBinding
import com.psipro.app.utils.AuthErrorHelper
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.HtmlCompat
import dagger.hilt.android.AndroidEntryPoint
import android.os.Build
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity(), AuthManager.AuthStateListener {
    private lateinit var binding: ActivityMainBinding

    private val createAccountLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val email = result.data?.getStringExtra("email") ?: ""
            val password = result.data?.getStringExtra("password") ?: ""
            binding.emailEditText.setText(email)
            binding.passwordEditText.setText(password)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeBasicComponents()
        setupViews()
        initializeHeavyComponents()
    }

    private fun initializeBasicComponents() {
        try {
            AuthManager.init(this)

            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            )

            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = ContextCompat.getColor(this, R.color.background_black)
                    window.navigationBarColor = ContextCompat.getColor(this, R.color.background_black)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro na inicialização básica", e)
        }
    }

    private fun initializeHeavyComponents() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                kotlinx.coroutines.withTimeout(5000) {
                    AuthManager.getInstance().addAuthStateListener(this@MainActivity)
                    kotlinx.coroutines.delay(100)
                }
                if (AuthManager.getInstance().isLoggedIn()) {
                    startDashboard()
                } else {
                    try {
                        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                        val aceitouLgpd = prefs.getBoolean("aceitou_lgpd", false)
                        if (!aceitouLgpd) {
                            val termo = "Este aplicativo coleta e armazena dados pessoais de pacientes para fins de gestão clínica, em conformidade com a LGPD (Lei Geral de Proteção de Dados). Ao prosseguir, você concorda com o tratamento dos dados conforme descrito na nossa política de privacidade. Você pode solicitar a exclusão ou exportação dos seus dados a qualquer momento."
                            androidx.appcompat.app.AlertDialog.Builder(ContextThemeWrapper(this@MainActivity, androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert))
                                .setTitle("Consentimento LGPD")
                                .setMessage(HtmlCompat.fromHtml(termo, HtmlCompat.FROM_HTML_MODE_LEGACY))
                                .setCancelable(false)
                                .setPositiveButton("Aceito") { _, _ ->
                                    prefs.edit().putBoolean("aceitou_lgpd", true).apply()
                                }
                                .show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao mostrar termo LGPD", e)
                    }
                }
                Log.d("MainActivity", "Componentes pesados inicializados")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("MainActivity", "Timeout na inicialização", e)
                Toast.makeText(this@MainActivity, "Inicialização demorou muito, mas o app está funcionando", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Erro na inicialização", e)
                Toast.makeText(this@MainActivity, "Erro na inicialização do app", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupViews() {
        val emailEditText = binding.emailEditText
        val passwordEditText = binding.passwordEditText
        val loginButton = binding.loginButton
        val forgotPasswordButton = binding.forgotPasswordButton
        val googleButton = binding.btnGoogleSignIn
        val createAccountButton = binding.createAccountButton

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!AuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            loginButton.text = "Entrando..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                        applicationContext,
                        com.psipro.app.sync.di.SyncEntryPoint::class.java
                    )
                    val backendAuth = entryPoint.backendAuthManager()
                    val ok = backendAuth.login(email, password)

                    withContext(Dispatchers.Main) {
                        loginButton.isEnabled = true
                        loginButton.text = "Entrar"

                        if (ok) {
                            backendAuth.ensureClinicId()
                            com.psipro.app.sync.work.PatientsSyncScheduler.enqueue(applicationContext, "login")
                            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                            prefs.edit()
                                .putString("current_user_email", email)
                                .putString("profile_name", email.substringBefore("@"))
                                .putString("profile_email", email)
                                .apply()
                            AuthManager.getInstance().notifyLoginSuccess()
                            Toast.makeText(this@MainActivity, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            startDashboard()
                        } else {
                            val errorMsg = "Credenciais inválidas. Verifique seu e-mail e senha."
                            androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                                .setTitle("Erro no Login")
                                .setMessage(errorMsg)
                                .setPositiveButton("Criar Conta") { _, _ ->
                                    createAccountLauncher.launch(Intent(this@MainActivity, CreateAccountActivity::class.java))
                                }
                                .setNegativeButton("OK", null)
                                .show()
                            Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loginButton.isEnabled = true
                        loginButton.text = "Entrar"
                        val msg = AuthErrorHelper.getErrorMessage(e)
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, informe seu e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!AuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Recuperação de senha disponível na plataforma web.", Toast.LENGTH_LONG).show()
        }

        googleButton.setOnClickListener {
            Toast.makeText(this, "Login com Google em breve.", Toast.LENGTH_SHORT).show()
        }

        createAccountButton.setOnClickListener {
            createAccountLauncher.launch(Intent(this, CreateAccountActivity::class.java))
        }
    }

    private fun startDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onAuthStateChanged(isLoggedIn: Boolean) {
        if (isLoggedIn) startDashboard()
    }

    override fun onTokenExpired() {
        Toast.makeText(this, "Sessão expirada. Por favor, faça login novamente.", Toast.LENGTH_LONG).show()
        AuthManager.getInstance().logout()
    }

    override fun onDestroy() {
        super.onDestroy()
        AuthManager.getInstance().removeAuthStateListener(this)
    }
}
