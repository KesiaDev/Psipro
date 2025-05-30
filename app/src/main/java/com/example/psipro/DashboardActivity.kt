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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.psipro.state.UserState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    @Inject
    lateinit var userState: UserState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Definir barra de navegação e status escuras no modo escuro
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.surface_black)
                window.statusBarColor = ContextCompat.getColor(this, R.color.surface_black)
            }
        } else {
            // Modo claro: Toolbar com texto e ícones bronze
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.bronze_gold))
            binding.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.bronze_gold))
            binding.toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, R.color.bronze_gold))
        }

        // Configurar a toolbar
        setSupportActionBar(binding.toolbar)

        // Forçar cor bronze nos ícones e título da Toolbar (inclusive overflow/3 pontinhos)
        val bronze = ContextCompat.getColor(this, R.color.bronze_gold)
        binding.toolbar.setTitleTextColor(bronze)
        binding.toolbar.setSubtitleTextColor(bronze)
        binding.toolbar.navigationIcon?.setTint(bronze)
        binding.toolbar.overflowIcon?.setTint(bronze)

        setupNavigation()
        observeUserState()
    }

    private fun setupNavigation() {
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_patients, R.id.nav_appointments),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
    }

    private fun observeUserState() {
        lifecycleScope.launch {
            userState.user.collectLatest { user ->
                user?.let { updateNavigationHeader(it) }
            }
        }
    }

    private fun updateNavigationHeader(user: User) {
        val headerView = navigationView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.nav_header_name)
        val photoImageView = headerView.findViewById<ImageView>(R.id.nav_header_photo)

        nameTextView.text = user.name.ifEmpty { "Profissional" }

        if (user.photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(user.photoUrl)
                .circleCrop()
                .into(photoImageView)
        } else {
            photoImageView.setImageResource(R.drawable.default_profile)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.bronze_gold)
        forceHamburgerColorIfLightMode()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.bronze_gold)
        forceHamburgerColorIfLightMode()
    }

    private fun forceHamburgerColorIfLightMode() {
        drawerToggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.bronze_gold)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
            for (i in 0 until menu.size()) {
                menu.getItem(i).icon?.setTint(ContextCompat.getColor(this, R.color.bronze_gold))
            }
        }
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
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        updateNavigationHeader(userState.user.value ?: User(name = "Profissional"))
    }
} 