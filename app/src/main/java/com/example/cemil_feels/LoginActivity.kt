package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityLoginBinding

/**
 * Aktivitas Authentication Screen (Page 2).
 * Halaman Login dengan input Email & Password, atau opsi login Google.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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

        // Ketika tombol Log In diklik
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text?.toString()?.trim()
            val password = binding.etLoginPassword.text?.toString()

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan ke AppState (State Retention)
            AppState.loggedInEmail = email
            AppState.loggedInPassword = password

            // Sukses login -> Berpindah ke MainActivity (Venting Space)
            Toast.makeText(this, "Selamat datang, ${AppState.USER_NICKNAME}!", Toast.LENGTH_SHORT).show()
            goToMainActivity()
        }

        // Login dengan Google (Mock)
        binding.btnLoginGoogle.setOnClickListener {
            AppState.loggedInEmail = "cemil.user@gmail.com"
            AppState.loggedInPassword = "google-oauth-password"
            Toast.makeText(this, "Login Google Berhasil!", Toast.LENGTH_SHORT).show()
            goToMainActivity()
        }

        // Tautan Sign Up (Mock)
        binding.tvBtnSignup.setOnClickListener {
            Toast.makeText(this, "Fitur Registrasi belum tersedia secara lokal.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Menghindari agar user tidak bisa kembali ke halaman login via tombol Back
    }
}
