package com.example.cemil_feels

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.cemil_feels.databinding.ActivityRecommendationBinding
import com.example.cemil_feels.di.ServiceLocator
import com.example.cemil_feels.data.model.Snack
import com.example.cemil_feels.viewmodel.RecommendationViewModel
import com.example.cemil_feels.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

/**
 * Aktivitas Mood-Based Recommendation Screen (Page 5).
 * Menampilkan rekomendasi camilan berbasis mood dalam ViewPager2 carousel.
 * Menangani penyesuaian level kepedasan, catatan, dan proses pembayaran.
 * Refactored to follow MVVM architecture.
 */
class RecommendationActivity : AppCompatActivity() {

    companion object {
        // Preserved STATIC_SNACKS for full compatibility with legacy code
        val STATIC_SNACKS = listOf(
            Snack(
                id = 1,
                name = "Basreng Stik",
                price = 16000.0,
                rating = 4.5,
                description = "Pedasnya basreng nagih, instan usir bad mood dan bikin happy!",
                imageResId = R.drawable.basreng_stik,
                stock = 5,
                flavorTag = "Pedas"
            ),
            Snack(
                id = 2,
                name = "Makaroni Bantet",
                price = 10000.0,
                rating = 4.4,
                description = "Makaroni bantet renyah gurih pedas khas jajanan pasar.",
                imageResId = R.drawable.makaroni_bantet,
                stock = 3,
                flavorTag = "Pedas"
            ),
            Snack(
                id = 3,
                name = "Cireng Sambal Rujak",
                price = 15000.0,
                rating = 4.7,
                description = "Cireng hangat renyah disajikan dengan saus rujak manis pedas.",
                imageResId = R.drawable.cireng_sambal_rujak,
                stock = 4,
                flavorTag = "Crispy"
            ),
            Snack(
                id = 4,
                name = "Tahu Walik",
                price = 12000.0,
                rating = 4.6,
                description = "Tahu goreng walik isi adonan bakso ayam gurih lezat.",
                imageResId = R.drawable.tahu_walik,
                stock = 2,
                flavorTag = "Gurih"
            ),
            Snack(
                id = 5,
                name = "Piscok Lumer Coklat",
                price = 12000.0,
                rating = 4.8,
                description = "Pisang coklat goreng renyah dengan coklat lumer melimpah.",
                imageResId = R.drawable.piscok_lumer_coklat,
                stock = 3,
                flavorTag = "Manis"
            ),
            Snack(
                id = 6,
                name = "Bola Bola Coklat",
                price = 14000.0,
                rating = 4.5,
                description = "Kue manis berbentuk bola berbalut mesis coklat legit.",
                imageResId = R.drawable.bola_bola_coklat,
                stock = 0,
                flavorTag = "Manis"
            ),
            Snack(
                id = 7,
                name = "Kulpi Balado",
                price = 11000.0,
                rating = 4.3,
                description = "Keripik kulit lumpia renyah bertabur bumbu balado manis gurih.",
                imageResId = R.drawable.kulpi_balado,
                stock = 4,
                flavorTag = "Crispy"
            ),
            Snack(
                id = 8,
                name = "Kerupuk Pangsit",
                price = 8000.0,
                rating = 4.2,
                description = "Kerupuk pangsit goreng renyah teman santai hari ini.",
                imageResId = R.drawable.krupuk_pangsit,
                stock = 6,
                flavorTag = "Gurih"
            )
        )
    }

    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var snackAdapter: SnackAdapter

