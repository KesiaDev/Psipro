package com.example.apppisc.security

import android.content.Context
import com.example.apppisc.data.AppDatabase
import com.example.apppisc.data.entities.User
import com.example.apppisc.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AuthManager {
    private lateinit var userRepository: UserRepository
    private var currentUser: User? = null

    fun init(context: Context) {
        val userDao = AppDatabase.getInstance(context).userDao()
        userRepository = UserRepository(userDao)
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val user = userRepository.getUserByEmail(email)
            if (user != null && user.password == password) {
                currentUser = user
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun isAdmin(): Boolean {
        return currentUser?.isAdmin == true
    }
} 