package com.example.cemil_feels

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
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
 * Redesigned for Premium UI/UX with Sliding Segmented Control.
 */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private var isDeliverySelected = true

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

        binding.layoutCheckoutHeader.btnCheckoutBack.setOnClickListener {
            finish()
        }

        setupObservers()
        setupSegmentedControl()

        val totalCart = intent.getDoubleExtra("TOTAL_CART_EXTRA", 16000.0)
        viewModel.calculateCheckoutDetails(totalCart)

        binding.btnSelectPayment.setOnClickListener {
            val totalCost = viewModel.uiState.value.totalCost
            AppState.lastOrderTotalCost = totalCost

            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalCost)
            }
            startActivity(intent)
        }

        binding.btnChangeAddress.setOnClickListener {
            Toast.makeText(this, "Fitur Ubah Alamat akan segera hadir!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSegmentedControl() {
        binding.tvToggleDelivery.setOnClickListener {
            if (!isDeliverySelected) {
                switchDeliveryMethod(true)
            }
        }

        binding.tvTogglePickup.setOnClickListener {
            if (isDeliverySelected) {
                switchDeliveryMethod(false)
            }
        }
        
        // Initial state
        binding.segmentedContainer.post {
            updateIndicatorPosition(true, animate = false)
        }
    }

    private fun switchDeliveryMethod(isDelivery: Boolean) {
        isDeliverySelected = isDelivery
        
        // 1. Animate Indicator
        updateIndicatorPosition(isDelivery, animate = true)

        // 2. Update Text Colors
        binding.tvToggleDelivery.setTextColor(if (isDelivery) Color.parseColor("#FF7A1A") else Color.parseColor("#757575"))
        binding.tvTogglePickup.setTextColor(if (!isDelivery) Color.parseColor("#FF7A1A") else Color.parseColor("#757575"))

        // 3. Update Visibility & Content
        if (isDelivery) {
            binding.cardAddress.visibility = View.VISIBLE
            binding.cardPickupLocation.visibility = View.GONE
            binding.layoutOrderSummary.layoutDeliveryFee.visibility = View.VISIBLE

            binding.tvDeliveryLabel.text = "Estimasi Tiba"
            binding.tvDeliveryEta.text = "20 - 30 menit"
            binding.ivDeliveryIcon.setImageResource(R.drawable.logo_checkout_delivery)
        } else {
            binding.cardAddress.visibility = View.GONE
            binding.cardPickupLocation.visibility = View.VISIBLE
            binding.layoutOrderSummary.layoutDeliveryFee.visibility = View.GONE

            binding.tvDeliveryLabel.text = "Waktu Pengambilan"
            binding.tvDeliveryEta.text = "Siap dalam 15 - 20 menit"
            binding.ivDeliveryIcon.setImageResource(R.drawable.logo_checkout_restaurant)
        }
    }

    private fun updateIndicatorPosition(isDelivery: Boolean, animate: Boolean) {
        val targetX = if (isDelivery) 0f else binding.segmentedContainer.width / 2f
        
        if (animate) {
            binding.viewIndicator.animate().cancel()
            binding.viewIndicator.animate()
                .translationX(targetX)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            binding.viewIndicator.translationX = targetX
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.layoutOrderSummary.tvCheckoutSubtotal.text = state.subtotalText
                binding.layoutOrderSummary.tvCheckoutTotal.text = state.totalText

                binding.layoutOrderSummary.layoutOrderItemsContainer.removeAllViews()
                state.cartItems.forEach { item ->
                    val itemView = layoutInflater.inflate(R.layout.item_checkout_summary, binding.layoutOrderSummary.layoutOrderItemsContainer, false)

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

                    binding.layoutOrderSummary.layoutOrderItemsContainer.addView(itemView)
                }
            }
        }
    }
}