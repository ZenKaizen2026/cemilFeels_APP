package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.BuildConfig
import com.example.cemil_feels.data.model.AiRecommendationResult
import com.example.cemil_feels.data.model.Snack
import com.example.cemil_feels.data.repository.CartRepository
import com.example.cemil_feels.data.repository.OrderRepository
import com.example.cemil_feels.data.repository.SnackRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationViewModel(
    private val snackRepository: SnackRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val gson = Gson()

    // --- AI-specific State ----------------------------------------------------
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _aiEmpathyMessage = MutableStateFlow<String?>(null)
    val aiEmpathyMessage: StateFlow<String?> = _aiEmpathyMessage.asStateFlow()

    private val _aiReasoning = MutableStateFlow<String?>(null)
    val aiReasoning: StateFlow<String?> = _aiReasoning.asStateFlow()

    // --- Existing State -------------------------------------------------------
    private val _processedMood = MutableStateFlow("Biasa aja")
    val processedMood: StateFlow<String> = _processedMood.asStateFlow()

    private val _selectedSpiceLevel = MutableStateFlow("Pedas")
    val selectedSpiceLevel: StateFlow<String> = _selectedSpiceLevel.asStateFlow()

    private val _selectedSnackNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedSnackNames: StateFlow<Set<String>> = _selectedSnackNames.asStateFlow()

    private val _filteredSnacksOverride = MutableStateFlow<List<Snack>?>(null)

    val filteredSnacksState: StateFlow<List<Snack>> = MutableStateFlow<List<Snack>>(emptyList()).apply {
        viewModelScope.launch {
            combine(
                snackRepository.getSnacks(),
                _processedMood,
                _filteredSnacksOverride
            ) { allSnacks, mood, aiOverride ->
                aiOverride ?: getLocalFallbackSnacks(allSnacks, mood)
            }.collect {
                value = it
            }
        }
    }

    val isSpiceSelectorVisible: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _processedMood.collect { mood ->
                value = mood.equals("Marah", ignoreCase = true) || mood.equals("Cemas", ignoreCase = true)
            }
        }
    }

    val isPesanButtonEnabled: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            cartRepository.getCartItems().collect { cartItems ->
                value = cartItems.isNotEmpty()
            }
        }
    }

    sealed class RecommendationEvent {
        data class NavigateToCheckout(val totalCartAmount: Double) : RecommendationEvent()
    }

    private val _eventFlow = MutableSharedFlow<RecommendationEvent>()
    val eventFlow: SharedFlow<RecommendationEvent> = _eventFlow.asSharedFlow()

    init {
        cartRepository.clearCart()
    }

    // --- Public API -----------------------------------------------------------

    fun initMoodAndStory(story: String?, selectedMood: String?) {
        val normalizedStory = story?.lowercase() ?: ""

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
            normalizedStory.contains("bersyukur") -> "Senang"

            normalizedStory.contains("bosan") || normalizedStory.contains("jenuh") ||
            normalizedStory.contains("penat") -> "Bosan"

            else -> selectedMood ?: "Biasa aja"
        }

        _processedMood.value = moodFromStory

        // Fetch AI recommendation using the resolved mood
        fetchAiRecommendation(story = story ?: "", mood = moodFromStory)
    }

    // --- Gemini AI Integration ------------------------------------------------

    private fun fetchAiRecommendation(story: String, mood: String) {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            android.util.Log.d("GeminiAI", "GEMINI_API_KEY is blank in BuildConfig.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    callGeminiApi(story = story, mood = mood)
                }

                if (result != null) {
                    val allSnacks = snackRepository.getSnacks().first()
                    val aiSnacks = result.selected_snack_ids
                        .mapNotNull { id -> allSnacks.find { it.id == id } }
                        .take(2)

                    if (aiSnacks.isNotEmpty()) {
                        _filteredSnacksOverride.value = aiSnacks
                        _aiEmpathyMessage.value = result.empathy_message
                        _aiReasoning.value = result.reasoning
                    } else {
                        android.util.Log.d("GeminiAI", "AI snacks empty or mapped to unknown IDs: ${result.selected_snack_ids}")
                    }
                } else {
                    android.util.Log.d("GeminiAI", "API response resulted in null parsing result.")
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiAI", "Error in fetchAiRecommendation: ", e)
                _filteredSnacksOverride.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun callGeminiApi(story: String, mood: String): AiRecommendationResult? {
        return try {
            val model = GenerativeModel(
                modelName = "gemini-3.1-flash-lite",
                apiKey = BuildConfig.GEMINI_API_KEY,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )

            val allSnacks = snackRepository.getSnacks().first()
            val snackContext = allSnacks.joinToString("\n") { snack ->
                "ID:${snack.id} | Nama:${snack.name} | Rasa:${snack.flavorTag}"
            }

            val prompt = """
                Kamu adalah asisten rekomendasi camilan emosional yang hangat dan empati.
                
                Informasi:
                - Mood pengguna: $mood
                - Curhatan pengguna: "${story.ifEmpty { "Tidak ada cerita, hanya memilih mood." }}"
                
                Daftar camilan yang tersedia (gunakan ID numerik):
                $snackContext
                
                Aturan pencocokan mood dengan camilan:
                - Marah/Cemas ? pilih camilan berlabel "Pedas" (ID 1 atau 2 diutamakan)
                - Sedih ? pilih camilan berlabel "Manis" (ID 5 atau 6 diutamakan)
                - Senang ? pilih camilan berlabel "Crispy" (ID 3 atau 7 diutamakan)
                - Bosan/Biasa aja ? pilih camilan berlabel "Crispy" atau "Gurih" (ID 7 atau 8)
                
                Kembalikan HANYA JSON valid berikut, tanpa teks lain:
                {
                  "selected_snack_ids": [<id1>, <id2>],
                  "empathy_message": "<pesan empati hangat 1-2 kalimat dalam Bahasa Indonesia>",
                  "reasoning": "<alasan singkat mengapa camilan ini cocok, 1 kalimat>"
                }
            """.trimIndent()

            val response = model.generateContent(prompt)
            val rawJson = response.text?.trim() ?: return null
            android.util.Log.d("GeminiAI", "Raw response from Gemini: $rawJson")
            
            // Ekstrak blok JSON { ... } jika dibungkus markdown backticks atau teks lain
            val cleanJson = if (rawJson.contains("{") && rawJson.contains("}")) {
                val start = rawJson.indexOf("{")
                val end = rawJson.lastIndexOf("}")
                if (end > start) {
                    rawJson.substring(start, end + 1)
                } else {
                    rawJson
                }
            } else {
                rawJson
            }

            android.util.Log.d("GeminiAI", "Cleaned JSON to parse: $cleanJson")
            gson.fromJson(cleanJson, AiRecommendationResult::class.java)
        } catch (e: JsonSyntaxException) {
            android.util.Log.e("GeminiAI", "JSON Syntax Error parsing Gemini response", e)
            null
        } catch (e: Exception) {
            android.util.Log.e("GeminiAI", "Error calling Gemini API", e)
            null
        }
    }

    // --- Local Fallback Logic -------------------------------------------------

    private fun getLocalFallbackSnacks(allSnacks: List<Snack>, mood: String): List<Snack> {
        val m = mood.lowercase(java.util.Locale.ROOT)
        val filtered = when {
            m == "marah" || m == "cemas" ->
                allSnacks.filter { it.name == "Basreng Stik" || it.name == "Tahu Walik" }
            m == "sedih" ->
                allSnacks.filter { it.name == "Piscok Lumer Coklat" || it.name == "Bola Bola Coklat" }
            m == "senang" || m == "bahagia" ->
                allSnacks.filter { it.name == "Cireng Sambal Rujak" || it.name == "Kerupuk Pangsit" }
            m == "bosan" || m == "biasa aja" ->
                allSnacks.filter { it.name == "Kulpi Balado" || it.name == "Makaroni Bantet" }
            else ->
                allSnacks.filter { it.name == "Kulpi Balado" || it.name == "Makaroni Bantet" }
        }
        return filtered.take(2)
    }

    // --- Existing Functions ---------------------------------------------------

    fun setSpiceLevel(level: String) {
        _selectedSpiceLevel.value = level
    }

    fun addSnackToCart(snack: Snack) {
        if (snack.stock > 0) {
            snackRepository.reduceStock(snack.name)
            cartRepository.addToCart(snack.name)
            val currentSelected = _selectedSnackNames.value.toMutableSet()
            currentSelected.add(snack.name)
            _selectedSnackNames.value = currentSelected
        }
    }

    fun toggleSnackSelection(snack: Snack, isSelected: Boolean) {
        val currentSelected = _selectedSnackNames.value.toMutableSet()
        if (isSelected) {
            currentSelected.add(snack.name)
            if (!cartRepository.getCartMap().containsKey(snack.name)) {
                cartRepository.addToCart(snack.name)
            }
        } else {
            currentSelected.remove(snack.name)
            cartRepository.removeFromCart(snack.name)
        }
        _selectedSnackNames.value = currentSelected
    }

    fun onPesanClicked() {
        val cartMap = cartRepository.getCartMap()
        var totalCartAmount = 0.0
        var lastSnackName = "Basreng Stik"
        var lastQty = 1

        cartMap.forEach { (snackName, qty) ->
            val snackObj = snackRepository.getSnackByName(snackName)
            if (snackObj != null) {
                totalCartAmount += qty * snackObj.price
                lastSnackName = snackName
                lastQty = qty
            }
        }

        if (totalCartAmount == 0.0) {
            totalCartAmount = 16000.0
        }

        val deliveryCost = totalCartAmount + 5000.0 + 2000.0

        orderRepository.saveOrderDetails(
            snackName = lastSnackName,
            qty = lastQty,
            totalCost = deliveryCost,
            spiceLevel = _selectedSpiceLevel.value
        )

        viewModelScope.launch {
            _eventFlow.emit(RecommendationEvent.NavigateToCheckout(totalCartAmount))
        }
    }
}
