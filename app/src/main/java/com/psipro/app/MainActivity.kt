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
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
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
import androidx.activity.result.contract.ActivityResultContracts
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class MainActivity : AppCompatActivity(), AuthManager.AuthStateListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: AuthViewModel

    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    
    // Activity Result Launchers (substituindo startActivityForResult)
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            Log.d("GoogleSignIn", "Callback recebido - ResultCode: ${result.resultCode}, Data: ${result.data != null}")
            
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    Log.d("GoogleSignIn", "Processando resultado do login...")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    Log.d("GoogleSignIn", "Task obtida, isComplete: ${task.isComplete}, isSuccessful: ${task.isSuccessful}")
                    handleSignInResult(task)
                } else {
                    Log.e("GoogleSignIn", "Result data é null!")
                    Toast.makeText(this, "Erro: dados de login não recebidos", Toast.LENGTH_LONG).show()
                }
            } else if (result.resultCode == RESULT_CANCELED) {
                Log.d("GoogleSignIn", "Login cancelado pelo usuário (código: ${result.resultCode})")
                Log.w("GoogleSignIn", "⚠️ RESULT_CANCELED - Possíveis causas:")
                Log.w("GoogleSignIn", "1. SHA-1 não configurado no Google Cloud Console OAuth Client")
                Log.w("GoogleSignIn", "2. Google Sign-In não habilitado no Firebase Authentication")
                Log.w("GoogleSignIn", "3. Client ID incorreto ou não corresponde ao SHA-1")
                Log.w("GoogleSignIn", "4. Aguardar propagação (pode levar até 10 minutos)")
                
                // Verificar se há dados mesmo com RESULT_CANCELED (às vezes o Google retorna assim mesmo com sucesso)
                if (result.data != null) {
                    Log.d("GoogleSignIn", "Tentando processar mesmo com RESULT_CANCELED (pode ser um falso negativo)")
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        if (task.isSuccessful) {
                            Log.d("GoogleSignIn", "Task foi bem-sucedida mesmo com RESULT_CANCELED")
                            handleSignInResult(task)
                        } else {
                            Log.d("GoogleSignIn", "Task não foi bem-sucedida, realmente cancelado")
                            val exception = task.exception
                            if (exception is ApiException) {
                                Log.e("GoogleSignIn", "ApiException: statusCode=${exception.statusCode}, message=${exception.message}")
                                when (exception.statusCode) {
                                    10 -> {
                                        Log.e("GoogleSignIn", "Erro 10: DEVELOPER_ERROR - Verifique SHA-1 no Google Cloud Console")
                                        showError10Solution()
                                    }
                                    12501 -> {
                                        Log.d("GoogleSignIn", "Erro 12501: Usuário cancelou explicitamente")
                                    }
                                    else -> {
                                        Log.e("GoogleSignIn", "Erro desconhecido: ${exception.statusCode}")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Erro ao processar com RESULT_CANCELED: ${e.message}", e)
                    }
                } else {
                    Log.w("GoogleSignIn", "Result data é null - login foi realmente cancelado")
                }
                // Não mostrar toast para cancelamento explícito
            } else {
                // Outro código de resultado
                Log.e("GoogleSignIn", "Resultado não OK: ${result.resultCode}")
                Log.e("GoogleSignIn", "RESULT_OK = $RESULT_OK, RESULT_CANCELED = $RESULT_CANCELED")
                
                // Tentar processar mesmo assim se houver dados
                if (result.data != null) {
                    Log.d("GoogleSignIn", "Tentando processar com código ${result.resultCode}")
                    try {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        handleSignInResult(task)
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Erro ao processar resultado", e)
                        Toast.makeText(this, "Erro ao fazer login: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Erro ao fazer login com Google (código: ${result.resultCode}). Verifique a configuração do Firebase.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Erro ao processar resultado do login", e)
            e.printStackTrace()
            Toast.makeText(this, "Erro ao processar login: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
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

        // Inicializar componentes básicos primeiro
        initializeBasicComponents()
        
        // Configurar UI
        setupViews()
        
        // Inicializar componentes pesados de forma assíncrona
        initializeHeavyComponents()
    }
    
    private fun initializeBasicComponents() {
        try {
            // Inicializar AuthManager primeiro
            AuthManager.init(this)
            
            // Aplicar tema salvo (operação rápida)
            val themePrefs = getSharedPreferences("settings", MODE_PRIVATE)
            when (themePrefs.getString("app_theme", "light")) {
                "dark" -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
                "light" -> androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
            
            // Configurar cores da barra de status (operação rápida)
            val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.statusBarColor = ContextCompat.getColor(this, R.color.background_black)
                    window.navigationBarColor = ContextCompat.getColor(this, R.color.background_black)
                }
            }
            
            // Inicializar ViewModel de forma assíncrona
            lifecycleScope.launch(Dispatchers.Main) {
                try {
                    viewModel = ViewModelProvider(this@MainActivity)[AuthViewModel::class.java]
                    Log.d("MainActivity", "ViewModel inicializado com sucesso")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Erro ao inicializar ViewModel", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro na inicialização básica", e)
        }
    }
    
    private fun initializeHeavyComponents() {
        // Executar operações pesadas de forma assíncrona com timeout
        lifecycleScope.launch(Dispatchers.Main) { // Mudar para Main para garantir que o GoogleSignInClient seja inicializado na thread principal
            try {
                Log.d("MainActivity", "Iniciando componentes pesados...")
                
                // Usar withTimeout para evitar travamento
                kotlinx.coroutines.withTimeout(5000) { // 5 segundos de timeout
                    // Configuração do Google Sign-In (deve ser na thread principal)
                    try {
                        // Obter Client ID do google-services.json dinamicamente
                        val clientId = try {
                            val inputStream = resources.openRawResource(
                                resources.getIdentifier("google_services_client_id", "raw", packageName)
                            )
                            // Se não encontrar, usar o hardcoded
                            "231873451192-ai0rpmu8iumkk4lo9sbcbg0b4tvbtald.apps.googleusercontent.com"
                        } catch (e: Exception) {
                            // Fallback para Client ID hardcoded
                            "231873451192-ai0rpmu8iumkk4lo9sbcbg0b4tvbtald.apps.googleusercontent.com"
                        }
                        
                        Log.d("GoogleSignIn", "Configurando Google Sign-In com Client ID: ${clientId.take(30)}...")
                        
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(clientId)
                            .requestEmail()
                            .build()
                        googleSignInClient = GoogleSignIn.getClient(this@MainActivity, gso)
                        Log.d("MainActivity", "Google Sign-In configurado com sucesso")
                        Log.d("GoogleSignIn", "Client ID usado: $clientId")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao configurar Google Sign-In", e)
                        e.printStackTrace()
                    }
                    
                    // Registrar para receber callbacks de autenticação
                    AuthManager.getInstance().addAuthStateListener(this@MainActivity)
                    
                    Log.d("MainActivity", "Google Sign-In configurado")
                    
                    // Aguardar um pouco para garantir que o ViewModel foi inicializado
                    kotlinx.coroutines.delay(100)
                    
                    // Verificar se já está logado - mover para thread principal
                    try {
                        if (::viewModel.isInitialized && viewModel.isLoggedIn()) {
                            withContext(Dispatchers.Main) {
                                try {
                                    startDashboard()
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Erro ao iniciar dashboard", e)
                                }
                            }
                            return@withTimeout
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao verificar login", e)
                    }
                    
                    // Exibir termo LGPD se ainda não aceito - mover para thread principal
                    withContext(Dispatchers.Main) {
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
                            } else {
                                // Usuário já aceitou o termo LGPD
                            }
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Erro ao mostrar termo LGPD", e)
                        }
                    }
                }
                
                Log.d("MainActivity", "Componentes pesados inicializados com sucesso")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("MainActivity", "Timeout na inicialização dos componentes pesados", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Inicialização demorou muito, mas o app está funcionando", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro na inicialização", e)
                // Mostrar erro para o usuário na thread principal
                withContext(Dispatchers.Main) {
                    try {
                        Toast.makeText(this@MainActivity, "Erro na inicialização do app", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao mostrar toast", e)
                    }
                }
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
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            
            // Validação básica
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de e-mail
            if (!com.psipro.app.utils.FirebaseAuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Verificar se Firebase está inicializado
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            Log.d("MainActivity", "=== INÍCIO DO LOGIN ===")
            Log.d("MainActivity", "Firebase Auth instance: $auth")
            Log.d("MainActivity", "Current user antes do login: $currentUser")
            Log.d("MainActivity", "Tentando login com email: $email")
            Log.d("MainActivity", "Senha fornecida: ${if (password.isNotEmpty()) "***${password.length} caracteres***" else "VAZIA"}")
            
            // Desabilitar botão durante o login
            loginButton.isEnabled = false
            loginButton.text = "Entrando..."
            
            // Tentar login
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    loginButton.isEnabled = true
                    loginButton.text = "Entrar"
                    
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Log.d("MainActivity", "Login bem-sucedido: ${user?.email}")
                        Log.d("MainActivity", "User UID: ${user?.uid}")
                        Log.d("MainActivity", "User verified: ${user?.isEmailVerified}")
                        
                        // Atualizar informações do usuário nas SharedPreferences
                        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                        val editor = prefs.edit()
                        
                        // Sempre atualizar com os dados do usuário atual do Firebase
                        user?.email?.let { email ->
                            val currentEmail = prefs.getString("current_user_email", null)
                            // Se mudou de usuário, limpar dados antigos do perfil
                            if (currentEmail != null && currentEmail != email) {
                                Log.d("MainActivity", "Usuário diferente detectado. Limpando dados antigos.")
                                editor.remove("profile_name")
                                editor.remove("profile_email")
                                editor.remove("profile_crp")
                                editor.remove("profile_photo_path")
                                editor.remove("profile_photo_url")
                            }
                            editor.putString("current_user_email", email)
                        }
                        
                        // Atualizar nome do perfil (usar displayName do Firebase ou email)
                        val displayName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Usuário"
                        editor.putString("profile_name", displayName)
                        
                        // Se não tiver email no perfil, usar o email do Firebase
                        if (!prefs.contains("profile_email")) {
                            user?.email?.let { email ->
                                editor.putString("profile_email", email)
                            }
                        }
                        
                        editor.apply()
                        
                        Log.d("MainActivity", "Dados do perfil atualizados - Email: ${user?.email}, Nome: $displayName")
                        
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        // Sync pacientes no login (não bloqueia o uso offline).
                        startBackendSessionAndEnqueueSync(email, password)
                        startDashboard()
                    } else {
                        val exception = task.exception
                        val errorCode = (exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
                        val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(exception)
                        
                        Log.e("MainActivity", "=== ERRO NO LOGIN ===")
                        Log.e("MainActivity", "Error code: $errorCode")
                        Log.e("MainActivity", "Error message: ${exception?.message}")
                        Log.e("MainActivity", "Email usado: $email")
                        Log.e("MainActivity", "Exception type: ${exception?.javaClass?.simpleName}")
                        exception?.printStackTrace()
                        
                        // Mensagem mais específica baseada no código de erro
                        val userMessage = when (errorCode) {
                            "ERROR_USER_NOT_FOUND" -> {
                                "Conta não encontrada.\n\nCrie uma conta primeiro ou verifique se o email está correto."
                            }
                            "ERROR_WRONG_PASSWORD" -> {
                                "Senha incorreta.\n\nUse 'Esqueci minha senha' para recuperar ou verifique a senha."
                            }
                            "ERROR_INVALID_EMAIL" -> {
                                "Email inválido.\n\nVerifique o formato do email (ex: usuario@email.com)."
                            }
                            "ERROR_INVALID_CREDENTIAL" -> {
                                "Credenciais inválidas.\n\nVerifique se:\n• O email está correto\n• A senha está correta\n• A conta existe no Firebase"
                            }
                            else -> errorMessage
                        }
                        
                        // Mostrar dialog com mais informações
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Erro no Login")
                            .setMessage(userMessage)
                            .setPositiveButton("Criar Conta") { _, _ ->
                                val intent = Intent(this, CreateAccountActivity::class.java)
                                createAccountLauncher.launch(intent)
                            }
                            .setNeutralButton("Recuperar Senha") { _, _ ->
                                forgotPasswordButton.performClick()
                            }
                            .setNegativeButton("OK", null)
                            .show()
                        
                        // Também mostrar toast
                        Toast.makeText(this, userMessage.split("\n").first(), Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    loginButton.isEnabled = true
                    loginButton.text = "Entrar"
                    val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(e)
                    Log.e("MainActivity", "Falha no login", e)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, informe seu e-mail", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de e-mail
            if (!com.psipro.app.utils.FirebaseAuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            forgotPasswordButton.isEnabled = false
            
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    forgotPasswordButton.isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Instruções de recuperação enviadas para seu e-mail", Toast.LENGTH_LONG).show()
                    } else {
                        val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(task.exception)
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    forgotPasswordButton.isEnabled = true
                    val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(e)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }

        googleButton.setOnClickListener {
            try {
                // Verificar se o Google Sign-In foi inicializado
                if (!::googleSignInClient.isInitialized) {
                    Toast.makeText(this, "Aguarde, inicializando login Google...", Toast.LENGTH_SHORT).show()
                    Log.w("GoogleSignIn", "GoogleSignInClient não inicializado ainda")
                    return@setOnClickListener
                }
                
                Log.d("GoogleSignIn", "Iniciando processo de login Google")
                
                // Verificar conectividade (usando NetworkCapabilities ao invés de activeNetworkInfo)
                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
                val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                if (!isConnected) {
                    Toast.makeText(this, "Sem conexão com a internet", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                
                // Limpar cache do Google Sign-In e iniciar login
                googleSignInClient.signOut().addOnCompleteListener {
                    Log.d("GoogleSignIn", "Sign out completado, iniciando login")
                    
                    try {
                        // Tentar login direto usando Activity Result API
                        val signInIntent = googleSignInClient.signInIntent
                        Log.d("GoogleSignIn", "Intent criado: ${signInIntent.action}")
                        Log.d("GoogleSignIn", "Intent package: ${signInIntent.`package`}")
                        Log.d("GoogleSignIn", "Intent component: ${signInIntent.component}")
                        
                        // Verificar se o Google Play Services está disponível
                        try {
                            val availability = GoogleApiAvailability.getInstance()
                            val resultCode = availability.isGooglePlayServicesAvailable(this)
                            if (resultCode != ConnectionResult.SUCCESS) {
                                Log.e("GoogleSignIn", "Google Play Services não disponível: $resultCode")
                                if (availability.isUserResolvableError(resultCode)) {
                                    availability.getErrorDialog(this, resultCode, 9000)?.show()
                                } else {
                                    Toast.makeText(this, "Google Play Services não está disponível neste dispositivo", Toast.LENGTH_LONG).show()
                                }
                                return@addOnCompleteListener
                            } else {
                                Log.d("GoogleSignIn", "Google Play Services está disponível")
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleSignIn", "Erro ao verificar Google Play Services", e)
                        }
                        
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Erro ao iniciar activity", e)
                        e.printStackTrace()
                        Toast.makeText(this, "Erro ao abrir login Google: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { e ->
                    Log.e("GoogleSignIn", "Erro ao fazer sign out", e)
                    // Mesmo com erro no sign out, tentar login
                    try {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    } catch (ex: Exception) {
                        Log.e("GoogleSignIn", "Erro ao iniciar activity após sign out falho", ex)
                        Toast.makeText(this, "Erro ao abrir login Google: ${ex.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Erro ao iniciar login Google", e)
                Toast.makeText(this, "Erro ao iniciar login: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        createAccountButton.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            createAccountLauncher.launch(intent)
        }
    }

    private fun startBackendSessionAndEnqueueSync(email: String, password: String) {
        try {
            val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                applicationContext,
                com.psipro.app.sync.di.SyncEntryPoint::class.java
            )
            val backendAuth = entryPoint.backendAuthManager()

            lifecycleScope.launch(Dispatchers.IO) {
                val ok = backendAuth.login(email, password)
                if (ok) {
                    backendAuth.ensureClinicId()
                    com.psipro.app.sync.work.PatientsSyncScheduler.enqueue(applicationContext, "login")
                    Log.i("SyncPatients", "LOGIN_SYNC_ENQUEUED")
                } else {
                    Log.w("SyncPatients", "LOGIN_SYNC_SKIPPED_BACKEND_LOGIN_FAILED")
                }
            }
        } catch (e: Exception) {
            Log.e("SyncPatients", "LOGIN_SYNC_SETUP_FAILED", e)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            Log.d("GoogleSignIn", "Iniciando handleSignInResult")
            
            // Verificar se a task foi completada com sucesso
            if (!completedTask.isSuccessful) {
                val exception = completedTask.exception
                Log.e("GoogleSignIn", "Task não foi bem-sucedida", exception)
                
                if (exception is ApiException) {
                    when (exception.statusCode) {
                        10 -> {
                            Log.e("GoogleSignIn", "Erro 10 detectado - Problema de configuração")
                            showError10Solution()
                        }
                        12501 -> {
                            Log.d("GoogleSignIn", "Login cancelado pelo usuário")
                            // Não mostrar toast para cancelamento
                        }
                        else -> {
                            Toast.makeText(this, "Erro ao fazer login: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Erro ao fazer login: ${exception?.message ?: "Erro desconhecido"}", Toast.LENGTH_LONG).show()
                }
                return
            }
            
            val account = completedTask.getResult(ApiException::class.java)
            
            // Verificar se a conta e o token são válidos
            if (account == null) {
                Log.e("GoogleSignIn", "Conta é null")
                Toast.makeText(this, "Erro: não foi possível obter dados da conta", Toast.LENGTH_LONG).show()
                return
            }
            
            if (account.idToken == null) {
                Log.e("GoogleSignIn", "ID Token é null")
                Toast.makeText(this, "Erro: token de autenticação inválido", Toast.LENGTH_LONG).show()
                return
            }
            
            Log.d("GoogleSignIn", "Conta obtida: ${account.email}")
            
            // Autenticar com Firebase usando o token do Google
            Log.d("GoogleSignIn", "Criando credential com token: ${account.idToken?.take(20)}...")
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("GoogleSignIn", "Credential criada, autenticando com Firebase...")
            
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d("GoogleSignIn", "Firebase auth completado - Sucesso: ${task.isSuccessful}")
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Log.d("GoogleSignIn", "Login Firebase bem-sucedido - User: ${user?.email}")
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
                        Toast.makeText(this, "Bem-vindo(a), ${account.displayName ?: account.email}", Toast.LENGTH_SHORT).show()
                        startDashboard()
                    } else {
                        val exception = task.exception
                        Log.e("GoogleSignIn", "Erro no Firebase", exception)
                        val errorCode = (exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
                        val errorMessage = exception?.message ?: "Erro desconhecido na autenticação"
                        
                        Log.e("GoogleSignIn", "Error Code: $errorCode, Message: $errorMessage")
                        
                        // Usar o helper de erros para mensagens consistentes
                        val userMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(exception)
                        Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GoogleSignIn", "Falha na autenticação Firebase", e)
                    e.printStackTrace()
                    Toast.makeText(this, "Erro na autenticação: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Erro ApiException: ${e.statusCode} - ${e.message}")
            when (e.statusCode) {
                10 -> {
                    Log.e("GoogleSignIn", "Erro 10 detectado - Problema de configuração")
                    showError10Solution()
                }
                12501 -> {
                    Log.d("GoogleSignIn", "Login cancelado pelo usuário")
                    // Não mostrar toast para cancelamento
                }
                else -> {
                    Toast.makeText(this, "Erro ao fazer login com Google: ${e.statusCode} - ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Erro geral: ${e.message}", e)
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
                    googleSignInLauncher.launch(signInIntent)
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



