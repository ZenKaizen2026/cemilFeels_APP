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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cemil_feels.databinding.ActivityCartBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.CartViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Shopping Cart Screen (Page 6).
 * Menampilkan barang-barang yang ada di keranjang belanja secara dinamis menggunakan RecyclerView.
 * Refactored to follow MVVM architecture.
 */
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter

    private val viewModel: CartViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCartBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupObservers()

        binding.btnCartCheckout.setOnClickListener {
            syncCartToLegacyAppState()
            viewModel.onCheckoutClicked()
        }
        
        // Initial empty check
        viewModel.checkEmptyAndTriggerRedirect()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onPlusClicked = { item ->
                viewModel.incrementItem(item)
                syncCartToLegacyAppState()
            },
            onMinusClicked = { item ->
                viewModel.decrementItem(item)
                syncCartToLegacyAppState()
            },
            onRemoveClicked = { item ->
                viewModel.removeItem(item)
                syncCartToLegacyAppState()
                Toast.makeText(this, "${item.snack.name} dihapus.", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.cartItemsState.collect { items ->
                cartAdapter.submitList(items)
                cartAdapter.notifyDataSetChanged()
            }
        }

        lifecycleScope.launch {
            viewModel.formattedSubtotalState.collect { totalText ->
                binding.tvCartTotal.text = totalText
            }
        }

        lifecycleScope.launch {
            viewModel.isCheckoutEnabledState.collect { isEnabled ->
                binding.btnCartCheckout.isEnabled = isEnabled
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is CartViewModel.CartEvent.RedirectToRecommendation -> {
                        Toast.makeText(this@CartActivity, "Keranjang belanja kosong! Silakan pilih kembali camilan Anda.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@CartActivity, RecommendationActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                    is CartViewModel.CartEvent.NavigateToCheckout -> {
                        val intent = Intent(this@CartActivity, CheckoutActivity::class.java).apply {
                            putExtra("TOTAL_CART_EXTRA", event.subtotal)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun syncCartToLegacyAppState() {
        AppState.cart.clear()
        AppState.cart.putAll(ServiceLocator.container.cartRepository.getCartMap())
    }
}
