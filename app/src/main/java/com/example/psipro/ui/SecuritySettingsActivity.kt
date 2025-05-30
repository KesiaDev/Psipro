package com.example.psipro.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.psipro.databinding.ActivitySecuritySettingsBinding
import com.example.psipro.utils.BackupUtils
import java.io.File

class SecuritySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecuritySettingsBinding

    private val exportBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            val password = promptPassword() ?: return@registerForActivityResult
            val outputFile = File(cacheDir, "backup_temp.enc")
            if (BackupUtils.exportDatabase(this, password.toCharArray(), outputFile)) {
                contentResolver.openOutputStream(uri)?.use { out ->
                    outputFile.inputStream().copyTo(out)
                }
                outputFile.delete()
                Toast.makeText(this, "Backup exportado com sucesso!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Erro ao exportar backup.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val importBackupLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            BiometricHelper.authenticate(
                activity = this,
                onSuccess = {
                    val password = promptPassword() ?: return@authenticate
                    val inputFile = File(cacheDir, "backup_import.enc")
                    contentResolver.openInputStream(uri)?.use { inp ->
                        inputFile.outputStream().use { out -> inp.copyTo(out) }
                    }
                    if (BackupUtils.importDatabase(this, password.toCharArray(), inputFile)) {
                        Toast.makeText(this, "Backup restaurado com sucesso!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Erro ao restaurar backup.", Toast.LENGTH_LONG).show()
                    }
                    inputFile.delete()
                },
                onError = { errMsg ->
                    Toast.makeText(this, "Autenticação biométrica obrigatória para restaurar backup.", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecuritySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExportBackup.setOnClickListener {
            exportBackupLauncher.launch("backup_psipro.enc")
        }
        binding.btnImportBackup.setOnClickListener {
            importBackupLauncher.launch("application/octet-stream")
        }
    }

    // Exemplo simples de prompt de senha (substitua por um diálogo seguro na prática)
    private fun promptPassword(): String? {
        // Para produção, use um diálogo seguro!
        return "senhaSuperSegura123" // Troque por lógica real de input do usuário
    }
} 