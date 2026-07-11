package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityLoginBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.LoginViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Authentication Screen (Page 2).
 * Halaman Login dengan input Email & Password, atau opsi login Google.
 * Refactored to follow MVVM architecture.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupObservers()

        // Ketika tombol Log In diklik
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text?.toString()
            val password = binding.etLoginPassword.text?.toString()
            viewModel.login(email, password)
        }

        // Login dengan Google (Mock)
        binding.btnLoginGoogle.setOnClickListener {
            viewModel.loginWithGoogle()
        }

        // Tautan Sign Up (Mock)
        binding.tvBtnSignup.setOnClickListener {
            Toast.makeText(this, "Fitur Registrasi belum tersedia secara lokal.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is LoginViewModel.LoginEvent.Success -> {
                        // Sync with legacy AppState for full backwards compatibility
                        val userRepository = ServiceLocator.container.userRepository
                        AppState.loggedInEmail = userRepository.getLoggedInEmail()
                        AppState.loggedInPassword = userRepository.getLoggedInPassword()
                        
                        Toast.makeText(this@LoginActivity, "Selamat datang, ${event.nickname}!", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    }
                    is LoginViewModel.LoginEvent.Error -> {
                        Toast.makeText(this@LoginActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Menghindari agar user tidak bisa kembali ke halaman login via tombol Back
    }
}
