package com.psipro.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
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
import com.psipro.app.databinding.ActivityDashboardBinding
import com.psipro.app.ui.NotificationsActivity
import com.psipro.app.ui.viewmodels.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    private lateinit var notificationViewModel: NotificationViewModel
    
    private var notificationBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar o NotificationViewModel usando ViewModelProvider
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
                R.id.navigation_patients
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
                R.id.nav_financeiro -> navController.navigate(R.id.nav_financeiro)
                R.id.nav_notificacoes -> {
                    // Abrir tela de notificações
                    val intent = Intent(this, NotificationsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_autoavaliacao -> navController.navigate(R.id.nav_autoavaliacao)
                R.id.nav_aniversariantes -> navController.navigate(R.id.nav_aniversariantes)
                R.id.nav_configuracoes -> navController.navigate(R.id.nav_configuracoes)
                R.id.nav_suporte -> navController.navigate(R.id.nav_suporte)
                R.id.nav_sair -> {
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
        val bottomNavIds = setOf(R.id.navigation_home, R.id.navigation_schedule, R.id.navigation_patients, R.id.nav_financeiro)

        // Listener para atualizar a UI com base na navegação
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Atualiza o título da Toolbar
            supportActionBar?.title = destination.label

            // Se o destino for um item da navegação inferior, limpa a seleção do menu lateral
            if (bottomNavIds.contains(destination.id)) {
                for (i in 0 until navigationView.menu.size()) {
                    navigationView.menu.getItem(i).isChecked = false
                }
            }
        }
        
        // Configurar badge de notificações
        setupNotificationBadge()
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
    }

    fun updateDrawerHeader() {
        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.nav_header_name)
        val crpTextView = headerView.findViewById<TextView>(R.id.nav_header_crp)
        val photoImageView = headerView.findViewById<ImageView>(R.id.nav_header_photo)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        nameTextView.text = prefs.getString("profile_name", "Seu Nome aqui")
        crpTextView.text = prefs.getString("profile_crp", "CRP não informado")
        // Se tiver foto salva, carregue, senão use ic_account_circle
        val photoPath = prefs.getString("profile_photo_path", null)
        if (photoPath != null) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                photoImageView.setImageBitmap(bitmap)
            } else {
                photoImageView.setImageResource(R.drawable.ic_account_circle)
            }
        } else {
            photoImageView.setImageResource(R.drawable.ic_account_circle)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 



