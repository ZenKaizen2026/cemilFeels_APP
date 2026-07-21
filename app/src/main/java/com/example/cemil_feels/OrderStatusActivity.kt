package com.example.cemil_feels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityOrderStatusBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.data.repository.OrderStep
import com.example.cemil_feels.data.repository.OrderSimulationState
import com.example.cemil_feels.viewmodel.OrderStatusViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas untuk memantau status pesanan secara real-time (FT-05).
 * Menggunakan simulasi Coroutines dengan StateFlow yang dihubungkan ke OrderStatusViewModel.
 * Refactored to follow MVVM architecture.
 */
class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding
    private var lastRenderedStep: OrderStep? = null

    private val viewModel: OrderStatusViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    // Launcher untuk meminta izin notifikasi di Android 13+ (API 33+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Izin notifikasi ditolak. Anda tidak akan menerima notifikasi status.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Pengecekan izin notifikasi
        checkNotificationPermission()

        binding.btnStatusBack.setOnClickListener {
            finish()
        }

        binding.btnStatusDetail.setOnClickListener {
            showTransactionDetailDialog()
        }

        binding.btnCopyOrderId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Order ID", binding.tvOrderId.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Order ID disalin!", Toast.LENGTH_SHORT).show()
        }

        setupObservers()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.simulationState.collect { state ->
                renderSimulationState(state)
            }
        }
    }

    private fun renderSimulationState(state: OrderSimulationState) {
        // Update simple texts
        binding.tvChefStatus.text = state.chefStatus
        binding.tvEtaCountdown.text = state.etaCountdown
        if (state.etaTime.isNotEmpty()) {
            binding.tvEtaTime.text = state.etaTime
        }

        if (state.timeReceived.isNotEmpty()) {
            binding.tvTimeReceived.text = state.timeReceived
        }
        if (state.timePreparing.isNotEmpty()) {
            binding.tvTimePreparing.text = state.timePreparing
        }
        if (state.timeShipping.isNotEmpty()) {
            binding.tvTimeShipping.text = state.timeShipping
        }
        if (state.timeArrived.isNotEmpty()) {
            binding.tvTimeArrived.text = state.timeArrived
        }

        // Apply progress styling and line connections based on step
        when (state.currentStep) {
            OrderStep.RECEIVED -> {
                // Initial state
            }
            OrderStep.PREPARING -> {
                binding.tvLblPreparing.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblPreparing.setTypeface(null, android.graphics.Typeface.BOLD)
                updateProgressLine(binding.dotReceived, binding.dotPreparing)
            }
            OrderStep.SHIPPING -> {
                binding.tvLblPreparing.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblPreparing.setTypeface(null, android.graphics.Typeface.BOLD)

                binding.tvLblShipping.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblShipping.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.dotShipping.setImageResource(R.drawable.ic_timeline_dot_active)
                updateProgressLine(binding.dotReceived, binding.dotShipping)
            }
            OrderStep.ARRIVED -> {
                binding.tvLblPreparing.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblPreparing.setTypeface(null, android.graphics.Typeface.BOLD)

                binding.tvLblShipping.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblShipping.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.dotShipping.setImageResource(R.drawable.ic_timeline_dot_completed)

                binding.tvLblArrived.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
                binding.tvLblArrived.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.dotArrived.setImageResource(R.drawable.ic_timeline_dot_active)
                updateProgressLine(binding.dotReceived, binding.dotArrived)
            }
            OrderStep.COMPLETED -> {
                val intent = Intent(this, OrderCompletedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Trigger local notification if the step changed
        if (state.currentStep != lastRenderedStep) {
            state.triggerNotification?.let {
                sendLocalNotification(state.currentStep)
            }
            lastRenderedStep = state.currentStep
        }
    }

    private fun updateProgressLine(startView: View, endView: View) {
        binding.viewVerticalLineActive.post {
            val startY = startView.y + (startView.height / 2)
            val endY = endView.y + (endView.height / 2)

            binding.viewVerticalLineActive.y = startY
            val params = binding.viewVerticalLineActive.layoutParams
            params.height = (endY - startY).toInt()
            binding.viewVerticalLineActive.layoutParams = params
            binding.viewVerticalLineActive.visibility = View.VISIBLE
        }
    }

    private fun sendLocalNotification(step: OrderStep) {
        // 1. Tentukan Kalimat Custom berdasarkan Status Pesanan
        val (title, message) = when (step) {
            OrderStep.RECEIVED -> Pair("Pesanan Diterima! 📝", "Asyik, pesananmu sudah masuk dan sedang kami tinjau.")
            OrderStep.PREPARING -> Pair("Sedang Dimasak! 🍳", "Camilanmu sedang disiapkan dengan penuh cinta oleh koki kami.")
            OrderStep.SHIPPING -> Pair("Pesanan Meluncur! 🛵", "Siap-siap! Driver kami sedang meluncur bawa pesananmu.")
            OrderStep.ARRIVED -> Pair("Pesanan Tiba! 🎉", "Yey! Camilanmu sudah sampai. Selamat menikmati!")
            OrderStep.COMPLETED -> Pair("Selesai! ✅", "Transaksi selesai. Ditunggu pesanan selanjutnya ya!")
        }

        // 2. Tampilkan Notifikasi dengan Logo Kustom menggunakan NotificationHelper
        NotificationHelper.showNotification(this, title, message)

        // 3. Simpan ke Riwayat Notifikasi (SharedPreferences) agar tetap muncul di halaman history
        try {
            val sharedPrefs = getSharedPreferences("CemilFeelsPrefs", Context.MODE_PRIVATE)
            val currentHistory = sharedPrefs.getString("notification_history", "") ?: ""

            val timeFormatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
            val timeStr = timeFormatter.format(java.util.Date())

            val newNotif = "$title;;$message;;$timeStr"
            val updatedHistory = if (currentHistory.isEmpty()) newNotif else "$currentHistory||$newNotif"

            sharedPrefs.edit().putString("notification_history", updatedHistory).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showTransactionDetailDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val tvPayMethod = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_pay_method)
        val tvQty = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_qty)
        val tvSnackName = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_snack_name)
        val tvTotal = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_trans_total)
        val tvTime = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_time)
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_date)

        tvPayMethod?.text = viewModel.getPaymentMethod()
        tvQty?.text = viewModel.getFormattedQuantity()
        tvSnackName?.text = viewModel.getSnackName()
        tvTotal?.text = viewModel.getFormattedTotalCost()
        tvTime?.text = viewModel.getFormattedTime()
        tvDate?.text = viewModel.getFormattedDate()

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btn_close_detail)
        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            dialog.window?.attributes?.blurBehindRadius = 25
        }
    }
}