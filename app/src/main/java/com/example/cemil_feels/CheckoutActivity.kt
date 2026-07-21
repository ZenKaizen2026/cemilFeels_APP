package com.example.cemil_feels

import android.content.Intent
import android.graphics.Color
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
        setupDeliveryToggle()

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

    private fun setupDeliveryToggle() {
        binding.toggleDeliveryMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_delivery -> {
                        // Tampilkan Alamat dan Ongkos Kirim, Sembunyikan Resto
                        binding.cardAddress.visibility = View.VISIBLE
                        binding.cardPickupLocation.visibility = View.GONE
                        binding.layoutDeliveryFee.visibility = View.VISIBLE

                        // Kembalikan Teks Estimasi ke semula
                        binding.tvDeliveryLabel.text = "Estimasi Tiba"
                        binding.tvDeliveryEta.text = "Akan sampai 20 - 30 menit"

                        // Ubah styling button
                        binding.btnDelivery.setTextColor(Color.parseColor("#FF7A1A"))
                        binding.btnPickup.setTextColor(Color.parseColor("#757575"))
                    }
                    R.id.btn_pickup -> {
                        // Sembunyikan Alamat dan Ongkos Kirim, Tampilkan Resto
                        binding.cardAddress.visibility = View.GONE
                        binding.cardPickupLocation.visibility = View.VISIBLE
                        binding.layoutDeliveryFee.visibility = View.GONE

                        // Ubah Teks Estimasi untuk Pick Up
                        binding.tvDeliveryLabel.text = "Waktu Pengambilan"
                        binding.tvDeliveryEta.text = "Pesanan siap dalam 15 - 20 menit"

                        // Ubah styling button
                        binding.btnPickup.setTextColor(Color.parseColor("#FF7A1A"))
                        binding.btnDelivery.setTextColor(Color.parseColor("#757575"))
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvCheckoutSubtotal.text = state.subtotalText
                binding.tvCheckoutTotal.text = state.totalText

                // Update dynamic items
                binding.layoutOrderItemsContainer.removeAllViews()
                state.cartItems.forEach { item ->
                    val itemView = layoutInflater.inflate(R.layout.item_checkout_summary, binding.layoutOrderItemsContainer, false)

                    val ivImage = itemView.findViewById<android.widget.ImageView>(R.id.iv_summary_item_image)
                    val tvName = itemView.findViewById<android.widget.TextView>(R.id.tv_summary_item_name)
                    val tvLevel = itemView.findViewById<android.widget.TextView>(R.id.tv_summary_item_level)
                    val cvLevel = itemView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cv_summary_item_level)
                    val tvQty = itemView.findViewById<android.widget.TextView>(R.id.tv_summary_item_qty)
                    val tvPrice = itemView.findViewById<android.widget.TextView>(R.id.tv_summary_item_price)

                    ivImage.setImageResource(item.imageResId)
                    tvName.text = item.name
                    tvQty.text = item.qtyText
                    tvPrice.text = item.priceText

                    if (item.isSpiceLevelVisible) {
                        cvLevel.visibility = View.VISIBLE
                        tvLevel.text = item.spiceLevelText
                    } else {
                        cvLevel.visibility = View.GONE
                    }

                    binding.layoutOrderItemsContainer.addView(itemView)
                }
            }
        }
    }
}