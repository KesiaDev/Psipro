package com.psipro.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.navigation.NavigationView
import com.psipro.app.R
import com.psipro.app.auth.AuthManager
import com.psipro.app.ui.VoiceCommandDialogFragment
import com.psipro.app.utils.AccessibilityPreferences
import com.psipro.app.ui.viewmodels.VoiceCommandViewModel
import com.psipro.app.utils.VoiceAction
import com.psipro.app.databinding.ActivityDashboardBinding
import com.psipro.app.ui.NotificationsActivity
import com.psipro.app.ui.viewmodels.NotificationViewModel
import com.psipro.app.utils.WebNavigator
import com.psipro.app.utils.ProfessionalTypeHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import dagger.hilt.android.EntryPointAccessors
import com.psipro.app.sync.di.SyncEntryPoint


@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    private lateinit var notificationViewModel: NotificationViewModel
    private val voiceCommandViewModel: VoiceCommandViewModel by viewModels()

    private var notificationBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AccessibilityPreferences.getHighContrast(this)) {
            setTheme(R.style.Theme_Psipro_HighContrast)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar NotificationViewModel
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_schedule,
                R.id.navigation_patients,
                R.id.nav_configuracoes,
                R.id.nav_suporte,
                R.id.nav_notificacoes
            ),
            drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // Verifica se há uma instrução para navegar para um destino específico
        val navigateTo = intent.getIntExtra("NAVIGATE_TO", -1)
        if (navigateTo != -1) {
            navController.navigate(navigateTo)
            
            // Se for para a agenda e há um paciente pré-selecionado, passar essa informação
            if (navigateTo == R.id.navigation_schedule) {
                val preselectedPatientId = intent.getLongExtra("PRESELECTED_PATIENT_ID", -1)
                val preselectedPatientName = intent.getStringExtra("PRESELECTED_PATIENT_NAME")
                val tipoConsulta = intent.getStringExtra("TIPO_CONSULTA")
                
                if (preselectedPatientId != -1L) {
                    // Passar a informação do paciente pré-selecionado para o fragment
                    val bundle = Bundle().apply {
                        putLong("PRESELECTED_PATIENT_ID", preselectedPatientId)
                        putString("PRESELECTED_PATIENT_NAME", preselectedPatientName)
                        putString("TIPO_CONSULTA", tipoConsulta)
                    }
                    navController.navigate(navigateTo, bundle)
                }
            }
        }

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> navController.navigate(R.id.navigation_home)
                R.id.navigation_schedule -> navController.navigate(R.id.navigation_schedule)
                R.id.navigation_patients -> navController.navigate(R.id.navigation_patients)
                R.id.nav_notificacoes -> {
                    val intent = Intent(this, NotificationsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_plataforma_web -> openWebWithSso { WebNavigator.openDashboard(this) }
                R.id.nav_relatorios_web -> openWebWithSso { WebNavigator.openRelatoriosOnWeb(this) }
                R.id.nav_sync_patients -> {
                    runSyncWithFeedback()
                }
                R.id.nav_configuracoes -> {
                    try {
                        navController.navigate(R.id.nav_configuracoes)
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardActivity", "Erro ao navegar para Configurações", e)
                    }
                }
                R.id.nav_suporte -> {
                    try {
                        navController.navigate(R.id.nav_suporte)
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardActivity", "Erro ao navegar para Suporte", e)
                    }
                }
                R.id.nav_sair -> {
                    // Limpar dados do perfil ao fazer logout
                    val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                    prefs.edit().apply {
                        remove("current_user_email")
                        remove("profile_name")
                        remove("profile_email")
                        // Manter CRP e outros dados que não são específicos do usuário
                        apply()
                    }
                    
                    AuthManager.getInstance().logout()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Conjunto de IDs dos itens da navegação inferior para verificação
        val bottomNavIds = setOf(R.id.navigation_home, R.id.navigation_schedule, R.id.navigation_patients)

        // Listener para atualizar a UI com base na navegação
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label ?: destination.id.toString()

            // Se o destino for um item da navegação inferior, limpa a seleção do menu lateral
            if (bottomNavIds.contains(destination.id)) {
                for (i in 0 until navigationView.menu.size()) {
                    navigationView.menu.getItem(i).isChecked = false
                }
            }
        }
        
        // Configurar badge de notificações
        setupNotificationBadge()

        // Observar comandos de voz reconhecidos
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                voiceCommandViewModel.pendingAction.collect { action ->
                    handleVoiceCommand(action)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_voice_command -> {
                VoiceCommandDialogFragment().show(supportFragmentManager, "VoiceCommandDialog")
                true
            }
            R.id.action_settings -> {
                navController.navigate(R.id.nav_configuracoes)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Atualiza o token do backend antes de abrir o web para garantir login automático. */
    private fun openWebWithSso(open: () -> Unit) {
        lifecycleScope.launch {
            runCatching {
                val ep = EntryPointAccessors.fromApplication(applicationContext, SyncEntryPoint::class.java)
                ep.backendAuthManager().refreshToken()
            }
            open()
        }
    }

    private fun runSyncWithFeedback() {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )
        entryPoint.sessionStore().clearSyncWatermarks()
        com.psipro.app.sync.work.SyncScheduler.enqueueBoth(this, "manual")
        android.widget.Toast.makeText(
            this,
            "Sincronizando pacientes, agenda e sessões... Aguarde alguns segundos.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    private fun handleVoiceCommand(action: VoiceAction) {
        val dialog = supportFragmentManager.findFragmentByTag("VoiceCommandDialog")
            as? VoiceCommandDialogFragment
        dialog?.dismiss()
        when (action) {
            VoiceAction.NEW_SESSION -> {
                startActivity(Intent(this, com.psipro.app.ui.AppointmentScheduleActivity::class.java))
            }
            VoiceAction.SEARCH_PATIENT, VoiceAction.TODAY_AGENDA -> {
                navController.navigate(
                    if (action == VoiceAction.SEARCH_PATIENT) R.id.navigation_patients
                    else R.id.navigation_schedule
                )
            }
            VoiceAction.HOME -> navController.navigate(R.id.navigation_home)
            VoiceAction.UNKNOWN -> { /* feedback já mostrado no dialog */ }
        }
    }
    
    private fun setupNotificationBadge() {
        // Observar mudanças no contador de notificações não lidas
        lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                // Atualizar o texto do item de menu para mostrar o contador
                val menuItem = navigationView.menu.findItem(R.id.nav_notificacoes)
                if (count > 0) {
                    menuItem.title = "Notificações ($count)"
                } else {
                    menuItem.title = "Notificações"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDrawerHeader()
        // Sync leve ao voltar ao foreground (não bloqueia offline)
        com.psipro.app.sync.work.SyncScheduler.enqueueBoth(this, "foreground")
    }

    fun updateDrawerHeader() {
        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.nav_header_name)
        val subtitleTextView = headerView.findViewById<TextView>(R.id.nav_header_crp)
        val photoImageView = headerView.findViewById<ImageView>(R.id.nav_header_photo)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        
        val displayName = prefs.getString("profile_name", null)
            ?: prefs.getString("current_user_email", null)?.substringBefore("@")
            ?: "Seu Nome aqui"
        nameTextView.text = displayName
        
        // Subtítulo: Tipo de profissional (quando disponível) ou CRP
        val professionalType = try {
            dagger.hilt.android.EntryPointAccessors.fromApplication(
                applicationContext,
                com.psipro.app.sync.di.SyncEntryPoint::class.java
            ).sessionStore().getProfessionalType()
        } catch (_: Exception) { null }
        subtitleTextView.text = when {
            !professionalType.isNullOrBlank() -> ProfessionalTypeHelper.toDisplayLabel(professionalType)
            else -> prefs.getString("profile_crp", "CRP não informado").takeIf { it?.isNotBlank() == true }
                ?: "Psicólogo"
        }
        
        // Foto: Priorizar foto local, depois URL do Google, depois padrão
        val photoPath = prefs.getString("profile_photo_path", null)
        val photoUrl = prefs.getString("profile_photo_url", null)
        
        when {
            photoPath != null -> {
                val file = File(photoPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    photoImageView.setImageBitmap(bitmap)
                } else {
                    loadPhotoFromUrl(photoImageView, photoUrl, null)
                }
            }
            photoUrl != null -> {
                loadPhotoFromUrl(photoImageView, photoUrl, null)
            }
            else -> {
                photoImageView.setImageResource(R.drawable.ic_account_circle)
            }
        }
        
        android.util.Log.d("DashboardActivity", "Header atualizado - Nome: $displayName")
    }
    
    private fun loadPhotoFromUrl(imageView: ImageView, photoUrl: String?, firebasePhotoUrl: String? = null) {
        // Sem Glide por enquanto
        // Fallback seguro para não quebrar o build
        imageView.setImageResource(R.drawable.ic_account_circle)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 



