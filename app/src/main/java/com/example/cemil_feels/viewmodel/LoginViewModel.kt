package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    sealed class LoginEvent {
        data class Success(val nickname: String) : LoginEvent()
        data class Error(val message: String) : LoginEvent()
    }

    private val _eventFlow = MutableSharedFlow<LoginEvent>()
    val eventFlow: SharedFlow<LoginEvent> = _eventFlow.asSharedFlow()

    fun login(email: String?, password: String?) {
        val trimmedEmail = email?.trim()
        if (trimmedEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
            triggerError("Email dan Password tidak boleh kosong!")
            return
        }

        if (!trimmedEmail.contains("@")) {
            triggerError("Format email tidak valid!")
            return
        }

        val success = userRepository.login(trimmedEmail, password)
        if (success) {
            triggerSuccess(userRepository.getNickname())
        } else {
            triggerError("Gagal login, silakan periksa kredensial Anda.")
        }
    }

    fun loginWithGoogle() {
        val success = userRepository.loginWithGoogle()
        if (success) {
            triggerSuccess(userRepository.getNickname())
        } else {
            triggerError("Gagal login via Google.")
        }
    }

    private fun triggerError(msg: String) {
        viewModelScope.launch {
            _eventFlow.emit(LoginEvent.Error(msg))
        }
    }

    private fun triggerSuccess(nickname: String) {
        viewModelScope.launch {
            _eventFlow.emit(LoginEvent.Success(nickname))
        }
    }
}
