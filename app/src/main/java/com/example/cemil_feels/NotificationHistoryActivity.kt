package com.example.cemil_feels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityNotificationHistoryBinding
import com.google.android.material.card.MaterialCardView

class NotificationHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadNotifications()
    }

    private fun loadNotifications() {
        val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
        val historyStr = sharedPrefs.getString("notification_history", "") ?: ""

        if (historyStr.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            return
        }

        val notifications = historyStr.split("||").filter { it.isNotBlank() }.reversed()

        if (notifications.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            return
        }

        for (notif in notifications) {
            val parts = notif.split(";;")
            if (parts.size >= 3) {
                val title = parts[0]
                val msg = parts[1]
                val time = parts[2]
                addNotificationView(title, msg, time)
            }
        }
    }

    private fun addNotificationView(title: String, message: String, time: String) {
        // Create a simple MaterialCardView for each notification
        val card = MaterialCardView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24) // 8dp bottom margin
            }
            radius = 16f
            setCardBackgroundColor(resources.getColor(android.R.color.white, theme))
            cardElevation = 4f
            strokeWidth = 1
            strokeColor = resources.getColor(android.R.color.darker_gray, theme)
        }

        val linearLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32) // padding 12dp
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.textPrimary, theme))
        }

        val timeView = TextView(this).apply {
            text = time
            textSize = 12f
            setTextColor(resources.getColor(R.color.textSecondary, theme))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        val messageView = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(resources.getColor(R.color.textPrimary, theme))
        }

        linearLayout.addView(titleView)
        linearLayout.addView(timeView)
        linearLayout.addView(messageView)
        
        card.addView(linearLayout)
        binding.llNotificationsContainer.addView(card)
    }
}
