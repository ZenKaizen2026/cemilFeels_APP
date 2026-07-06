package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentConfirmationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Aktivitas Payment Confirmation Screen (Page 9).
 * Menampilkan pesan pemrosesan pembayaran dan loading spinner sebelum mengalihkan otomatis ke layar sukses.
 */
class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding
    private var totalPayment = 23000.0
    private var paymentMethod = "ShopeePay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaymentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)
        paymentMethod = intent.getStringExtra("PAYMENT_METHOD_EXTRA") ?: "ShopeePay"

        // Jalankan coroutine untuk simulasi delay konfirmasi selama 3 detik
        lifecycleScope.launch {
            delay(3000)
            
            // Berpindah ke SuccessActivity
            val intent = Intent(this@PaymentConfirmationActivity, SuccessActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                putExtra("PAYMENT_METHOD_EXTRA", paymentMethod)
            }
            startActivity(intent)
            finish()
        }
    }
}
