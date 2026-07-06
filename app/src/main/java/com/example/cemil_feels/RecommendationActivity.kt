package com.example.cemil_feels

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.cemil_feels.databinding.ActivityRecommendationBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Aktivitas Mood-Based Recommendation Screen (Page 5).
 * Menampilkan rekomendasi camilan berbasis mood dalam ViewPager2 carousel.
 * Menangani penyesuaian level kepedasan, catatan, dan proses pembayaran (E-Wallet/QRIS).
 */
class RecommendationActivity : AppCompatActivity() {

    companion object {
        val STATIC_SNACKS = listOf(
            Snack(
                name = "Basreng Stik",
                price = 16000.0,
                rating = 4.5,
                description = "Pedasnya basreng nagih, instan usir bad mood dan bikin happy!",
                imageResId = R.drawable.basreng_stik,
                stock = 5
            ),
            Snack(
                name = "Makaroni Bantet",
                price = 10000.0,
                rating = 4.4,
                description = "Makaroni bantet renyah gurih pedas khas jajanan pasar.",
                imageResId = R.drawable.makaroni_bantet,
                stock = 3
            ),
            Snack(
                name = "Cireng Sambal Rujak",
                price = 15000.0,
                rating = 4.7,
                description = "Cireng hangat renyah disajikan dengan saus rujak manis pedas.",
                imageResId = R.drawable.cireng_sambal_rujak,
                stock = 4
            ),
            Snack(
                name = "Tahu Walik",
                price = 12000.0,
                rating = 4.6,
                description = "Tahu goreng walik isi adonan bakso ayam gurih lezat.",
                imageResId = R.drawable.tahu_walik,
                stock = 2
            ),
            Snack(
                name = "Piscok Lumer Coklat",
                price = 12000.0,
                rating = 4.8,
                description = "Pisang coklat goreng renyah dengan coklat lumer melimpah.",
                imageResId = R.drawable.piscok_lumer_coklat,
                stock = 3
            ),
            Snack(
                name = "Bola Bola Coklat",
                price = 14000.0,
                rating = 4.5,
                description = "Kue manis berbentuk bola berbalut mesis coklat legit.",
                imageResId = R.drawable.bola_bola_coklat,
                stock = 0
            ),
            Snack(
                name = "Kulpi Balado",
                price = 11000.0,
                rating = 4.3,
                description = "Keripik kulit lumpia renyah bertabur bumbu balado manis gurih.",
                imageResId = R.drawable.kulpi_balado,
                stock = 4
            ),
            Snack(
                name = "Kerupuk Pangsit",
                price = 8000.0,
                rating = 4.2,
                description = "Kerupuk pangsit goreng renyah teman santai hari ini.",
                imageResId = R.drawable.krupuk_pangsit,
                stock = 6
            )
        )
    }

    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var snackAdapter: SnackAdapter

    private val _snacksState = MutableStateFlow<List<Snack>>(STATIC_SNACKS)
    val snacksState: StateFlow<List<Snack>> = _snacksState.asStateFlow()

    private var selectedSpiceLevel = "Pedas" // Level default dari spesifikasi Page 5
    private var finalSelectedMood = "Biasa aja"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tombol kembali ke Venting (MainActivity)
        binding.btnRecBack.setOnClickListener {
            finish()
        }

        // Tombol keranjang belanja
        binding.btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Bersihkan keranjang saat masuk halaman rekomendasi (spesifikasi 1)
        AppState.cart.clear()

        // Ambil data mood & cerita dari Intent untuk memfilter camilan
        val story = intent.getStringExtra("STORY_EXTRA")
        val mood = intent.getStringExtra("MOOD_EXTRA")
        processMoodAndFilterSnacks(story, mood)

        setupCarousel()
        setupSpiceSelector()
        setupPesanButton()
        
        // Tombol Pesan awalnya dinonaktifkan jika keranjang kosong
        binding.btnPesan.isEnabled = AppState.cart.isNotEmpty()
        
