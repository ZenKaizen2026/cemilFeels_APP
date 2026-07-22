package com.example.cemil_feels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentBinding
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Custom ActivityResultContract for Midtrans Snap
 */
class SnapActivityResultContract : ActivityResultContract<Intent, TransactionResult?>() {
    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): TransactionResult? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            // "UiKitConstants.key_transaction_result" is the internal key used by Midtrans SDK
            intent.getParcelableExtra("UiKitConstants.key_transaction_result")
        } else {
            null
        }
    }
}

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment: Double = 0.0
    private var selectedMethod: String = "ShopeePay"

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

        // Ambil total dari Intent
        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 0.0)

        initMidtrans()
        setupClickListeners()
    }

    private fun initMidtrans() {
        UiKitApi.Builder()
            .withContext(applicationContext)
            .withMerchantUrl(BuildConfig.MERCHANT_BASE_URL)
            .withMerchantClientKey(BuildConfig.MIDTRANS_CLIENT_KEY)
            .enableLog(true)
            .withColorTheme(CustomColorTheme("#FF6B35", "#E5602F", "#FF6B35"))
            .build()
    }

    private fun setupClickListeners() {
        binding.btnPaymentBack.setOnClickListener { finish() }

        binding.btnWalletShopee.setOnClickListener  { selectMethod("ShopeePay") }
        binding.btnWalletGopay.setOnClickListener   { selectMethod("GoPay") }
        binding.btnWalletDana.setOnClickListener    { selectMethod("DANA") }
        binding.btnWalletOvo.setOnClickListener     { selectMethod("OVO") }
        binding.btnWalletJago.setOnClickListener    { selectMethod("Jago") }
        binding.btnWalletLinkaja.setOnClickListener { selectMethod("LinkAja") }
        binding.btnPaymentQris.setOnClickListener   { selectMethod("QRIS") }

        binding.btnActionPay.setOnClickListener {
            binding.btnActionPay.isEnabled = false
            binding.btnActionPay.text = "Memproses..."
            
            if (selectedMethod == "QRIS") {
                // Bypass Midtrans langsung ke PaymentConfirmationActivity untuk metode QRIS
                val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                    putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                    putExtra("PAYMENT_METHOD_EXTRA", "QRIS")
                    putExtra("TRANSACTION_ID_EXTRA", "MOCK-QRIS-${UUID.randomUUID().toString().take(8).uppercase()}")
                }
                startActivity(intent)
                finish()
            } else {
                requestSnapToken()
            }
        }
    }

    private fun selectMethod(method: String) {
        selectedMethod = method
        val fmt = String.format(java.util.Locale("id", "ID"), "Rp %,.0f", totalPayment)
        binding.btnActionPay.text = "Bayar $fmt dengan $method"
        Toast.makeText(this, "$method terpilih", Toast.LENGTH_SHORT).show()
    }

    private fun requestSnapToken() {
        val orderId = "CEMIL-${UUID.randomUUID().toString().take(8).uppercase()}"

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.merchantApiService.getSnapToken(
                        SnapTokenRequest(
                            order_id      = orderId,
                            amount        = totalPayment.toLong(),
                            customer_name = "Customer Cemil"
                        )
                    )
                }

                if (response.success && !response.token.isNullOrBlank()) {
                    launchMidtransPayment(response.token)
                } else {
                    resetPayButton()
                    showError("Gagal mendapatkan token. Coba lagi.")
                }

            } catch (e: Exception) {
                resetPayButton()
                showError("Tidak bisa terhubung ke server:\n${e.message}")
            }
        }
    }

    // Perbaikan Launcher
    private val snapLauncher = registerForActivityResult(SnapActivityResultContract()) { result ->
        if (result == null) {
            resetPayButton()
            return@registerForActivityResult
        }

        when (result.status) {
            "canceled" -> {
                resetPayButton()
                Toast.makeText(this, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
            }
            "success", "settlement", "pending" -> {
                startActivity(
                    Intent(this, PaymentConfirmationActivity::class.java).apply {
                        putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                        putExtra("PAYMENT_METHOD_EXTRA", selectedMethod)
                        putExtra("TRANSACTION_ID_EXTRA", result.transactionId ?: "")
                    }
                )
                finish()
            }
            else -> {
                resetPayButton()
                showError("Pembayaran gagal.\nStatus: ${result.status}")
            }
        }
    }

    private fun launchMidtransPayment(snapToken: String) {
        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            activity  = this,
            launcher  = snapLauncher,
            snapToken = snapToken
        )
    }

    private fun resetPayButton() {
        binding.btnActionPay.isEnabled = true
        val fmt = String.format(java.util.Locale("id", "ID"), "Rp %,.0f", totalPayment)
        binding.btnActionPay.text = "Bayar $fmt dengan $selectedMethod"
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Terjadi Kesalahan")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}