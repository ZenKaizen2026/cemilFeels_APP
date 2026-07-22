package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.PaymentUiEvent
import com.example.cemil_feels.viewmodel.PaymentViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment = 23000.0
    private var customerName = "Pelanggan CemilFeels"

    private val viewModel: PaymentViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    // ✅ ActivityResultLauncher untuk menerima hasil dari Midtrans Snap UI
    private val snapPaymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleSnapPaymentResult(result)
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

        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)
        customerName = intent.getStringExtra("CUSTOMER_NAME_EXTRA") ?: "Pelanggan CemilFeels"

        // ✅ Inisialisasi Midtrans SDK (HARUS sebelum tombol bayar diklik)
        initMidtransSDK()

        setupClickListeners()
        setupObservers()
    }

    private fun initMidtransSDK() {
        UiKitApi.Builder()
            // ✅ GANTI dengan Client Key Sandbox kamu dari Midtrans Dashboard
            .withMerchantClientKey("SB-Mid-client-XXXXXXXXXXXX")
            // ✅ GANTI dengan URL Render.com kamu — WAJIB diakhiri "/"
            .withContext(applicationContext)
            .withMerchantUrl("https://cemilfeels-api.onrender.com/")
            .enableLog(true) // Set false untuk production
            .withColorTheme(CustomColorTheme("#FF7A1A", "#E56A10", "#FF7A1A"))
            .build()
    }

    private fun setupClickListeners() {
        binding.btnPaymentBack.setOnClickListener { finish() }

        // --- E-Wallet Click Listeners ---
        binding.btnWalletShopee.setOnClickListener  { viewModel.selectMethod("ShopeePay") }
        binding.btnWalletGopay.setOnClickListener   { viewModel.selectMethod("GoPay") }
        binding.btnWalletDana.setOnClickListener    { viewModel.selectMethod("DANA") }
        binding.btnWalletOvo.setOnClickListener     { viewModel.selectMethod("OVO") }
        binding.btnWalletJago.setOnClickListener    { viewModel.selectMethod("Jago") }
        binding.btnWalletLinkaja.setOnClickListener { viewModel.selectMethod("LinkAja") }

        binding.btnPaymentQris.setOnClickListener   { viewModel.selectMethod("QRIS") }

        // ✅ Tombol Bayar — sekarang request Snap Token ke server
        binding.btnActionPay.setOnClickListener {
            viewModel.savePaymentMethod()
            // Request Snap Token dari Merchant Server → buka Midtrans Snap UI
            viewModel.requestSnapToken(totalPayment, customerName)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.selectedMethod.collect { method ->
                binding.btnActionPay.text = "Bayar Rp ${totalPayment.toInt()} dengan $method"
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Tampilkan/sembunyikan loading indicator
                binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.btnActionPay.isEnabled = !isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is PaymentUiEvent.LaunchMidtransSnap -> {
                        launchMidtransSnap(event.snapToken)
                    }
                    is PaymentUiEvent.ShowError -> {
                        Toast.makeText(this@PaymentActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                    else -> { /* handled above */ }
                }
            }
        }
    }

    /**
     * Meluncurkan Midtrans Snap UI dengan token yang sudah didapatkan dari server.
     */
    private fun launchMidtransSnap(snapToken: String) {
        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            activity = this,
            launcher = snapPaymentLauncher,
            snapToken = snapToken
        )
    }

    /**
     * Menangani hasil pembayaran dari Midtrans Snap UI.
     */
    private fun handleSnapPaymentResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val transactionResult = result.data?.getParcelableExtra<TransactionResult>(
                "UiKitConstants.key_transaction_result"
            )

            when (transactionResult?.status) {
                "success" -> {
                    // ✅ Pembayaran berhasil
                    Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                        putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                        putExtra("PAYMENT_METHOD_EXTRA", viewModel.selectedMethod.value)
                        putExtra("TRANSACTION_ID", transactionResult.transactionId)
                    }
                    startActivity(intent)
                    finish()
                }

                "pending" -> {
                    // ⏳ Pembayaran pending (misal: bank transfer, menunggu konfirmasi)
                    Toast.makeText(this, "Pembayaran Pending - cek status di aplikasi", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                        putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                        putExtra("PAYMENT_METHOD_EXTRA", viewModel.selectedMethod.value)
                    }
                    startActivity(intent)
                    finish()
                }

                "failed" -> {
                    // ❌ Pembayaran gagal
                    Toast.makeText(this, "Pembayaran Gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                }

                "canceled" -> {
                    // 🚫 User membatalkan pembayaran
                    Toast.makeText(this, "Pembayaran dibatalkan.", Toast.LENGTH_SHORT).show()
                }

                "invalid" -> {
                    // ⚠️ Token tidak valid / sudah expired
                    Toast.makeText(this, "Sesi pembayaran tidak valid. Silakan ulangi.", Toast.LENGTH_SHORT).show()
                }

                null -> {
                    // User back tanpa melakukan apa-apa
                }

                else -> {
                    Toast.makeText(this, "Status: ${transactionResult.status}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
