package com.example.apppisc

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.apppisc.auth.AuthManager
import com.example.apppisc.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.navigation.ui.navigateUp

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: androidx.navigation.NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar a toolbar
        setSupportActionBar(binding.toolbar)

        // Configurar a navegação
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar o Drawer
        val drawerLayout = binding.drawerLayout
        val navView = binding.navigationView
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar o AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_schedule,
                R.id.navigation_patients,
                R.id.nav_financeiro,
                R.id.nav_notificacoes,
                R.id.nav_configuracoes,
                R.id.nav_suporte,
                R.id.nav_aniversariantes
            ),
            drawerLayout
        )

        // Configurar a navegação
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)
        binding.bottomNavigation.setupWithNavController(navController)

        // Configurar o listener do NavigationView
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    navController.navigate(R.id.navigation_home)
                }
                R.id.nav_agenda -> {
                    navController.navigate(R.id.navigation_schedule)
                }
                R.id.nav_pacientes -> {
                    navController.navigate(R.id.navigation_patients)
                }
                R.id.nav_financeiro -> {
                    navController.navigate(R.id.nav_financeiro)
                }
                R.id.nav_notificacoes -> {
                    navController.navigate(R.id.nav_notificacoes)
                }
                R.id.nav_configuracoes -> {
                    navController.navigate(R.id.nav_configuracoes)
                }
                R.id.nav_suporte -> {
                    navController.navigate(R.id.nav_suporte)
                }
                R.id.nav_aniversariantes -> {
                    navController.navigate(R.id.nav_aniversariantes)
                }
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                AuthManager.getInstance().logout()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            R.id.action_settings -> {
                navController.navigate(R.id.nav_configuracoes)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 