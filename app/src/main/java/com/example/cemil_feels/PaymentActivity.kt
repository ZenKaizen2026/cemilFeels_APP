package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.PaymentViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Payment Selection Screen (Page 8).
 * Menangani pemilihan metode pembayaran (Bank, E-Wallet, atau QRIS) dan mengecek keberadaan aplikasi E-Wallet.
 * Refactored to follow MVVM architecture.
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment = 23000.0

    private val viewModel: PaymentViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

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
            viewModel.selectMethod("ShopeePay")
            Toast.makeText(this, "ShopeePay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletGopay.setOnClickListener {
            viewModel.selectMethod("GoPay")
            Toast.makeText(this, "GoPay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletDana.setOnClickListener {
            viewModel.selectMethod("DANA")
            Toast.makeText(this, "DANA Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletOvo.setOnClickListener {
            viewModel.selectMethod("OVO")
            Toast.makeText(this, "OVO Terpilih", Toast.LENGTH_SHORT).show()
        }

        // Tombol QRIS bulat
        binding.btnPaymentQris.setOnClickListener {
            viewModel.selectMethod("QRIS")
            goToQrisScreen()
        }

        // Tombol aksi di bawah: "Bayar Dengan ..."
        binding.btnActionPay.setOnClickListener {
            viewModel.savePaymentMethod()
            syncOrderToLegacyAppState()
            
            val packageName = viewModel.getTargetPackageName()
            if (packageName.isNotEmpty()) {
                checkAndProcessWallet(packageName)
            } else {
                goToQrisScreen()
            }
        }

        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.selectedMethod.collect { method ->
                binding.btnActionPay.text = "Bayar Dengan $method"
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
            putExtra("PAYMENT_METHOD_EXTRA", viewModel.selectedMethod.value)
        }
        startActivity(intent)
    }

    private fun goToQrisScreen() {
        val intent = Intent(this, QrisActivity::class.java).apply {
            putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
        }
        startActivity(intent)
    }

    private fun syncOrderToLegacyAppState() {
        val orderRepository = ServiceLocator.container.orderRepository
        AppState.lastPaymentMethod = orderRepository.getLastPaymentMethod()
    }
}
