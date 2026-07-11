package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentConfirmationBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.PaymentConfirmationViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Payment Confirmation Screen (Page 9).
 * Menampilkan pesan pemrosesan pembayaran dan loading spinner sebelum mengalihkan otomatis ke layar sukses.
 * Refactored to follow MVVM architecture.
 */
class PaymentConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentConfirmationBinding

    private val viewModel: PaymentConfirmationViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

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

        setupObservers()

        val totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD_EXTRA")

        viewModel.startConfirmationTimer(totalPayment, paymentMethod)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is PaymentConfirmationViewModel.ConfirmationEvent.NavigateToSuccess -> {
                        // Sync legacy AppState
                        AppState.lastPaymentMethod = event.paymentMethod
                        AppState.lastOrderTotalCost = event.totalPayment

                        val intent = Intent(this@PaymentConfirmationActivity, SuccessActivity::class.java).apply {
                            putExtra("TOTAL_PAYMENT_EXTRA", event.totalPayment)
                            putExtra("PAYMENT_METHOD_EXTRA", event.paymentMethod)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}
