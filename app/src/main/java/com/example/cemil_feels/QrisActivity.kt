package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityQrisBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Aktivitas QRIS Screen (Page 12).
 * Menampilkan barcode QRIS dinamis berbasis total biaya dari checkout.
 */
class QrisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrisBinding
    private var totalPayment = 23000.0
    private var countdownJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityQrisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnQrisBack.setOnClickListener {
            finish()
        }

        // Baca Grand Total dari checkout
        totalPayment = AppState.lastOrderTotalCost

        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        binding.tvQrisTotalAmount.text = "Rp. " + formatter.format(totalPayment.toInt())

        binding.btnQrisDownload.setOnClickListener {
            // Hentikan timer karena pembayaran telah dilanjutkan
            countdownJob?.cancel()
            
            Toast.makeText(this, "QR Code Berhasil Diunduh!", Toast.LENGTH_SHORT).show()
            // Setelah download, mock proses konfirmasi pembayaran otomatis (Page 9)
            val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                putExtra("PAYMENT_METHOD_EXTRA", "QRIS")
            }
            startActivity(intent)
        }

        binding.btnQrisHome.setOnClickListener {
            countdownJob?.cancel()
            // Kembali ke Dashboard Home (Page 3)
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Mulai hitung mundur 15 detik
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        countdownJob = lifecycleScope.launch {
            for (seconds in 15 downTo 0) {
                val formattedTime = String.format(Locale.getDefault(), "00:00:%02d", seconds)
                binding.tvQrisTimer.text = formattedTime
                
                if (seconds == 0) {
                    // 1. Kosongkan keranjang belanja global
                    AppState.cart.clear()
                    
                    Toast.makeText(this@QrisActivity, "Waktu pembayaran QRIS habis! Keranjang dikosongkan.", Toast.LENGTH_LONG).show()
                    
                    // 2. Alihkan kembali ke halaman pemilihan pembayaran
                    val intent = Intent(this@QrisActivity, PaymentActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownJob?.cancel()
    }
}