    private val viewModel: RecommendationViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container)
    }

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

        binding.btnRecBack.setOnClickListener {
            finish()
        }

        binding.btnCart.setOnClickListener {
            // Ensure AppState.cart is synchronized for legacy subcomponents
            syncCartToLegacyAppState()
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        // Initialize state inside ViewModel only if savedInstanceState is null
        if (savedInstanceState == null) {
            val story = intent.getStringExtra("STORY_EXTRA")
            val mood = intent.getStringExtra("MOOD_EXTRA")
            viewModel.initMoodAndStory(story, mood)
        }

        setupCarousel()
        setupSpiceSelector()
        setupPesanButton()
        setupObservers()
    }

    private fun setupCarousel() {
        snackAdapter = SnackAdapter(
            onAddClicked = { snack ->
                viewModel.addSnackToCart(snack)
            },
            onCardClicked = { snack, isSelected ->
                viewModel.toggleSnackSelection(snack, isSelected)
            }
        )
        binding.vpSnacksCarousel.adapter = snackAdapter
        
        binding.vpSnacksCarousel.offscreenPageLimit = 3
        binding.vpSnacksCarousel.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimensionPixelOffset(R.dimen.carousel_margin)))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
            page.scaleX = 0.85f + r * 0.15f
            page.alpha = 0.5f + r * 0.5f
        }
        binding.vpSnacksCarousel.setPageTransformer(compositePageTransformer)

        binding.vpSnacksCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDotsIndicator(position)
            }
        })
    }

    private fun setupDotsIndicator(count: Int) {
        binding.layoutDotsIndicator.removeAllViews()
        if (count <= 0) return
        
        val dots = arrayOfNulls<android.widget.ImageView>(count)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)

        for (i in 0 until count) {
            dots[i] = android.widget.ImageView(this)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
            binding.layoutDotsIndicator.addView(dots[i], params)
        }
    }

    private fun updateDotsIndicator(position: Int) {
        val childCount = binding.layoutDotsIndicator.childCount
        if (childCount <= 0) return
        
        for (i in 0 until childCount) {
            val imageView = binding.layoutDotsIndicator.getChildAt(i) as android.widget.ImageView
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
        }
    }

    private fun setupSpiceSelector() {
        val spiceOptions = mapOf(
            "Tidak Pedas" to binding.btnSpiceNone,
            "Sedang" to binding.btnSpiceMedium,
            "Pedas" to binding.btnSpiceHigh,
            "Extra Pedas" to binding.btnSpiceExtra
        )

        spiceOptions.forEach { (level, layout) ->
            layout.setOnClickListener {
                viewModel.setSpiceLevel(level)
            }
        }
    }

    private fun updateSpiceSelectorUI(selectedSpiceLevel: String) {
        val spiceOptions = mapOf(
            "Tidak Pedas" to binding.btnSpiceNone,
            "Sedang" to binding.btnSpiceMedium,
            "Pedas" to binding.btnSpiceHigh,
            "Extra Pedas" to binding.btnSpiceExtra
        )

        spiceOptions.forEach { (level, layout) ->
            val isSelected = level == selectedSpiceLevel
            layout.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, if (isSelected) R.color.colorPrimary else R.color.white)
            )

            for (i in 0 until layout.childCount) {
                val child = layout.getChildAt(i)
                if (child is android.widget.TextView) {
                    child.setTextColor(
                        ContextCompat.getColor(this, if (isSelected) R.color.white else R.color.colorTextDark)
                    )
                } else if (child is android.widget.ImageView) {
                    child.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this, if (isSelected) R.color.white else R.color.colorAccent)
                    )
                } else if (child is LinearLayout) {
                    for (j in 0 until child.childCount) {
                        val nestedChild = child.getChildAt(j)
                        if (nestedChild is android.widget.ImageView) {
                            nestedChild.imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(this, if (isSelected) R.color.white else R.color.colorAccent)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupPesanButton() {
        binding.btnPesan.setOnClickListener {
            syncCartToLegacyAppState()
            viewModel.onPesanClicked()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.filteredSnacksState.collect { filteredSnacks ->
                snackAdapter.submitList(filteredSnacks)
                setupDotsIndicator(filteredSnacks.size)
            }
        }

        lifecycleScope.launch {
            viewModel.isSpiceSelectorVisible.collect { isVisible ->
                val visibility = if (isVisible) View.VISIBLE else View.GONE
                binding.tvSpiceLevelTitle.visibility = visibility
                binding.layoutSpiceSelector.visibility = visibility
            }
        }

        lifecycleScope.launch {
            viewModel.selectedSpiceLevel.collect { spiceLevel ->
                updateSpiceSelectorUI(spiceLevel)
            }
        }

        lifecycleScope.launch {
            viewModel.isPesanButtonEnabled.collect { isEnabled ->
                binding.btnPesan.isEnabled = isEnabled
            }
        }

        lifecycleScope.launch {
            viewModel.selectedSnackNames.collect { names ->
                snackAdapter.selectedSnackNames.clear()
                snackAdapter.selectedSnackNames.addAll(names)
                // Removed notifyDataSetChanged() to prevent ViewPager2 reset/jump
            }
        }

        // ── Observer AI: Loading State ──────────────────────────────────────
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.pbAiLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // ── Observer AI: Empathy & Reasoning ────────────────────────────────
        lifecycleScope.launch {
            viewModel.aiEmpathyMessage.collect { message ->
                if (!message.isNullOrBlank()) {
                    binding.cardAiRecommendation.visibility = View.VISIBLE
                    binding.tvAiEmpathyMessage.text = message
                }
            }
        }

        lifecycleScope.launch {
            viewModel.aiReasoning.collect { reasoning ->
                if (!reasoning.isNullOrBlank()) {
                    binding.tvAiReasoning.text = reasoning
                }
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is RecommendationViewModel.RecommendationEvent.NavigateToCheckout -> {
                        // Sync order details for checkout page compatibility
                        syncOrderToLegacyAppState()
                        val intent = Intent(this@RecommendationActivity, CheckoutActivity::class.java).apply {
                            putExtra("TOTAL_CART_EXTRA", event.totalCartAmount)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun syncCartToLegacyAppState() {
        AppState.cart.clear()
        AppState.cart.putAll(ServiceLocator.container.cartRepository.getCartMap())
    }

    private fun syncOrderToLegacyAppState() {
        val orderRepository = ServiceLocator.container.orderRepository
        AppState.lastOrderSnackName = orderRepository.getLastOrderSnackName()
        AppState.lastOrderQty = orderRepository.getLastOrderQty()
        AppState.lastOrderTotalCost = orderRepository.getLastOrderTotalCost()
        AppState.lastOrderSpiceLevel = orderRepository.getLastOrderSpiceLevel()
    }

    // Unused but preserved methods for full API/Signature compatibility
    private fun processCheckout(targetPackage: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
        if (launchIntent != null) {
            try {
                startActivity(launchIntent)
                navigateToLiveTracker()
            } catch (e: Exception) {
                showQrisFallback()
            }
        } else {
            showQrisFallback()
        }
    }

    private fun showQrisFallback() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_qris, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val btnClose = dialogView.findViewById<Button>(R.id.btn_close_qris)
        btnClose?.setOnClickListener {
            dialog.dismiss()
            navigateToLiveTracker()
        }

        dialog.show()
    }

    private fun navigateToLiveTracker() {
        val intent = Intent(this, OrderStatusActivity::class.java)
        startActivity(intent)
    }
}
