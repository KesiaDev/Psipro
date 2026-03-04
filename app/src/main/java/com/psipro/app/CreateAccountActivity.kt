package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.databinding.ActivityCreateAccountBinding
import com.psipro.app.utils.AuthErrorHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()
            val confirmPassword = binding.editConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.length < 2) {
                Toast.makeText(this, "O nome deve ter pelo menos 2 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!AuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordValidation = AuthErrorHelper.validatePassword(password)
            if (!passwordValidation.isValid) {
                Toast.makeText(this, passwordValidation.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnCreateAccount.isEnabled = false
            binding.btnCreateAccount.text = "Criando conta..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                        applicationContext,
                        com.psipro.app.sync.di.SyncEntryPoint::class.java
                    )
                    val backendAuth = entryPoint.backendAuthManager()
                    backendAuth.register(email, password, name)

                    withContext(Dispatchers.Main) {
                        binding.btnCreateAccount.isEnabled = true
                        binding.btnCreateAccount.text = "CRIAR CONTA"
                        backendAuth.ensureClinicId()
                        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                        prefs.edit().putString("profile_name", name).putString("profile_email", email).apply()
                        Toast.makeText(this@CreateAccountActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                        val resultIntent = Intent()
                        resultIntent.putExtra("email", email)
                        resultIntent.putExtra("password", password)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.btnCreateAccount.isEnabled = true
                        binding.btnCreateAccount.text = "CRIAR CONTA"
                        Toast.makeText(this@CreateAccountActivity, AuthErrorHelper.getErrorMessage(e, AuthErrorHelper.Context.REGISTER), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
