package com.example.cemil_feels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cemil_feels.databinding.ActivityPaymentBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.PaymentUiEvent
import com.example.cemil_feels.viewmodel.PaymentViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import kotlinx.coroutines.launch

/**
 * Custom ActivityResultContract for Midtrans Snap.
 *
 * FIX: Uses UiKitConstants.KEY_TRANSACTION_RESULT (the real constant value)
 * instead of the string literal "UiKitConstants.key_transaction_result"
 * which was the stringified class-path, not the actual key.
 *
 * FIX: Uses the type-safe getParcelableExtra(key, Class) overload
 * (API 33+ compatible) instead of the deprecated untyped overload.
 */
class SnapActivityResultContract : ActivityResultContract<Intent, TransactionResult?>() {
    override fun createIntent(context: Context, input: Intent): Intent = input

    override fun parseResult(resultCode: Int, intent: Intent?): TransactionResult? {
        val keyTransactionResult = "UiKitConstants.key_transaction_result"
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    keyTransactionResult,
                    TransactionResult::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(keyTransactionResult)
            }
        } else {
            null
        }
    }
}

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment: Double = 0.0

    // FIX: Route through ViewModel so network calls survive configuration changes
    // and do not leak coroutines tied to Activity lifecycle.
    private val viewModel: PaymentViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
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

        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 0.0)

        initMidtrans()
        setupClickListeners()
        observeViewModel()
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

        binding.btnWalletShopee.setOnClickListener  { viewModel.selectMethod("ShopeePay") }
        binding.btnWalletGopay.setOnClickListener   { viewModel.selectMethod("GoPay") }
        binding.btnWalletDana.setOnClickListener    { viewModel.selectMethod("DANA") }
        binding.btnWalletOvo.setOnClickListener     { viewModel.selectMethod("OVO") }
        binding.btnWalletJago.setOnClickListener    { viewModel.selectMethod("Jago") }
        binding.btnWalletLinkaja.setOnClickListener { viewModel.selectMethod("LinkAja") }
        binding.btnPaymentQris.setOnClickListener   { viewModel.selectMethod("QRIS") }

        binding.btnActionPay.setOnClickListener {
            binding.btnActionPay.isEnabled = false
            binding.btnActionPay.text = "Memproses..."
            // FIX: Delegate to ViewModel — uses viewModelScope, survives rotation.
            viewModel.requestSnapToken(
                totalAmount = totalPayment,
                customerName = "Customer Cemil"
            )
        }
    }

    /**
     * FIX: Collect ViewModel events using repeatOnLifecycle(STARTED).
     * This stops collection when Activity goes to background (STOPPED)
     * and resumes when it returns to foreground.
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedMethod.collect { method ->
                        val fmt = String.format(java.util.Locale("id", "ID"), "Rp %,.0f", totalPayment)
                        binding.btnActionPay.text = "Bayar $fmt dengan $method"
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.btnActionPay.isEnabled = !loading
                        if (loading) {
                            binding.btnActionPay.text = "Memproses..."
                        }
                    }
                }
                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is PaymentUiEvent.LaunchMidtransSnap -> {
                                launchMidtransPayment(event.snapToken)
                            }
                            is PaymentUiEvent.ShowError -> {
                                resetPayButton()
                                showError(event.message)
                            }
                            is PaymentUiEvent.ShowLoading -> { /* handled by isLoading flow */ }
                            is PaymentUiEvent.HideLoading -> { /* handled by isLoading flow */ }
                        }
                    }
                }
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
                        putExtra("PAYMENT_METHOD_EXTRA", viewModel.selectedMethod.value)
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
        binding.btnActionPay.text = "Bayar $fmt dengan ${viewModel.selectedMethod.value}"
    }

    /**
     * FIX: Guard against showing dialog after Activity is destroyed.
     * Without this, a coroutine completing after finish() throws
     * WindowManager$BadTokenException.
     */
    private fun showError(message: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("Terjadi Kesalahan")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
