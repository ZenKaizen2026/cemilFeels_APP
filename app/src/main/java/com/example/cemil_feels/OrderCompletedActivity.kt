package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Aktivitas Order Completed Screen (Page 13).
 * Menyajikan halaman pesanan selesai, rating kepuasan pelanggan, dan rincian detail transaksi.
 */
class OrderCompletedActivity : AppCompatActivity() {

    private lateinit var binding: com.example.cemil_feels.databinding.ActivityOrderCompletedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = com.example.cemil_feels.databinding.ActivityOrderCompletedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCompletedBack.setOnClickListener {
            goToHome()
        }

        // Tampilkan dialog Rincian Transaksi (Page 14) saat "Lihat Detail" diklik
        binding.btnCompletedViewDetail.setOnClickListener {
            showTransactionDetailDialog()
        }

        // Aksi tombol kembali ke Beranda (Dashboard)
        binding.btnCompletedHome.setOnClickListener {
            goToHome()
        }

        // Aksi tombol pesan lagi kembali ke Beranda (Dashboard)
        binding.btnCompletedOrderAgain.setOnClickListener {
            Toast.makeText(this, "Ayo cari camilan lagi!", Toast.LENGTH_SHORT).show()
            goToHome()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    /**
     * Memunculkan overlay lembar Rincian Transaksi (Page 14) menggunakan Custom Dialog.
     */
    private fun showTransactionDetailDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Ambil komponen TextView dari dialog layout
        val tvPayMethod = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_pay_method)
        val tvQty = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_qty)
        val tvSnackName = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_snack_name)
        val tvTotal = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_trans_total)
        val tvTime = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_time)
        val tvDate = dialogView.findViewById<android.widget.TextView>(R.id.tv_val_date)

        // Set value dinamis dari AppState
        tvPayMethod?.text = AppState.lastPaymentMethod
        tvQty?.text = "${AppState.lastOrderQty} (100gr)"
        tvSnackName?.text = AppState.lastOrderSnackName

        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID"))
        tvTotal?.text = "Rp. " + formatter.format(AppState.lastOrderTotalCost.toInt())

        // Set waktu & tanggal secara real-time
        val sdfTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.forLanguageTag("id-ID"))
        val sdfDate = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.forLanguageTag("id-ID"))
        val now = java.util.Calendar.getInstance().time
        tvTime?.text = sdfTime.format(now)
        tvDate?.text = sdfDate.format(now)

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btn_close_detail)
        btnClose?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
