package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cemil_feels.databinding.ActivityCartBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Aktivitas Shopping Cart Screen (Page 6).
 * Menampilkan barang-barang yang ada di keranjang belanja secara dinamis menggunakan RecyclerView.
 */
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter

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
        refreshCartData()

        // Tombol checkout menuju CheckoutActivity
        binding.btnCartCheckout.setOnClickListener {
            var totalCartAmount = 0.0
            AppState.cart.forEach { (snackName, qty) ->
                val snackObj = RecommendationActivity.STATIC_SNACKS.find { it.name == snackName }
                if (snackObj != null) {
                    totalCartAmount += qty * snackObj.price
                }
            }

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("TOTAL_CART_EXTRA", totalCartAmount)
            }
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onPlusClicked = { item ->
                AppState.cart[item.snack.name] = item.qty + 1
                refreshCartData()
            },
            onMinusClicked = { item ->
                if (item.qty > 1) {
                    AppState.cart[item.snack.name] = item.qty - 1
                    refreshCartData()
                }
            },
            onRemoveClicked = { item ->
                AppState.cart.remove(item.snack.name)
                refreshCartData()
                Toast.makeText(this, "${item.snack.name} dihapus.", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun refreshCartData() {
        // Konversi map in-memory ke list CartItem
        val cartItemsList = AppState.cart.mapNotNull { (snackName, qty) ->
            val snackObj = RecommendationActivity.STATIC_SNACKS.find { it.name == snackName }
            if (snackObj != null) {
                CartItem(snackObj, qty)
            } else {
                null
            }
        }

        // Tampilkan item ke adapter
        cartAdapter.submitList(cartItemsList)
        cartAdapter.notifyDataSetChanged()

        // Hitung total belanja
        var subtotal = 0.0
        cartItemsList.forEach { item ->
            subtotal += item.qty * item.snack.price
        }

        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        binding.tvCartTotal.text = "Rp. " + formatter.format(subtotal.toInt())

        // Cek jika keranjang menjadi kosong (FT-03 / Aturan Redireksi)
        if (cartItemsList.isEmpty()) {
            binding.btnCartCheckout.isEnabled = false
            Toast.makeText(this, "Keranjang belanja kosong! Silakan pilih kembali camilan Anda.", Toast.LENGTH_LONG).show()
            
            // Redirect ke RecommendationActivity
            val intent = Intent(this, RecommendationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        } else {
            binding.btnCartCheckout.isEnabled = true
        }
    }
}
