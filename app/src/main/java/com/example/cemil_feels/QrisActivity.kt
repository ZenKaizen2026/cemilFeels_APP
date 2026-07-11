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
import com.example.cemil_feels.databinding.ActivityQrisBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.QrisViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas QRIS Screen (Page 12).
 * Menampilkan barcode QRIS dinamis berbasis total biaya dari checkout.
 * Refactored to follow MVVM architecture.
 */
class QrisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrisBinding

    private val viewModel: QrisViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

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

        binding.btnQrisDownload.setOnClickListener {
            viewModel.onDownloadQris()
        }

        binding.btnQrisHome.setOnClickListener {
            viewModel.onHomeClicked()
        }

        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.totalPaymentText.collect { amountText ->
                binding.tvQrisTotalAmount.text = amountText
            }
        }

        lifecycleScope.launch {
            viewModel.formattedTime.collect { timeText ->
                binding.tvQrisTimer.text = timeText
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                // Sync legacy AppState before navigating
                syncOrderAndCartToLegacyAppState()
                
                when (event) {
                    is QrisViewModel.QrisEvent.Timeout -> {
                        Toast.makeText(this@QrisActivity, "Waktu pembayaran QRIS habis! Keranjang dikosongkan.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@QrisActivity, PaymentActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                    is QrisViewModel.QrisEvent.NavigateToConfirmation -> {
                        Toast.makeText(this@QrisActivity, "QR Code Berhasil Diunduh!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@QrisActivity, PaymentConfirmationActivity::class.java).apply {
                            putExtra("TOTAL_PAYMENT_EXTRA", ServiceLocator.container.orderRepository.getLastOrderTotalCost())
                            putExtra("PAYMENT_METHOD_EXTRA", "QRIS")
                        }
                        startActivity(intent)
                    }
                    is QrisViewModel.QrisEvent.NavigateToHome -> {
                        val intent = Intent(this@QrisActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun syncOrderAndCartToLegacyAppState() {
        AppState.cart.clear()
        AppState.cart.putAll(ServiceLocator.container.cartRepository.getCartMap())
        AppState.lastPaymentMethod = ServiceLocator.container.orderRepository.getLastPaymentMethod()
    }
}
