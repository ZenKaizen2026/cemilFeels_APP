package com.example.cemil_feels.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface UserRepository {
    fun login(email: String, password: String): Boolean
    fun loginWithGoogle(): Boolean
    fun getLoggedInEmail(): String?
    fun getLoggedInPassword(): String?
    fun getNickname(): String
    fun observeUserEmail(): Flow<String?>
}

class UserRepositoryImpl : UserRepository {
    private val _loggedInEmail = MutableStateFlow<String?>(null)
    private var loggedInPassword: String? = null

    override fun login(email: String, password: String): Boolean {
        if (email.isNotEmpty() && email.contains("@") && password.isNotEmpty()) {
            _loggedInEmail.value = email
            loggedInPassword = password
            return true
        }
        return false
    }

    override fun loginWithGoogle(): Boolean {
        _loggedInEmail.value = "cemil.user@gmail.com"
        loggedInPassword = "google-oauth-password"
        return true
    }

    override fun getLoggedInEmail(): String? = _loggedInEmail.value
    override fun getLoggedInPassword(): String? = loggedInPassword
    override fun getNickname(): String = "Cemil"

    override fun observeUserEmail(): Flow<String?> = _loggedInEmail.asStateFlow()
}
