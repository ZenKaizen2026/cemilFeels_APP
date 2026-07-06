package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityCheckoutBinding
import java.text.NumberFormat
import java.util.Locale

/**
 * Aktivitas Checkout Screen (Page 7).
 * Menampilkan alamat, estimasi durasi, ringkasan belanja, dan rincian biaya.
 */
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private var subtotal = 16000.0
    private val shippingFee = 5000.0
    private val serviceFee = 2000.0

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

        // Hitung subtotal dinamis langsung dari AppState.cart
        var calculatedSubtotal = 0.0
        AppState.cart.forEach { (snackName, qty) ->
            val snackObj = RecommendationActivity.STATIC_SNACKS.find { it.name == snackName }
            if (snackObj != null) {
                calculatedSubtotal += qty * snackObj.price
            }
        }
        if (calculatedSubtotal > 0) {
            subtotal = calculatedSubtotal
        } else {
            val totalCart = intent.getDoubleExtra("TOTAL_CART_EXTRA", 16000.0)
            if (totalCart > 0) {
                subtotal = totalCart
            }
        }

        val totalCost = subtotal + shippingFee + serviceFee

        // Tampilkan teks terformat
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        binding.tvCheckoutSubtotal.text = "Rp. " + formatter.format(subtotal.toInt())
        binding.tvCheckoutTotal.text = "Rp. " + formatter.format(totalCost.toInt())
        
        // Bind dynamic item details from AppState
        val firstItem = AppState.cart.entries.firstOrNull()
        if (firstItem != null) {
            val snackName = firstItem.key
            val qty = firstItem.value
            val originalSnack = RecommendationActivity.STATIC_SNACKS.find { it.name == snackName }
            val itemPrice = originalSnack?.price ?: 16000.0

            val otherItemsCount = AppState.cart.size - 1
            if (otherItemsCount > 0) {
                binding.tvSummaryItemName.text = "${snackName} (+${otherItemsCount} item lainnya)"
            } else {
                binding.tvSummaryItemName.text = snackName
            }

            binding.tvSummaryItemPriceQty.text = "Rp. ${formatter.format(itemPrice.toInt())} ${qty} X"

            // 1. Set Image secara dinamis
            if (originalSnack != null) {
                binding.ivSummaryItemImage.setImageResource(originalSnack.imageResId)
            } else {
                binding.ivSummaryItemImage.setImageResource(R.drawable.basreng_stik)
            }

            // 2. Tampilkan level kepedasan hanya jika camilan termasuk kategori 'marah' (Basreng Stik atau Makaroni Bantet) (FT-02)
            val isSpicyCategory = snackName.equals("Basreng Stik", ignoreCase = true) ||
                    snackName.equals("Makaroni Bantet", ignoreCase = true)
                    
            if (isSpicyCategory) {
                binding.tvSummaryItemLevel.visibility = android.view.View.VISIBLE
                binding.tvSummaryItemLevel.text = "Level: ${AppState.lastOrderSpiceLevel}"
            } else {
                binding.tvSummaryItemLevel.visibility = android.view.View.GONE
            }
        }

        binding.btnSelectPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalCost)
            }
            startActivity(intent)
        }
    }
}
