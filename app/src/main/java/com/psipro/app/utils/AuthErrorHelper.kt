package com.psipro.app.utils

import android.util.Log
import retrofit2.HttpException

/**
 * Helper para validação e mensagens de erro de autenticação (backend JWT).
 * Substitui FirebaseAuthErrorHelper após migração para backend exclusivo.
 */
object AuthErrorHelper {
    private const val TAG = "AuthError"

    /** Contexto: login (padrão) ou register — mensagens de 404 variam */
    enum class Context { LOGIN, REGISTER }

    fun getErrorMessage(exception: Exception?, context: Context = Context.LOGIN): String {
        if (exception == null) {
            return "Erro desconhecido. Tente novamente."
        }

        // Erros HTTP da API
        if (exception is HttpException) {
            val code = exception.code()
            val body = try {
                exception.response()?.errorBody()?.string()
            } catch (_: Exception) { null }
            return when (code) {
                401 -> "Credenciais inválidas. Verifique seu e-mail e senha."
                403 -> "Acesso negado."
                404 -> when (context) {
                    Context.LOGIN -> "Conta não encontrada. Verifique o e-mail ou crie uma nova conta."
                    Context.REGISTER -> "Não foi possível criar a conta. Verifique sua conexão e tente novamente."
                }
                409 -> body?.let { if (it.contains("cadastrado") || it.contains("already")) "Este e-mail já está cadastrado." else "Conflito: $it" } ?: "Este e-mail já está cadastrado."
                422 -> "Dados inválidos. Verifique os campos."
                else -> "Erro do servidor ($code). Tente novamente."
            }
        }

        // Erros genéricos
        return when {
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Erro de conexão. Verifique sua internet e tente novamente."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Tempo de conexão esgotado. Tente novamente."
            else -> "Erro: ${exception.message ?: "Erro desconhecido. Tente novamente."}"
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

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

/**
 * Verifica se a exceção indica conflito de agenda (HTTP 409).
 * Usado quando backend retorna conflito em create/update de appointment.
 */
fun isConflict409(e: Exception): Boolean =
    (e as? HttpException)?.code() == 409
