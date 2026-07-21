package com.example.cemil_feels

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    // 1. UBAH CHANNEL ID: Kita tambahkan "_headsup" agar Android membuat channel baru
    // yang belum terkontaminasi settingan lama, sehingga pop-up bisa muncul.
    private const val CHANNEL_ID = "cemil_feels_channel_headsup"
    private const val CHANNEL_NAME = "Cemil Feels Notifications"

    private const val GROUP_KEY_CEMIL = "com.example.cemil_feels.ORDER_STATUS"
    private const val SUMMARY_ID = 9999

    /**
     * Memunculkan notifikasi dengan logo kustom dan Heads-Up (Pop-up di atas layar).
     */
    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2. Buat Notification Channel dengan IMPORTANCE_HIGH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // WAJIB HIGH untuk Heads-up
            )
            // Wajib ada getaran agar sistem memicu Heads-up
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 250, 250, 250)

            notificationManager.createNotificationChannel(channel)
        }

        // BAGIAN INI UNTUK LOGO BESAR (LARGE ICON) - Gambar utuh berwarna
        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_cemilaja)

        // 3. Bangun Notifikasi Individual (Pop-up)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logonotif)
            .setColor(Color.parseColor("#FF7A1A"))
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(title)
            .setContentText(message)
            // KUNCI HEADS-UP NOTIFICATION ADA DI 2 BARIS INI:
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Wajib set default (suara & getar)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Memberi tahu sistem ini pesan penting
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_CEMIL)

        // Tampilkan Notifikasi Individual
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        notificationManager.notify(notificationId, builder.build())

        // 4. BANGUN NOTIFIKASI INDUK (GROUP SUMMARY)
        val summaryBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logonotif)
            .setColor(Color.parseColor("#FF7A1A"))
            .setGroup(GROUP_KEY_CEMIL)
            .setGroupSummary(true) // Tandai ini sebagai header dari grup
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Tampilkan Header Grup
        notificationManager.notify(SUMMARY_ID, summaryBuilder.build())
    }
}