package com.example.cemil_feels

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityPaymentBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.PaymentViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Payment Selection Screen (Page 8).
 * Menangani pemilihan metode pembayaran (Bank, E-Wallet, atau QRIS).
 *
 * Fix UX Flow:
 * User klik "Bayar" → E-Wallet terbuka di foreground
 * PaymentConfirmationActivity ditempatkan di back stack terlebih dahulu
 * Saat user back dari E-Wallet → langsung mendarat di PaymentConfirmationActivity ✅
 *
 * Fix Package Visibility:
 * Membutuhkan tag <queries> di AndroidManifest.xml agar getLaunchIntentForPackage()
 * bekerja di Android 11+ (API 30+).
 */
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var totalPayment = 23000.0

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

        totalPayment = intent.getDoubleExtra("TOTAL_PAYMENT_EXTRA", 23000.0)

        binding.btnPaymentBack.setOnClickListener {
            finish()
        }

        // --- E-Wallet Click Listeners ---
        binding.btnWalletShopee.setOnClickListener {
            viewModel.selectMethod("ShopeePay")
            Toast.makeText(this, "ShopeePay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletGopay.setOnClickListener {
            viewModel.selectMethod("GoPay")
            Toast.makeText(this, "GoPay Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletDana.setOnClickListener {
            viewModel.selectMethod("DANA")
            Toast.makeText(this, "DANA Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletOvo.setOnClickListener {
            viewModel.selectMethod("OVO")
            Toast.makeText(this, "OVO Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletJago.setOnClickListener {
            viewModel.selectMethod("Jago")
            Toast.makeText(this, "Jago Terpilih", Toast.LENGTH_SHORT).show()
        }

        binding.btnWalletLinkaja.setOnClickListener {
            viewModel.selectMethod("LinkAja")
            Toast.makeText(this, "LinkAja Terpilih", Toast.LENGTH_SHORT).show()
        }

        // --- QRIS ---
        binding.btnPaymentQris.setOnClickListener {
            viewModel.selectMethod("QRIS")
            showQrisDialog()
        }

        // --- Tombol Bayar Utama ---
        binding.btnActionPay.setOnClickListener {
            viewModel.savePaymentMethod()
            syncOrderToLegacyAppState()

            val targetPackage = viewModel.getTargetPackageName()
            checkAndProcessWallet(targetPackage)
        }

        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.selectedMethod.collect { method ->
                binding.btnActionPay.text = "Bayar Dengan $method"
            }
        }
    }

    /**
     * Mengecek keberadaan aplikasi E-Wallet dan menyusun back stack yang benar.
     *
     * Urutan yang WAJIB diikuti:
     * 1. startActivity(PaymentConfirmationActivity) → masuk ke back stack (belum terlihat)
     * 2. startActivity(EWalletApp)                  → muncul di foreground
     * 3. finish()                                   → PaymentActivity dihapus dari stack
     *
     * Hasil back stack: [..., PaymentConfirmationActivity, EWalletApp (foreground)]
     * Saat user Back dari EWallet → PaymentConfirmationActivity tampil ✅
     */
    private fun checkAndProcessWallet(targetPackage: String) {
        val selectedMethod = viewModel.selectedMethod.value

        // Early return: QRIS tidak butuh buka aplikasi eksternal
        if (selectedMethod == "QRIS") {
            showQrisDialog()
            return
        }

        val packageName = when (selectedMethod) {
            "ShopeePay" -> "com.shopee.id"
            "GoPay"     -> "com.gojek.app"
            "DANA"      -> "id.dana"
            "OVO"       -> "ovo.id"
            "Jago"      -> "com.jago.digitalBanking"
            "LinkAja"   -> "id.or.tcash.wallet"
            else        -> targetPackage
        }

        if (packageName.isEmpty()) {
            showQrisDialog()
            return
        }

        // getLaunchIntentForPackage() hanya return non-null jika:
        // (a) Aplikasi terinstal di device, DAN
        // (b) Tag <queries> sudah ada di AndroidManifest.xml (wajib untuk API 30+)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            Toast.makeText(this, "Membuka $selectedMethod...", Toast.LENGTH_SHORT).show()

            // ✅ LANGKAH 1: Tempatkan PaymentConfirmation di back stack terlebih dahulu
            val confirmIntent = Intent(this, PaymentConfirmationActivity::class.java).apply {
                putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
                putExtra("PAYMENT_METHOD_EXTRA", selectedMethod)
            }
            startActivity(confirmIntent)

            // ✅ LANGKAH 2: Buka E-Wallet di atas PaymentConfirmation
            try {
                startActivity(launchIntent)
            } catch (e: ActivityNotFoundException) {
                // Sangat jarang terjadi setelah getLaunchIntentForPackage() non-null,
                // tapi tetap ditangani sebagai safety net.
                redirectToPlayStore(packageName)
            }

            // ✅ LANGKAH 3: Hancurkan PaymentActivity dari stack
            finish()

        } else {
            // Aplikasi tidak terinstal → arahkan ke Play Store
            // Tidak perlu goToConfirmationScreen() karena user belum melakukan pembayaran
            Toast.makeText(
                this,
                "Aplikasi $selectedMethod tidak ditemukan. Mengarahkan ke Play Store...",
                Toast.LENGTH_LONG
            ).show()
            redirectToPlayStore(packageName)
        }
    }

    private fun redirectToPlayStore(packageName: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun showQrisDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_qris)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnClose = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_close_qris)
        btnClose.setOnClickListener {
            dialog.dismiss()
            goToConfirmationScreen()
        }

        dialog.show()
    }

    private fun goToConfirmationScreen() {
        val intent = Intent(this, PaymentConfirmationActivity::class.java).apply {
            putExtra("TOTAL_PAYMENT_EXTRA", totalPayment)
            putExtra("PAYMENT_METHOD_EXTRA", viewModel.selectedMethod.value)
        }
        startActivity(intent)
        finish()
    }

    private fun syncOrderToLegacyAppState() {
        val orderRepository = ServiceLocator.container.orderRepository
        AppState.lastPaymentMethod = orderRepository.getLastPaymentMethod()
    }
}