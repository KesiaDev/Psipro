package com.psipro.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.psipro.app.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString()
            val confirmPassword = binding.editConfirmPassword.text.toString()

            // Validação de campos vazios
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de nome
            if (name.length < 2) {
                Toast.makeText(this, "O nome deve ter pelo menos 2 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de e-mail
            if (!com.psipro.app.utils.FirebaseAuthErrorHelper.isValidEmail(email)) {
                Toast.makeText(this, "Por favor, insira um e-mail válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de senha
            val passwordValidation = com.psipro.app.utils.FirebaseAuthErrorHelper.validatePassword(password)
            if (!passwordValidation.isValid) {
                Toast.makeText(this, passwordValidation.message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de confirmação de senha
            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Desabilitar botão durante criação
            binding.btnCreateAccount.isEnabled = false
            binding.btnCreateAccount.text = "Criando conta..."
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                            binding.btnCreateAccount.isEnabled = true
                            binding.btnCreateAccount.text = "CRIAR CONTA"
                            
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                val resultIntent = Intent()
                                resultIntent.putExtra("email", email)
                                resultIntent.putExtra("password", password)
                                setResult(RESULT_OK, resultIntent)

                                // Salvar nome nas SharedPreferences
                                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                                prefs.edit().putString("profile_name", name).apply()

                                finish()
                            } else {
                                val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(updateTask.exception)
                                Toast.makeText(this, "Erro ao salvar nome: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }?.addOnFailureListener { e ->
                            binding.btnCreateAccount.isEnabled = true
                            binding.btnCreateAccount.text = "CRIAR CONTA"
                            val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(e)
                            Toast.makeText(this, "Erro ao salvar nome: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        binding.btnCreateAccount.isEnabled = true
                        binding.btnCreateAccount.text = "CRIAR CONTA"
                        val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(task.exception)
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    binding.btnCreateAccount.isEnabled = true
                    binding.btnCreateAccount.text = "CRIAR CONTA"
                    val errorMessage = com.psipro.app.utils.FirebaseAuthErrorHelper.getErrorMessage(e)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
        }
    }
} 



