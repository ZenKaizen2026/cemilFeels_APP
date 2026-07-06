package com.example.cemil_feels

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityOrderStatusBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Aktivitas untuk memantau status pesanan secara real-time (FT-05).
 * Menggunakan simulasi Coroutines dengan delay() untuk memajukan status pesanan lokal.
 */
class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding
    private val channelId = "cemil_feels_order_status"

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

        // Menggunakan View Binding
        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buat saluran notifikasi
        createNotificationChannel()
        
        // Minta izin notifikasi jika di Android 13+
        checkNotificationPermission()

        // Tombol kembali
        binding.btnStatusBack.setOnClickListener {
            finish()
        }

        // Tombol salin Order ID
        binding.btnCopyOrderId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Order ID", binding.tvOrderId.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Order ID disalin!", Toast.LENGTH_SHORT).show()
        }

        // Memulai simulasi pelacakan status pesanan
        startOrderSimulation()
    }

    /**
     * Memulai simulasi status pesanan secara bertahap menggunakan Kotlin Coroutines (FT-05).
     */
    private fun startOrderSimulation() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            // --- TAHAP 1: PESANAN DITERIMA ---
            val time1 = Calendar.getInstance()
            binding.tvTimeReceived.text = sdf.format(time1.time)
            binding.tvChefStatus.text = "Pesanan Diterima"
            binding.tvEtaCountdown.text = "Menunggu antrean..."
            
            val timeEta = Calendar.getInstance().apply { add(Calendar.MINUTE, 25) }
            binding.tvEtaTime.text = sdf.format(timeEta.time)
            
            sendLocalNotification("Pesanan Diterima", "Mitra kami telah menerima pesananmu.")
            
            delay(4000) // Delay simulasi 4 detik

            // --- TAHAP 2: SEDANG DISIAPKAN ---
            val time2 = Calendar.getInstance()
            binding.tvTimePreparing.text = sdf.format(time2.time)
            binding.tvChefStatus.text = "Sedang Disiapkan"
            binding.tvLblPreparing.setTextColor(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorTextDark))
            binding.tvLblPreparing.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.dotPreparing.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorPrimary))
            binding.tvEtaCountdown.text = "Sedang dimasak (20 menit lagi)..."
            
            // Gambar garis vertikal aktif dari node 1 ke node 2
            updateProgressLine(binding.dotReceived, binding.dotPreparing)
            
            sendLocalNotification("Sedang Disiapkan", "Camilanmu sedang digoreng hangat-hangat.")
            
            delay(4000) // Delay simulasi 4 detik

            // --- TAHAP 3: SEDANG DIKIRIM ---
            val time3 = Calendar.getInstance()
            binding.tvTimeShipping.text = sdf.format(time3.time)
            binding.tvChefStatus.text = "Sedang Dikirim"
            binding.tvLblShipping.setTextColor(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorTextDark))
            binding.tvLblShipping.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.dotShipping.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorPrimary))
            binding.tvEtaCountdown.text = "Driver sedang meluncur (10 menit lagi)..."
            
            updateProgressLine(binding.dotReceived, binding.dotShipping)
            
            sendLocalNotification("Sedang Dikirim", "Driver sedang mengantar pesananmu ke alamat.")
            
            delay(4000) // Delay simulasi 4 detik

            // --- TAHAP 4: PESANAN TIBA ---
            val time4 = Calendar.getInstance()
            binding.tvTimeArrived.text = sdf.format(time4.time)
            binding.tvChefStatus.text = "Pesanan Tiba!"
            binding.tvLblArrived.setTextColor(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorTextDark))
            binding.tvLblArrived.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.dotArrived.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@OrderStatusActivity, R.color.colorPrimary))
            binding.tvEtaCountdown.text = "Tiba di tujuan"
            
            updateProgressLine(binding.dotReceived, binding.dotArrived)
            
            sendLocalNotification("Pesanan Tiba", "Camilanmu sudah sampai! Nikmati selagi hangat.")
            
            delay(2000)
            val intent = Intent(this@OrderStatusActivity, OrderCompletedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Memperbarui panjang garis progress vertikal aktif berdasarkan simpul awal dan akhir.
     */
    private fun updateProgressLine(startView: View, endView: View) {
        binding.viewVerticalLineActive.post {
            val startY = startView.y + (startView.height / 2)
            val endY = endView.y + (endView.height / 2)
            
            // Set posisi Y dan tinggi garis aktif untuk menghubungkan simpul
            binding.viewVerticalLineActive.y = startY
            val params = binding.viewVerticalLineActive.layoutParams
            params.height = (endY - startY).toInt()
            binding.viewVerticalLineActive.layoutParams = params
            binding.viewVerticalLineActive.visibility = View.VISIBLE
        }
    }

    /**
     * Membuat Saluran Notifikasi Lokal untuk Android 8.0 ke atas.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Status Pesanan CemilFeels"
            val descriptionText = "Notifikasi status pelacakan pesanan makanan"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Mengirimkan notifikasi push lokal ke sistem operasi perangkat.
     */
    private fun sendLocalNotification(title: String, message: String) {
        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (e: SecurityException) {
            // Ditangani jika izin notifikasi dinonaktifkan
        }
    }

    /**
     * Memeriksa dan meminta izin notifikasi untuk Android 13+.
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
