package com.example.cemil_feels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Aktivitas untuk memantau status pesanan secara real-time (FT-05).
 * Redesigned for Premium UI/UX with Micro-interactions and Modern Timeline.
 */
class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding
    private var lastRenderedStep: OrderStep? = null
    private var pulseAnimator: ObjectAnimator? = null

    private val viewModel: OrderStatusViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
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

        checkNotificationPermission()
        setupClickListeners()
        setupObservers()
        startHeroPulse()
    }

    private fun setupClickListeners() {
        binding.btnStatusBack.setOnClickListener { finish() }
        binding.btnStatusDetail.setOnClickListener { showTransactionDetailDialog() }
        binding.btnCopyOrderId.setOnClickListener { copyOrderId() }
    }

    private fun copyOrderId() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Order ID", binding.tvOrderId.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Order ID copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun startHeroPulse() {
        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.viewHeroPulse,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.4f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.4f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.4f, 0f)
        ).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.simulationState.collect { state ->
                renderSimulationState(state)
            }
        }
    }

    private fun renderSimulationState(state: OrderSimulationState) {
        binding.tvChefStatus.text = state.chefStatus
        binding.tvEtaCountdown.text = state.etaCountdown
        if (state.etaTime.isNotEmpty()) binding.tvEtaTime.text = state.etaTime

        updateTimelineTimes(state)
        updateTimelineProgress(state.currentStep)

        if (state.currentStep != lastRenderedStep) {
            state.triggerNotification?.let { sendLocalNotification(state.currentStep) }
            lastRenderedStep = state.currentStep
        }
    }

    private fun updateTimelineTimes(state: OrderSimulationState) {
        if (state.timeReceived.isNotEmpty()) binding.tvTimeReceived.text = state.timeReceived
        if (state.timePreparing.isNotEmpty()) binding.tvTimePreparing.text = state.timePreparing
        if (state.timeShipping.isNotEmpty()) binding.tvTimeShipping.text = state.timeShipping
        if (state.timeArrived.isNotEmpty()) binding.tvTimeArrived.text = state.timeArrived
    }

    private fun updateTimelineProgress(step: OrderStep) {
        // Reset all to default (upcoming)
        resetTimelineNodes()

        when (step) {
            OrderStep.RECEIVED -> {
                setActiveNode(binding.dotReceived, binding.tvLblReceived, binding.tvTimeReceived)
            }
            OrderStep.PREPARING -> {
                setCompletedNode(binding.dotReceived, binding.tvLblReceived, binding.tvTimeReceived)
                setActiveNode(binding.dotPreparing, binding.tvLblPreparing, binding.tvTimePreparing)
                updateLine(binding.dotReceived, binding.dotPreparing)
                binding.ivHeroStatus.setImageResource(R.drawable.ic_chef_hat)
            }
            OrderStep.SHIPPING -> {
                setCompletedNode(binding.dotReceived, binding.tvLblReceived, binding.tvTimeReceived)
                setCompletedNode(binding.dotPreparing, binding.tvLblPreparing, binding.tvTimePreparing)
                setActiveNode(binding.dotShipping, binding.tvLblShipping, binding.tvTimeShipping)
                updateLine(binding.dotReceived, binding.dotShipping)
                binding.ivHeroStatus.setImageResource(R.drawable.ic_scooter)
            }
            OrderStep.ARRIVED -> {
                setCompletedNode(binding.dotReceived, binding.tvLblReceived, binding.tvTimeReceived)
                setCompletedNode(binding.dotPreparing, binding.tvLblPreparing, binding.tvTimePreparing)
                setCompletedNode(binding.dotShipping, binding.tvLblShipping, binding.tvTimeShipping)
                setActiveNode(binding.dotArrived, binding.tvLblArrived, binding.tvTimeArrived)
                updateLine(binding.dotReceived, binding.dotArrived)
                binding.ivHeroStatus.setImageResource(R.drawable.emot_home)
            }
            OrderStep.COMPLETED -> {
                startActivity(Intent(this, OrderCompletedActivity::class.java))
                finish()
            }
        }
    }

    private fun resetTimelineNodes() {
        val nodes = listOf(binding.dotReceived, binding.dotPreparing, binding.dotShipping, binding.dotArrived)
        nodes.forEach { 
            it.setImageResource(R.drawable.ic_circle_outline)
            it.imageTintList = ContextCompat.getColorStateList(this, R.color.colorTextGrey)
        }

        val labels = listOf(binding.tvLblReceived, binding.tvLblPreparing, binding.tvLblShipping, binding.tvLblArrived)
        labels.forEach { 
            it.setTextColor(ContextCompat.getColor(this, R.color.colorTextGrey))
            it.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        val times = listOf(binding.tvTimeReceived, binding.tvTimePreparing, binding.tvTimeShipping, binding.tvTimeArrived)
        times.forEach {
            it.setTextColor(ContextCompat.getColor(this, R.color.colorTextGrey))
        }

        binding.viewVerticalLineActive.visibility = View.GONE
    }

    private fun setActiveNode(dot: ImageView, label: TextView, time: TextView) {
        dot.setImageResource(R.drawable.bg_status_dot_current)
        dot.imageTintList = null
        label.setTextColor(ContextCompat.getColor(this, R.color.colorActive))
        label.setTypeface(null, android.graphics.Typeface.BOLD)
        time.setTextColor(ContextCompat.getColor(this, R.color.colorActive))
    }

    private fun setCompletedNode(dot: ImageView, label: TextView, time: TextView) {
        dot.setImageResource(R.drawable.bg_status_dot_completed)
        dot.imageTintList = null
        label.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
        label.setTypeface(null, android.graphics.Typeface.NORMAL)
        time.setTextColor(ContextCompat.getColor(this, R.color.colorTextGrey))
    }

    private fun updateLine(startView: View, endView: View) {
        binding.viewVerticalLineActive.post {
            val startY = startView.y + (startView.height / 2)
            val endY = endView.y + (endView.height / 2)
            val params = binding.viewVerticalLineActive.layoutParams
            params.height = (endY - startY).toInt()
            binding.viewVerticalLineActive.y = startY
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

        val tvPayMethod = dialogView.findViewById<TextView>(R.id.tv_val_pay_method)
        val tvQty = dialogView.findViewById<TextView>(R.id.tv_val_qty)
        val tvSnackName = dialogView.findViewById<TextView>(R.id.tv_val_snack_name)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tv_val_trans_total)
        val tvTime = dialogView.findViewById<TextView>(R.id.tv_val_time)
        val tvDate = dialogView.findViewById<TextView>(R.id.tv_val_date)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            dialog.window?.attributes?.blurBehindRadius = 25
        }
    }
}