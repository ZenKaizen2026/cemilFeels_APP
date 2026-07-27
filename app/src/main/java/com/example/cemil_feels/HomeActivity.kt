package com.example.cemil_feels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
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
        android.util.Log.d("HOME_DEBUG", "onCreate started")
        enableEdgeToEdge()

        try {
            android.util.Log.d("HOME_DEBUG", "Inflating binding...")
            binding = ActivityHomeBinding.inflate(layoutInflater)
            android.util.Log.d("HOME_DEBUG", "Binding inflated successfully")
            setContentView(binding.root)
            android.util.Log.d("HOME_DEBUG", "setContentView called")
        } catch (e: Exception) {
            android.util.Log.e("HOME_DEBUG", "CRASH during inflation: ${e.message}")
            e.printStackTrace()
            throw e
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            android.util.Log.d("HOME_DEBUG", "Setting up animations and views")
            setupAnimations()

            // Menampilkan nama dari SharedPreferences
            val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
            val savedName = sharedPrefs.getString("registered_name", "Setia")
            
            android.util.Log.d("HOME_DEBUG", "Accessing layoutTopBar.tvUsername")
            binding.layoutTopBar.tvUsername.text = getString(R.string.greeting_user, savedName)

            binding.layoutTopBar.btnMenu.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            binding.layoutTopBar.btnNotification.setOnClickListener {
                val intent = Intent(this, NotificationHistoryActivity::class.java)
                startActivity(intent)
            }
            android.util.Log.d("HOME_DEBUG", "onCreate finished successfully")
        } catch (e: Exception) {
            android.util.Log.e("HOME_DEBUG", "CRASH during initialization: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun setupAnimations() {
        val clickAnim = AnimationUtils.loadAnimation(this, R.anim.button_click)
        
        binding.layoutHero.btnSearchSnack.setOnClickListener {
            it.startAnimation(clickAnim)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.layoutMood.chipHappy.setOnClickListener { 
            it.startAnimation(clickAnim)
            navigateToRecommendation("Senang") 
        }
        binding.layoutMood.chipSad.setOnClickListener { 
            it.startAnimation(clickAnim)
            navigateToRecommendation("Sedih") 
        }
        binding.layoutMood.chipAngry.setOnClickListener { 
            it.startAnimation(clickAnim)
            navigateToRecommendation("Marah") 
        }
        binding.layoutMood.chipBored.setOnClickListener {
            it.startAnimation(clickAnim)
            navigateToRecommendation("Bosan")
        }
    }

    private fun navigateToRecommendation(mood: String) {
        val intent = Intent(this, RecommendationActivity::class.java).apply {
            putExtra("MOOD_EXTRA", mood)
        }
        startActivity(intent)
    }
}
