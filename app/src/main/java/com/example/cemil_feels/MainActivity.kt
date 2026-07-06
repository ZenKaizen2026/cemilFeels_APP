package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cemil_feels.databinding.ActivityVentingBinding

/**
 * Aktivitas Venting / Mood Selection Screen (Page 4).
 * Tempat pengguna memilih emotikon dan menuliskan keluh kesah sebelum mencari camilan.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentingBinding
    private var selectedMood: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Menggunakan View Binding dengan layout activity_venting
        binding = ActivityVentingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tombol kembali ke LoginActivity
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Setup fungsionalitas UI
        setupMoodSelection()
        setupTextWatcher()
        setupSubmitButton()
    }

    /**
     * Menangani pemilihan emotikon mood (Bahagia, Sedih, Biasa aja, Cemas, Marah).
     */
    private fun setupMoodSelection() {
        val moodPickers = mapOf(
            "Bahagia" to binding.pickerBahagia,
            "Sedih" to binding.pickerSedih,
            "Biasa aja" to binding.pickerBiasa,
            "Cemas" to binding.pickerCemas,
            "Marah" to binding.pickerMarah
        )

        moodPickers.forEach { (moodName, pickerLayout) ->
            pickerLayout.setOnClickListener {
                // Toggle pilihan mood
                selectedMood = if (selectedMood == moodName) null else moodName
                updateMoodPickersUI(moodPickers)
            }
        }

        updateMoodPickersUI(moodPickers)
    }

    /**
     * Memperbarui visual background untuk menyorot emotikon mood yang dipilih.
     */
    private fun updateMoodPickersUI(moodPickers: Map<String, LinearLayout>) {
        moodPickers.forEach { (moodName, pickerLayout) ->
            if (moodName == selectedMood) {
                // Di-highlight dengan warna orange salem
                pickerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSelected))
            } else {
                // Transparan jika tidak dipilih
                pickerLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            }
        }
    }

    /**
     * Memantau panjang karakter teks cerita secara dinamis (0/1000).
     */
    private fun setupTextWatcher() {
        binding.etStoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.tvCharCounter.text = "$length/1000"
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * Menghubungkan tombol "Siap, cari camilan!" untuk beralih ke halaman rekomendasi.
     */
    private fun setupSubmitButton() {
        binding.btnSubmitMood.setOnClickListener {
            val storyText = binding.etStoryInput.text?.toString()?.trim()

            // Validasi: Pengguna harus mengisi cerita ATAU memilih mood
            if (storyText.isNullOrEmpty() && selectedMood == null) {
                Toast.makeText(this, "Silakan pilih perasaanmu atau tulis ceritamu terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pindah ke RecommendationActivity sambil membawa parameter mood & keluhan
            val intent = Intent(this, RecommendationActivity::class.java).apply {
                putExtra("STORY_EXTRA", storyText)
                putExtra("MOOD_EXTRA", selectedMood)
            }
            startActivity(intent)
        }
    }
}