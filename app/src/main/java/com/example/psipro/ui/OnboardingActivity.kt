package com.example.psipro.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.psipro.config.AppConfig
import com.example.psipro.databinding.ActivityOnboardingBinding
import com.example.psipro.ui.adapters.OnboardingAdapter
import com.example.psipro.viewmodel.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.psipro.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.psipro.MainActivity

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()
    
    @Inject
    lateinit var appConfig: AppConfig
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        if (!appConfig.isFirstRun) {
            startMainActivity()
            return
        }
        
        setupViewPager()
        setupButtons()
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingAdapter(
            listOf(
                OnboardingPage(
                    title = "Bem-vindo ao PsiPro",
                    description = "Seu assistente pessoal para gerenciar sua clínica psicológica",
                    imageRes = R.drawable.logo_novo
                ),
                OnboardingPage(
                    title = "Agende Consultas",
                    description = "Organize sua agenda de forma simples e eficiente",
                    imageRes = R.drawable.ic_schedule
                ),
                OnboardingPage(
                    title = "Gerencie Pacientes",
                    description = "Mantenha o histórico e informações dos seus pacientes organizados",
                    imageRes = R.drawable.ic_people
                ),
                OnboardingPage(
                    title = "Backup Automático",
                    description = "Seus dados são automaticamente salvos e protegidos",
                    imageRes = R.drawable.logo_novo
                )
            )
        )
        
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ -> }.attach()
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.nextButton.text = if (position == adapter.itemCount - 1) "Começar" else "Próximo"
            }
        })
    }
    
    private fun setupButtons() {
        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem == binding.viewPager.adapter?.itemCount?.minus(1)) {
                completeOnboarding()
            } else {
                binding.viewPager.currentItem += 1
            }
        }
        
        binding.skipButton.setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun completeOnboarding() {
        appConfig.isFirstRun = false
        startMainActivity()
    }
    
    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    data class OnboardingPage(
        val title: String,
        val description: String,
        val imageRes: Int
    )
} 