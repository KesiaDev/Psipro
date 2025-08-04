package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.psipro.app.auth.AuthManager
import com.psipro.app.databinding.ActivityMainBinding
import com.psipro.app.ui.AppointmentScheduleActivity
import com.psipro.app.viewmodel.AuthViewModel
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.HtmlCompat
import android.view.Menu
import android.view.MenuItem
import androidx.biometric.BiometricManager
import com.psipro.app.ui.PatientListActivity
import com.psipro.app.ui.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import android.os.Build
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), AuthManager.AuthStateListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AuthViewModel

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar componentes básicos primeiro
        initializeBasicComponents()
        
        // Configurar UI
        setupViews()
        
        // Inicializar componentes pesados de forma assíncrona
        initializeHeavyComponents()
    }
    
    private fun initializeBasicComponents() {
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Aplicar tema salvo
        val themePrefs = getSharedPreferences("settings", MODE_PRIVATE)
        when (themePrefs.getString("app_theme", "light")) {
            "dark" -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Configurar cores da barra de status
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.background_black)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.background_black)
            }
        }
    }
    
    private fun initializeHeavyComponents() {
        // Executar operações pesadas de forma assíncrona
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Configuração do Google Sign-In
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("231873451192-ai0rpmu8iumkk4lo9sbcbg0b4tvbtald.apps.googleusercontent.com")
                    .requestEmail()
                    .build()
                googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                
                // Registrar para receber callbacks de autenticação
                AuthManager.getInstance().addAuthStateListener(this@MainActivity)
                
                // Verificar se já está logado
                if (viewModel.isLoggedIn()) {
                    withContext(Dispatchers.Main) {
                        startDashboard()
                    }
                    return@launch
                }
                
                // Exibir termo LGPD se ainda não aceito
                withContext(Dispatchers.Main) {
                    val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
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
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro na inicialização", e)
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

        // Remover preenchimento automático de teste
        // emailEditText.setText("")
        // passwordEditText.setText("")

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                        startDashboard()
                    } else {
                            Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, informe seu e-mail", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                Toast.makeText(this, "Instruções enviadas para seu e-mail", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro ao enviar e-mail: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        googleButton.setOnClickListener {
            try {
                Log.d("GoogleSignIn", "Iniciando processo de login Google")
                
                // Verificar conectividade
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo == null || !networkInfo.isConnected) {
                    Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                
                // Limpar cache do Google Sign-In
                googleSignInClient.signOut().addOnCompleteListener {
                    Log.d("GoogleSignIn", "Sign out completado")
                    
                    // Tentar login direto
                    val signInIntent = googleSignInClient.signInIntent
                    Log.d("GoogleSignIn", "Intent criado: ${signInIntent.action}")
                    
                    try {
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Erro ao iniciar activity", e)
                        Toast.makeText(this, "Erro ao abrir login Google: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Erro ao iniciar login Google", e)
                Toast.makeText(this, "Erro ao iniciar login: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        createAccountButton.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        // Preencher e-mail e senha após cadastro
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val email = data.getStringExtra("email") ?: ""
            val password = data.getStringExtra("password") ?: ""
            binding.emailEditText.setText(email)
            binding.passwordEditText.setText(password)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            Log.d("GoogleSignIn", "Iniciando handleSignInResult")
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("GoogleSignIn", "Conta obtida: ${account.email}")
            
            // Autenticar com Firebase usando o token do Google
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("GoogleSignIn", "Credential criada, autenticando com Firebase")
            
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("GoogleSignIn", "Login Firebase bem-sucedido")
                        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                        val hasName = prefs.contains("profile_name")
                        val hasPhoto = prefs.contains("profile_photo_path")
                        if (!hasName) {
                            prefs.edit().putString("profile_name", account.displayName ?: "").apply()
                        }
                        // Salvar a URL da foto do Google apenas se não houver foto local salva
                        if (!hasPhoto && account.photoUrl != null) {
                            prefs.edit().putString("profile_photo_url", account.photoUrl.toString()).apply()
                        }
                        Toast.makeText(this, "Bem-vindo(a), ${account.displayName}", Toast.LENGTH_SHORT).show()
                        startDashboard()
                    } else {
                        Log.e("GoogleSignIn", "Erro no Firebase: ${task.exception?.message}")
                        Toast.makeText(this, "Erro na autenticação: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Erro ApiException: ${e.statusCode} - ${e.message}")
            when (e.statusCode) {
                10 -> {
                    Log.e("GoogleSignIn", "Erro 10 detectado - Problema de configuração")
                    // Tentar solução alternativa
                    showError10Solution()
                }
                12501 -> Toast.makeText(this, "Login cancelado pelo usuário", Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(this, "Erro ao fazer login com Google: ${e.statusCode} - ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Erro geral: ${e.message}")
            Toast.makeText(this, "Erro inesperado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showError10Solution() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Erro de Configuração Google")
            .setMessage("O erro 10 indica um problema de configuração. Tente:\n\n" +
                    "1. Limpar cache do Google Play Services\n" +
                    "2. Verificar conexão com internet\n" +
                    "3. Tentar em outro dispositivo\n\n" +
                    "Deseja tentar login com e-mail/senha?")
            .setPositiveButton("Sim, usar e-mail") { _, _ ->
                // Focar no campo de e-mail
                binding.emailEditText.requestFocus()
            }
            .setNegativeButton("Tentar novamente") { _, _ ->
                // Tentar login Google novamente
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            }
            .setNeutralButton("Cancelar", null)
            .show()
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



