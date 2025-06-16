package com.example.psipro

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.psipro.auth.AuthManager
import com.example.psipro.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.navigation.ui.navigateUp
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import android.util.Log
import android.widget.Toast
import android.os.Build
import android.content.res.Configuration
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_schedule,
                R.id.navigation_patients,
                R.id.nav_financeiro,
                R.id.nav_notificacoes,
                R.id.nav_aniversariantes,
                R.id.nav_configuracoes,
                R.id.nav_suporte
            ),
            drawerLayout
        )

        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

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
                R.id.nav_notificacoes -> navController.navigate(R.id.nav_notificacoes)
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