package com.example.cemil_feels

import android.content.Context
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

        // Menampilkan nama dari SharedPreferences
        val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPrefs.getString("registered_name", "Setia")
        binding.tvUsername.text = getString(R.string.greeting_user, savedName)

        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Menu samping terpilih.", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotification.setOnClickListener {
            val intent = Intent(this, NotificationHistoryActivity::class.java)
            startActivity(intent)
        }

        // Menghubungkan tombol pointer (btn_search_snack) ke halaman Venting
        binding.btnSearchSnack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.chipHappy.setOnClickListener { navigateToRecommendation() }
        binding.chipSad.setOnClickListener { navigateToRecommendation() }
        binding.chipAngry.setOnClickListener { navigateToRecommendation() }
        binding.chipBored.setOnClickListener { navigateToRecommendation() }
    }

    private fun navigateToRecommendation() {
        val intent = Intent(this, RecommendationActivity::class.java)
        startActivity(intent)
    }
}
