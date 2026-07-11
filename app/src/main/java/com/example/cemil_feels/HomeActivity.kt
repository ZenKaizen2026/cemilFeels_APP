package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityHomeBinding

/**
 * Aktivitas Welcome Home Screen (Page 3).
 * Dashboard sapaan selamat datang kepada pengguna.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu samping terpilih.", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Kotak Masuk Notifikasi.", Toast.LENGTH_SHORT).show()
        }

        // Menghubungkan tombol pointer (start icon di search bar) ke halaman Venting (MainActivity)
        binding.searchLayout.setStartIconOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
