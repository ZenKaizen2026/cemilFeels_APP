package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.model.CartItem
import com.example.cemil_feels.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    val cartItemsState: StateFlow<List<CartItem>> = MutableStateFlow<List<CartItem>>(emptyList()).apply {
        viewModelScope.launch {
            cartRepository.getCartItems().collect {
                value = it
            }
        }
    }

    val formattedSubtotalState: StateFlow<String> = MutableStateFlow("Rp. 0").apply {
        viewModelScope.launch {
            cartItemsState.collect { items ->
                var subtotal = 0.0
                items.forEach { item ->
                    subtotal += item.qty * item.snack.price
                }
                val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                value = "Rp. " + formatter.format(subtotal.toInt())
            }
        }
    }

    val isCheckoutEnabledState: StateFlow<Boolean> = MutableStateFlow(false).apply {
        viewModelScope.launch {
            cartItemsState.collect { items ->
                value = items.isNotEmpty()
            }
        }
    }

    sealed class CartEvent {
        object RedirectToRecommendation : CartEvent()
        data class NavigateToCheckout(val subtotal: Double) : CartEvent()
    }

    private val _eventFlow = MutableSharedFlow<CartEvent>()
    val eventFlow: SharedFlow<CartEvent> = _eventFlow.asSharedFlow()

    fun incrementItem(item: CartItem) {
        cartRepository.incrementCartItem(item.snack.name)
    }

    fun decrementItem(item: CartItem) {
        cartRepository.decrementCartItem(item.snack.name)
    }

    fun removeItem(item: CartItem) {
        cartRepository.removeFromCart(item.snack.name)
        checkEmptyAndTriggerRedirect()
    }

    fun checkEmptyAndTriggerRedirect() {
        if (cartRepository.isCartEmpty()) {
            viewModelScope.launch {
                _eventFlow.emit(CartEvent.RedirectToRecommendation)
            }
        }
    }

    fun onCheckoutClicked() {
        val subtotal = cartRepository.getSubtotal()
        viewModelScope.launch {
            _eventFlow.emit(CartEvent.NavigateToCheckout(subtotal))
        }
    }
}
