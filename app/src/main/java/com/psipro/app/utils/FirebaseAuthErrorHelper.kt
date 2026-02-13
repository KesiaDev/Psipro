package com.psipro.app.utils

import com.google.firebase.auth.FirebaseAuthException
import android.util.Log

object FirebaseAuthErrorHelper {
    private const val TAG = "FirebaseAuthError"
    
    /**
     * Traduz mensagens de erro do Firebase para português
     */
    fun getErrorMessage(exception: Exception?): String {
        if (exception == null) {
            return "Erro desconhecido. Tente novamente."
        }
        
        if (exception is FirebaseAuthException) {
            return when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "O e-mail informado é inválido. Verifique e tente novamente."
                "ERROR_USER_DISABLED" -> "Esta conta foi desativada. Entre em contato com o suporte."
                "ERROR_USER_NOT_FOUND" -> "Nenhuma conta encontrada com este e-mail. Verifique ou crie uma nova conta."
                "ERROR_WRONG_PASSWORD" -> "Senha incorreta. Tente novamente ou recupere sua senha."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está cadastrado. Tente fazer login ou recuperar a senha."
                "ERROR_WEAK_PASSWORD" -> "A senha é muito fraca. Use pelo menos 6 caracteres."
                "ERROR_INVALID_CREDENTIAL" -> "Credenciais inválidas. Verifique seu e-mail e senha."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Este método de login não está habilitado. Entre em contato com o suporte."
                "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Aguarde alguns minutos e tente novamente."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Erro de conexão. Verifique sua internet e tente novamente."
                "ERROR_INTERNAL_ERROR" -> "Erro interno. Tente novamente em alguns instantes."
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Esta credencial já está sendo usada por outra conta."
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Uma conta já existe com este e-mail usando outro método de login. Tente fazer login com Google."
                else -> {
                    Log.w(TAG, "Código de erro não mapeado: ${exception.errorCode}")
                    "Erro: ${exception.message ?: "Erro desconhecido"}"
                }
            }
        }
        
        // Para outros tipos de exceção
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> 
                "Erro de conexão. Verifique sua internet e tente novamente."
            exception.message?.contains("timeout", ignoreCase = true) == true -> 
                "Tempo de conexão esgotado. Tente novamente."
            else -> "Erro: ${exception.message ?: "Erro desconhecido. Tente novamente."}"
        }
    }
    
    /**
     * Valida formato de e-mail
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Valida força da senha
     */
    fun validatePassword(password: String): PasswordValidationResult {
        if (password.length < 6) {
            return PasswordValidationResult(false, "A senha deve ter pelo menos 6 caracteres")
        }
        if (password.length > 128) {
            return PasswordValidationResult(false, "A senha não pode ter mais de 128 caracteres")
        }
        return PasswordValidationResult(true, "")
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val message: String
)

