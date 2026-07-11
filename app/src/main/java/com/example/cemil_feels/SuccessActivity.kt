package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivitySuccessBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.SuccessViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory

/**
 * Aktivitas Payment Success Screen (Page 10).
 * Menampilkan ringkasan tanda receipt pembayaran berhasil dan navigasi ke beranda atau pelacakan pesanan.
 * Refactored to follow MVVM architecture.
 */
class SuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessBinding

    private val viewModel: SuccessViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil data pembayaran
        val totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)
        val method = intent.getStringExtra("PAYMENT_METHOD_EXTRA")

        binding.tvSuccessTotal.text = viewModel.getFormattedTotalCost(totalPayment)
        binding.tvSuccessMethod.text = viewModel.getPaymentMethod(method)
        binding.tvSuccessTime.text = viewModel.getFormattedDateTime()

        // Sync with legacy AppState
        AppState.lastPaymentMethod = viewModel.getPaymentMethod(method)

        // Tombol Lihat Pesanan Saya menuju OrderStatusActivity
        binding.btnSuccessViewOrder.setOnClickListener {
            val intent = Intent(this, OrderStatusActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol Kembali Ke Beranda menuju HomeActivity (Dashboard)
        binding.btnSuccessHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
