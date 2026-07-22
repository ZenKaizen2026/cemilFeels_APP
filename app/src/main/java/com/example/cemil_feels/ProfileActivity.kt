package com.example.cemil_feels

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.data.model.TransactionHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup Back Button
        findViewById<View>(R.id.btn_profile_back).setOnClickListener {
            finish()
        }

        // Load SharedPreferences
        val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
        val name = sharedPrefs.getString("registered_name", "Setia") ?: "Setia"
        val email = sharedPrefs.getString("registered_email", "user@example.com") ?: "user@example.com"
        val phone = sharedPrefs.getString("registered_phone", "-") ?: "-"
        val favorite = sharedPrefs.getString("registered_favorite", "-") ?: "-"

        // Update User Info UI
        findViewById<TextView>(R.id.tv_profile_name).text = name
        findViewById<TextView>(R.id.tv_profile_email).text = email
        findViewById<TextView>(R.id.tv_profile_phone).text = phone
        findViewById<TextView>(R.id.tv_profile_favorite).text = favorite

        // Setup Avatar Initial
        val initial = if (name.isNotEmpty()) name.take(1).uppercase(Locale.ROOT) else "U"
        findViewById<TextView>(R.id.tv_avatar_initial).text = initial

        // Load and display transaction history list
        displayTransactionHistory()
    }

    private fun displayTransactionHistory() {
        val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
        val historyJson = sharedPrefs.getString("transaction_history", "[]") ?: "[]"

        val gson = Gson()
        val type = object : TypeToken<List<TransactionHistory>>() {}.type
        val historyList: List<TransactionHistory> = try {
            gson.fromJson(historyJson, type)
        } catch (e: Exception) {
            emptyList()
        }

        val container = findViewById<LinearLayout>(R.id.layout_history_container)
        val emptyState = findViewById<TextView>(R.id.tv_history_empty_state)

        container.removeAllViews()

        if (historyList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
            
            // Tampilkan history paling baru di urutan paling atas
            val reversedList = historyList.reversed()

            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))

            for (tx in reversedList) {
                val itemView = layoutInflater.inflate(R.layout.item_history, container, false)

                val tvDate = itemView.findViewById<TextView>(R.id.tv_history_date)
                val tvMethod = itemView.findViewById<TextView>(R.id.tv_history_method)
                val tvItems = itemView.findViewById<TextView>(R.id.tv_history_items)
                val tvTotal = itemView.findViewById<TextView>(R.id.tv_history_total)

                tvDate.text = tx.dateTime
                tvMethod.text = tx.paymentMethod

                // Susun string detail barang belanja
                val itemsDetail = tx.items.joinToString("\n") { item ->
                    " (x)"
                }
                tvItems.text = itemsDetail

                val formattedTotal = "Rp. " + formatter.format(tx.totalCost.toInt())
                tvTotal.text = formattedTotal

                container.addView(itemView)
            }
        }
    }
}
