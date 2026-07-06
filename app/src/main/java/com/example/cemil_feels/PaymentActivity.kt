package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityPaymentBinding

/**
 * Aktivitas Payment Selection Screen (Page 8).
 * Menangani pemilihan metode pembayaran (Bank, E-Wallet, atau QRIS) dan mengecek keberadaan aplikasi E-Wallet.
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment = 23000.0
    private var selectedMethod = "ShopeePay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnPaymentBack.setOnClickListener {
            finish()
        }

        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)

        // Setup E-Wallet Click Listeners
        binding.btnWalletShopee.setOnClickListener {
            selectedMethod = "ShopeePay"
            binding.btnActionPay.text = "Bayar Dengan Shopee Pay"
            Toast.makeText(this, "ShopeePay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletGopay.setOnClickListener {
            selectedMethod = "GoPay"
            binding.btnActionPay.text = "Bayar Dengan GoPay"
            Toast.makeText(this, "GoPay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletDana.setOnClickListener {
            selectedMethod = "DANA"
            binding.btnActionPay.text = "Bayar Dengan DANA"
            Toast.makeText(this, "DANA Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletOvo.setOnClickListener {
            selectedMethod = "OVO"
            binding.btnActionPay.text = "Bayar Dengan OVO"
            Toast.makeText(this, "OVO Terpilih", Toast.LENGTH_SHORT).show()
        }

        // Tombol QRIS bulat
        binding.btnPaymentQris.setOnClickListener {
            goToQrisScreen()
        }

        // Tombol aksi di bawah: "Bayar Dengan ..."
        binding.btnActionPay.setOnClickListener {
            val packageName = when (selectedMethod) {
                "ShopeePay" -> "com.shopee.id"
                "GoPay" -> "com.gojek.app"
                "DANA" -> "id.dana"
                "OVO" -> "id.ovo"
                else -> ""
            }

            if (packageName.isNotEmpty()) {
                checkAndProcessWallet(packageName)
            } else {
                goToQrisScreen()
            }
        }
    }

    private fun checkAndProcessWallet(targetPackage: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (launchIntent != null) {
            // Aplikasi terinstal -> Mock buka e-wallet dan berpindah ke Konfirmasi
            Toast.makeText(this, "Membuka aplikasi E-Wallet...", Toast.LENGTH_SHORT).show()
            goToConfirmationScreen()
        } else {
            // Aplikasi TIDAK terinstal -> Alihkan ke QRIS
            Toast.makeText(this, "Aplikasi E-Wallet tidak ditemukan secara lokal. Beralih ke QRIS.", Toast.LENGTH_LONG).show()
            goToQrisScreen()
        }
    }

    private fun goToConfirmationScreen() {
        val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
            putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
            putExtra("PAYMENT_METHOD_EXTRA", selectedMethod)
        }
        startActivity(intent)
    }

    private fun goToQrisScreen() {
        val intent = Intent(this, QrisActivity::class.java).apply {
            putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
        }
        startActivity(intent)
    }
}
