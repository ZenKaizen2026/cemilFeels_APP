package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.model.Snack
import com.example.cemil_feels.data.repository.CartRepository
import com.example.cemil_feels.data.repository.OrderRepository
import com.example.cemil_feels.data.repository.SnackRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val snackRepository: SnackRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _processedMood = MutableStateFlow("Biasa aja")
    val processedMood: StateFlow<String> = _processedMood.asStateFlow()

    private val _selectedSpiceLevel = MutableStateFlow("Pedas")
    val selectedSpiceLevel: StateFlow<String> = _selectedSpiceLevel.asStateFlow()

    private val _selectedSnackNames = MutableStateFlow<Set<String>>(emptySet())
    val selectedSnackNames: StateFlow<Set<String>> = _selectedSnackNames.asStateFlow()

    // Flow of filtered snacks mapped from the repository's snack list based on processedMood
    val filteredSnacksState: StateFlow<List<Snack>> = MutableStateFlow<List<Snack>>(emptyList()).apply {
        viewModelScope.launch {
            combine(snackRepository.getSnacks(), _processedMood) { allSnacks, mood ->
                when (mood) {
                    "Marah", "Cemas" -> allSnacks.filter { it.name == "Basreng Stik" || it.name == "Makaroni Bantet" }
                    "Bahagia" -> allSnacks.filter { it.name == "Cireng Sambal Rujak" || it.name == "Tahu Walik" }
                    "Sedih" -> allSnacks.filter { it.name == "Piscok Lumer Coklat" || it.name == "Bola Bola Coklat" }
                    "Biasa aja" -> allSnacks.filter { it.name == "Kulpi Balado" || it.name == "Kerupuk Pangsit" }
                    else -> allSnacks
                }
            }.collect {
                value = it
            }
        }
    }

    // Expose whether the spice selector should be visible
    val isSpiceSelectorVisible: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            _processedMood.collect { mood ->
                value = mood.equals("Marah", ignoreCase = true)
            }
        }
    }

    // Expose whether the checkout/order button is enabled
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
        // Clear the cart on starting, as per the specifications
        cartRepository.clearCart()
    }

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
            normalizedStory.contains("bersyukur") -> "Bahagia"
            
            else -> selectedMood ?: "Biasa aja"
        }

        _processedMood.value = moodFromStory
    }

    fun setSpiceLevel(level: String) {
        _selectedSpiceLevel.value = level
    }

    fun addSnackToCart(snack: Snack) {
        if (snack.stock > 0) {
            // Reduce stock count in repository
            snackRepository.reduceStock(snack.name)
            // Add quantity in repository
            cartRepository.addToCart(snack.name)
            // Highlight visually
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

        val deliveryCost = totalCartAmount + 5000.0 + 2000.0 // Subtotal + Ongkir + Jasa
        
        // Cache order details
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
