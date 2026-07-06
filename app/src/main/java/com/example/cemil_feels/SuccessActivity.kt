package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivitySuccessBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Aktivitas Payment Success Screen (Page 10).
 * Menampilkan ringkasan tanda terima pembayaran berhasil dan navigasi ke beranda atau pelacakan pesanan.
 */
class SuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessBinding

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
        val method = intent.getStringExtra("PAYMENT_METHOD_EXTRA") ?: "ShopeePay"
        AppState.lastPaymentMethod = method

        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        binding.tvSuccessTotal.text = "Rp. " + formatter.format(totalPayment.toInt())
        binding.tvSuccessMethod.text = method

        // Tampilkan tanggal & waktu transaksi saat ini
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH.mm", Locale.forLanguageTag("id-ID"))
        binding.tvSuccessTime.text = sdf.format(Calendar.getInstance().time)

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
