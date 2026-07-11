package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityCheckoutBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.CheckoutViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Checkout Screen (Page 7).
 * Menampilkan alamat, estimasi durasi, ringkasan belanja, dan rincian biaya.
 * Refactored to follow MVVM architecture.
 */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private val viewModel: CheckoutViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCheckoutBack.setOnClickListener {
            finish()
        }

        setupObservers()

        val totalCart = intent.getDoubleExtra("TOTAL_CART_EXTRA", 16000.0)
        viewModel.calculateCheckoutDetails(totalCart)

        binding.btnSelectPayment.setOnClickListener {
            val totalCost = viewModel.uiState.value.totalCost
            // Sync with legacy AppState just in case
            AppState.lastOrderTotalCost = totalCost

            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalCost)
            }
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvCheckoutSubtotal.text = state.subtotalText
                binding.tvCheckoutTotal.text = state.totalText
                binding.tvSummaryItemName.text = state.firstItemName
                binding.tvSummaryItemPriceQty.text = state.firstItemPriceQty

                if (state.firstItemImageResId != 0) {
                    binding.ivSummaryItemImage.setImageResource(state.firstItemImageResId)
                } else {
                    binding.ivSummaryItemImage.setImageResource(R.drawable.basreng_stik)
                }

                if (state.isSpiceLevelVisible) {
                    binding.tvSummaryItemLevel.visibility = View.VISIBLE
                    binding.tvSummaryItemLevel.text = state.spiceLevelText
                } else {
                    binding.tvSummaryItemLevel.visibility = View.GONE
                }
            }
        }
    }
}
