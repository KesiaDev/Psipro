package com.psipro.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.psipro.app.DashboardActivity
import com.psipro.app.databinding.ActivityLgpdConsentBinding
import com.psipro.app.sync.di.SyncEntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tela obrigatória de consentimento LGPD.
 * Bloqueia o acesso ao app até o usuário aceitar.
 * Envia consentimento para o backend (auditoria).
 */
@AndroidEntryPoint
class LgpdConsentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLgpdConsentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLgpdConsentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val termo = """
            Este aplicativo coleta e armazena dados pessoais de pacientes para fins de gestão clínica, 
            em conformidade com a LGPD (Lei Geral de Proteção de Dados).
            <br><br>
            Ao prosseguir, você concorda com o tratamento dos dados conforme descrito na nossa 
            <a href="privacy">política de privacidade</a>.
            <br><br>
            Você pode solicitar a exclusão ou exportação dos seus dados a qualquer momento.
        """.trimIndent()

        binding.tvTermo.text = HtmlCompat.fromHtml(termo, HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.btnAceitar.setOnClickListener {
            if (!binding.cbAceito.isChecked) {
                Toast.makeText(this, "Marque a opção para aceitar o termo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            recordConsentAndProceed()
        }
    }

    private fun recordConsentAndProceed() {
        binding.btnAceitar.isEnabled = false
        binding.btnAceitar.text = "Salvando..."

        lifecycleScope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    SyncEntryPoint::class.java
                )
                val authManager = entryPoint.backendAuthManager()

                val ok = withContext(Dispatchers.IO) {
                    authManager.recordLgpdConsent()
                }

                if (ok) {
                    // Manter compatibilidade com settings (MainActivity usa prefs)
                    getSharedPreferences("settings", MODE_PRIVATE)
                        .edit()
                        .putBoolean("aceitou_lgpd", true)
                        .apply()
                    goToDashboard()
                } else {
                    // Falha na API: salva local e prossegue (evita bloquear offline)
                    authManager.setLgpdConsentLocal(true)
                    getSharedPreferences("settings", MODE_PRIVATE)
                        .edit()
                        .putBoolean("aceitou_lgpd", true)
                        .apply()
                    Toast.makeText(this@LgpdConsentActivity, "Consentimento registrado localmente. Será enviado quando houver conexão.", Toast.LENGTH_LONG).show()
                    goToDashboard()
                }
            } catch (e: Exception) {
                // Erro de rede: salva local e prossegue
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    SyncEntryPoint::class.java
                )
                entryPoint.backendAuthManager().setLgpdConsentLocal(true)
                getSharedPreferences("settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("aceitou_lgpd", true)
                    .apply()
                Toast.makeText(this@LgpdConsentActivity, "Sem conexão. Consentimento salvo localmente.", Toast.LENGTH_LONG).show()
                goToDashboard()
            } finally {
                binding.btnAceitar.isEnabled = true
                binding.btnAceitar.text = "Aceito"
            }
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
