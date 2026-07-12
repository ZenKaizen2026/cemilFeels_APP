package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.model.CartItem
import com.example.cemil_feels.data.repository.CartRepository
import com.example.cemil_feels.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CheckoutUiItem(
    val name: String,
    val priceText: String,
    val qtyText: String,
    val imageResId: Int,
    val isSpiceLevelVisible: Boolean,
    val spiceLevelText: String
)

data class CheckoutUiState(
    val subtotalText: String = "",
    val totalText: String = "",
    val cartItems: List<CheckoutUiItem> = emptyList(),
    val totalCost: Double = 0.0
)

class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val shippingFee = 5000.0
    private val serviceFee = 2000.0

    fun calculateCheckoutDetails(intentSubtotal: Double) {
        viewModelScope.launch {
            val cartItems = cartRepository.getCartItems().first()
            val finalSubtotal = if (intentSubtotal > 0) intentSubtotal else cartRepository.getSubtotal()
            val grandTotal = finalSubtotal + shippingFee + serviceFee

            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            
            val uiItems = cartItems.map { item ->
                val snack = item.snack
                val isSpiceVisible = snack.name.equals("Basreng Stik", ignoreCase = true) ||
                        snack.name.equals("Makaroni Bantet", ignoreCase = true)
                
                CheckoutUiItem(
                    name = snack.name,
                    priceText = "Rp ${formatter.format(snack.price.toInt())}",
                    qtyText = "${item.qty}x",
                    imageResId = snack.imageResId,
                    isSpiceLevelVisible = isSpiceVisible,
                    spiceLevelText = if (isSpiceVisible) "Level: ${orderRepository.getLastOrderSpiceLevel()}" else ""
                )
            }

            // Sync legacy order repository with first item for backward compatibility if needed
            cartItems.firstOrNull()?.let { firstItem ->
                orderRepository.saveOrderDetails(
                    snackName = firstItem.snack.name,
                    qty = firstItem.qty,
                    totalCost = grandTotal,
                    spiceLevel = orderRepository.getLastOrderSpiceLevel()
                )
            }

            _uiState.value = CheckoutUiState(
                subtotalText = "Rp " + formatter.format(finalSubtotal.toInt()),
                totalText = "Rp " + formatter.format(grandTotal.toInt()),
                cartItems = uiItems,
                totalCost = grandTotal
            )
        }
    }
}
