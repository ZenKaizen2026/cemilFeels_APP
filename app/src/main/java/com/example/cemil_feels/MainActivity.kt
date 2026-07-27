package com.example.cemil_feels

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.LinearLayout
import android.widget.Toast
import android.view.View
import androidx.core.view.updatePadding
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cemil_feels.databinding.ActivityVentingBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.viewmodel.VentingViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Venting / Mood Selection Screen (Page 4).
 * Tempat pengguna memilih emotikon dan menuliskan keluh kesah sebelum mencari camilan.
 * Refactored to follow MVVM architecture.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentingBinding
    
    private val viewModel: VentingViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityVentingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val density = resources.displayMetrics.density

            // 1. Root: Handle horizontal insets (notches, landscape, etc.)
            v.updatePadding(left = systemBars.left, right = systemBars.right)

            // 2. Header: Add status bar height to base 20dp padding
            val headerTopPadding = systemBars.top + (20 * density).toInt()
            binding.layoutHeader.updatePadding(top = headerTopPadding)

            // 3. Footer (Sticky CTA): Handle Keyboard vs Navigation Bar insets
            // We use ime() insets when keyboard is visible, otherwise systemBars() (navigation bars)
            val isKeyboardVisible = isImeVisible && imeInsets.bottom > 0
            val bottomInset = if (isKeyboardVisible) imeInsets.bottom else systemBars.bottom
            
            // Adjust base padding for footer: 12dp when keyboard is open (tight), 24dp normally
            val footerBasePadding = if (isKeyboardVisible) (12 * density).toInt() else (24 * density).toInt()
            binding.layoutFooter.updatePadding(bottom = bottomInset + footerBasePadding)

            // 4. Privacy Badge: Hide when keyboard is open to maximize screen space for input
            binding.layoutSecurityFooter.visibility = if (isKeyboardVisible) View.GONE else View.VISIBLE

            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupMoodSelection()
        setupTextWatcher()
        setupSubmitButton()
        setupObservers()
    }

    private fun setupMoodSelection() {
        val moodPickers = mapOf(
            "Senang" to binding.pickerSenang,
            "Sedih" to binding.pickerSedih,
            "Bosan" to binding.pickerBosan,
            "Marah" to binding.pickerMarah
        )

        moodPickers.forEach { (moodName, pickerLayout) ->
            pickerLayout.setOnClickListener {
                viewModel.toggleMood(moodName)
            }
        }
    }

    private fun updateMoodPickersUI(selectedMood: String?) {
        val moodPickers = mapOf(
            "Senang" to binding.pickerSenang,
            "Sedih" to binding.pickerSedih,
            "Bosan" to binding.pickerBosan,
            "Marah" to binding.pickerMarah
        )

        moodPickers.forEach { (moodName, pickerLayout) ->
            if (moodName == selectedMood) {
                pickerLayout.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorSelected))
                pickerLayout.strokeColor = ContextCompat.getColor(this, R.color.colorPrimary)
            } else {
                pickerLayout.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                pickerLayout.strokeColor = android.graphics.Color.parseColor("#EAEAEA")
            }
        }
    }

    private fun setupTextWatcher() {
        binding.etStoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onStoryTextChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSubmitButton() {
        binding.btnSubmitMood.setOnClickListener {
            viewModel.submitMood()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.selectedMood.collect { mood ->
                updateMoodPickersUI(mood)
            }
        }

        lifecycleScope.launch {
            viewModel.charCounter.collect { countText ->
                binding.tvCharCounter.text = countText
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is VentingViewModel.VentingEvent.ShowValidationToast -> {
                        Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is VentingViewModel.VentingEvent.NavigateToRecommendations -> {
                        val intent = Intent(this@MainActivity, RecommendationActivity::class.java).apply {
                            putExtra("STORY_EXTRA", event.story)
                            putExtra("MOOD_EXTRA", event.mood)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}