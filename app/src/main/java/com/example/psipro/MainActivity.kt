package com.example.psipro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.psipro.auth.AuthManager
import com.example.psipro.databinding.ActivityMainBinding
import com.example.psipro.ui.AppointmentScheduleActivity
import com.example.psipro.viewmodel.AuthViewModel
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.text.HtmlCompat
import android.view.Menu
import android.view.MenuItem
import androidx.biometric.BiometricManager
import com.example.psipro.ui.PatientListActivity
import com.example.psipro.ui.BiometricHelper
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

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.background_black)
                window.navigationBarColor = ContextCompat.getColor(this, R.color.background_black)
            }
        }

        setupViews()
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
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
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
            val account = completedTask.getResult(ApiException::class.java)
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
        } catch (e: ApiException) {
            Toast.makeText(this, "Erro ao fazer login com Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
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