        observeSnacksFlow()
    }

    private fun setupCarousel() {
        snackAdapter = SnackAdapter(
            onAddClicked = { snack ->
                // 1. Kurangi stok lokal di view
                reduceSnackStock(snack)
                
                // 2. Tambah kuantitas di AppState.cart
                val currentQty = AppState.cart[snack.name] ?: 0
                AppState.cart[snack.name] = currentQty + 1
                
                // 3. Pastikan item tersebut terpilih secara visual
                if (!snackAdapter.selectedSnackNames.contains(snack.name)) {
                    snackAdapter.selectedSnackNames.add(snack.name)
                }
                snackAdapter.notifyDataSetChanged()
                
                // 4. Update status tombol Pesan
                binding.btnPesan.isEnabled = AppState.cart.isNotEmpty()
            },
            onCardClicked = { snack, isSelected ->
                if (isSelected) {
                    // Pilih kartu -> Tambah ke keranjang (default 1)
                    if (!AppState.cart.containsKey(snack.name)) {
                        AppState.cart[snack.name] = 1
                    }
                } else {
                    // Hapus seleksi -> Keluarkan dari keranjang
                    AppState.cart.remove(snack.name)
                }
                // Update status tombol Pesan
                binding.btnPesan.isEnabled = AppState.cart.isNotEmpty()
            }
        )
        binding.vpSnacksCarousel.adapter = snackAdapter
    }

    /**
     * Pengkondisian Level Kepedasan (Tidak Pedas, Sedang, Pedas, Extra Pedas).
     */
    private fun setupSpiceSelector() {
        val spiceOptions = mapOf(
            "Tidak Pedas" to binding.btnSpiceNone,
            "Sedang" to binding.btnSpiceMedium,
            "Pedas" to binding.btnSpiceHigh,
            "Extra Pedas" to binding.btnSpiceExtra
        )

        spiceOptions.forEach { (level, layout) ->
            layout.setOnClickListener {
                selectedSpiceLevel = level
                updateSpiceSelectorUI(spiceOptions)
            }
        }

        updateSpiceSelectorUI(spiceOptions)
    }

    /**
     * Memperbarui warna visual background dan teks dari pilihan tingkat kepedasan.
     */
    private fun updateSpiceSelectorUI(spiceOptions: Map<String, LinearLayout>) {
        spiceOptions.forEach { (level, layout) ->
            val isSelected = level == selectedSpiceLevel
            
            // Atur background tint
            layout.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this, 
                    if (isSelected) R.color.colorPrimary else R.color.white
                )
            )

            // Atur warna teks/ikon di dalamnya
            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is android.widget.TextView) {
                    child.setTextColor(
                        ContextCompat.getColor(
                            this, 
                            if (isSelected) R.color.white else R.color.colorTextDark
                        )
                    )
                } else if (child is android.widget.ImageView) {
                    child.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this,
                            if (isSelected) R.color.white else R.color.colorAccent
                        )
                    )
                } else if (child is LinearLayout) {
                    // Kasus untuk icon chili yang bersarang di LinearLayout
                    for (j in 0 until child.childCount) {
                        val nestedChild = child.getChildAt(j)
                        if (nestedChild is android.widget.ImageView) {
                            nestedChild.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    this,
                                    if (isSelected) R.color.white else R.color.colorAccent
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupPesanButton() {
        binding.btnPesan.setOnClickListener {
            // Hitung total belanja dari AppState.cart
            var totalCartAmount = 0.0
            var lastSnackName = "Basreng Stik"
            var lastQty = 1

            AppState.cart.forEach { (snackName, qty) ->
                val snackObj = STATIC_SNACKS.find { it.name == snackName }
                if (snackObj != null) {
                    totalCartAmount += qty * snackObj.price
                    lastSnackName = snackName
                    lastQty = qty
                }
            }

            // Jika kosong (pengaman), gunakan default
            if (totalCartAmount == 0.0) {
                totalCartAmount = 16000.0
            }

            // Simpan data dinamis pesanan ke AppState untuk ditampilkan di halaman konfirmasi dan rincian transaksi
            AppState.lastOrderSnackName = lastSnackName
            AppState.lastOrderQty = lastQty
            AppState.lastOrderTotalCost = totalCartAmount + 5000.0 + 2000.0 // Subtotal + Ongkir + Jasa
            AppState.lastOrderSpiceLevel = selectedSpiceLevel

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("TOTAL_CART_EXTRA", totalCartAmount)
            }
            startActivity(intent)
        }
    }

    /**
     * Memantau data StateFlow camilan.
     */
    private fun observeSnacksFlow() {
        lifecycleScope.launch {
            snacksState.collect { filteredSnacks ->
                snackAdapter.submitList(filteredSnacks)
            }
        }
    }

    /**
     * Mengurangi stok dari camilan tertentu sebanyak 1 ketika dimasukkan ke keranjang belanja (FT-03).
     */
    private fun reduceSnackStock(snack: Snack) {
        val currentList = _snacksState.value
        val updatedList = currentList.map { item ->
            if (item.name == snack.name && item.stock > 0) {
                item.copy(stock = item.stock - 1)
            } else {
                item
            }
        }
        _snacksState.value = updatedList
    }

    /**
     * Menyaring daftar camilan (Snack) berdasarkan keluhan cerita atau mood picker (FT-01 & FT-02).
     */
    private fun processMoodAndFilterSnacks(story: String?, selectedMood: String?) {
        val normalizedStory = story?.lowercase() ?: ""
        
        // Klasifikasi emosi dari cerita atau input emosi picker
        val moodFromStory = when {
            normalizedStory.contains("stres") || normalizedStory.contains("marah") ||
            normalizedStory.contains("kesal") || normalizedStory.contains("emosi") ||
            normalizedStory.contains("muak") || normalizedStory.contains("capek") -> "Marah"
            
            normalizedStory.contains("sedih") || normalizedStory.contains("galau") ||
            normalizedStory.contains("nangis") || normalizedStory.contains("kecewa") ||
            normalizedStory.contains("sepi") -> "Sedih"
            
            normalizedStory.contains("cemas") || normalizedStory.contains("takut") ||
            normalizedStory.contains("khawatir") || normalizedStory.contains("gugup") ||
            normalizedStory.contains("panik") -> "Cemas"
            
            normalizedStory.contains("bahagia") || normalizedStory.contains("senang") ||
            normalizedStory.contains("gembira") || normalizedStory.contains("ceria") ||
            normalizedStory.contains("bersyukur") -> "Bahagia"
            
            else -> selectedMood ?: "Biasa aja"
        }

        finalSelectedMood = moodFromStory

        // Sembunyikan bumbu/level kepedasan jika mood TIDAK sama dengan "Marah" (FT-02)
        if (moodFromStory.equals("Marah", ignoreCase = true)) {
            binding.tvSpiceLevelTitle.visibility = View.VISIBLE
            binding.layoutSpiceSelector.visibility = View.VISIBLE
        } else {
            binding.tvSpiceLevelTitle.visibility = View.GONE
            binding.layoutSpiceSelector.visibility = View.GONE
        }

        // Filter list berdasarkan drawable khusus
        val filteredList = when (moodFromStory) {
            "Marah", "Cemas" -> STATIC_SNACKS.filter { it.name == "Basreng Stik" || it.name == "Makaroni Bantet" }
            "Bahagia" -> STATIC_SNACKS.filter { it.name == "Cireng Sambal Rujak" || it.name == "Tahu Walik" }
            "Sedih" -> STATIC_SNACKS.filter { it.name == "Piscok Lumer Coklat" || it.name == "Bola Bola Coklat" }
            "Biasa aja" -> STATIC_SNACKS.filter { it.name == "Kulpi Balado" || it.name == "Kerupuk Pangsit" }
            else -> STATIC_SNACKS
        }
        _snacksState.value = filteredList
    }

    /**
     * Membuka aplikasi E-Wallet tujuan menggunakan Implicit Intent.
     * Jika aplikasi E-Wallet tidak terinstal, alur akan dialihkan ke layar fallback QRIS lokal.
     */
    private fun processCheckout(targetPackage: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
                // Setelah selesai membayar via e-wallet terpasang, langsung arahkan ke Tracker Status
                navigateToLiveTracker()
            } catch (e: Exception) {
                showQrisFallback()
            }
        } else {
            showQrisFallback()
        }
    }

    /**
     * Menampilkan dialog fallback QRIS lokal (dialog_qris.xml) berisi barcode pembayaran.
     */
    private fun showQrisFallback() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_qris, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_qris)
        btnClose?.setOnClickListener {
            dialog.dismiss()
            // Setelah QRIS ditutup oleh pengguna, lanjut arahkan ke Tracker Status (FT-05)
            navigateToLiveTracker()
        }

        dialog.show()
    }

    private fun navigateToLiveTracker() {
        val intent = Intent(this, OrderStatusActivity::class.java)
        startActivity(intent)
    }
}
