package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.CartRepository
import com.example.cemil_feels.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class CheckoutUiState(
    val subtotalText: String = "",
    val totalText: String = "",
    val firstItemName: String = "",
    val firstItemPriceQty: String = "",
    val firstItemImageResId: Int = 0,
    val isSpiceLevelVisible: Boolean = false,
    val spiceLevelText: String = "",
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
            // Get subtotal from repository, fallback to intent subtotal
            val repoSubtotal = cartRepository.getSubtotal()
            val finalSubtotal = if (repoSubtotal > 0) repoSubtotal else intentSubtotal
            val grandTotal = finalSubtotal + shippingFee + serviceFee

            // Fetch first item in the cart to represent the summary
            val cartItems = cartRepository.getCartMap()
            val firstItem = cartItems.entries.firstOrNull()

            var itemName = ""
            var itemPriceQty = ""
            var itemImageResId = 0 // Will fallback in activity if 0
            var isSpiceVisible = false
            var spiceText = ""

            if (firstItem != null) {
                val snackName = firstItem.key
                val qty = firstItem.value
                
                // Lookup snack details
                // To avoid needing SnackRepository directly in CheckoutViewModel,
                // we can look up from orderRepository details, or retrieve snackName.
                // But wait, it's easier to check if name is Basreng Stik or Makaroni Bantet.
                // Let's use the last order details from orderRepository as source of truth.
                val lastOrderSnack = orderRepository.getLastOrderSnackName()
                val lastQty = orderRepository.getLastOrderQty()
                
                val otherItemsCount = cartItems.size - 1
                itemName = if (otherItemsCount > 0) {
                    "$snackName (+$otherItemsCount item lainnya)"
                } else {
                    snackName
                }

                // Since we need the individual snack price to format "Rp. Price Qty X",
                // we can extract it by dividing subtotal or checking if we can find it.
                // However, let's keep it safe. Let's just find the snack from the snack list.
                // Wait! To make CheckoutViewModel self-contained and clean, let's get the price from the snack or use fallback.
                // Let's find it using standard check or pass SnackRepository. But we can also do it directly since static details are known.
                val itemPrice = when (snackName) {
                    "Basreng Stik" -> 16000.0
                    "Makaroni Bantet" -> 10000.0
                    "Cireng Sambal Rujak" -> 15000.0
                    "Tahu Walik" -> 12000.0
                    "Piscok Lumer Coklat" -> 12000.0
                    "Bola Bola Coklat" -> 14000.0
                    "Kulpi Balado" -> 11000.0
                    "Kerupuk Pangsit" -> 8000.0
                    else -> 16000.0
                }

                val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                itemPriceQty = "Rp. ${formatter.format(itemPrice.toInt())} ${qty} X"

                itemImageResId = when (snackName) {
                    "Basreng Stik" -> com.example.cemil_feels.R.drawable.basreng_stik
                    "Makaroni Bantet" -> com.example.cemil_feels.R.drawable.makaroni_bantet
                    "Cireng Sambal Rujak" -> com.example.cemil_feels.R.drawable.cireng_sambal_rujak
                    "Tahu Walik" -> com.example.cemil_feels.R.drawable.tahu_walik
                    "Piscok Lumer Coklat" -> com.example.cemil_feels.R.drawable.piscok_lumer_coklat
                    "Bola Bola Coklat" -> com.example.cemil_feels.R.drawable.bola_bola_coklat
                    "Kulpi Balado" -> com.example.cemil_feels.R.drawable.kulpi_balado
                    "Kerupuk Pangsit" -> com.example.cemil_feels.R.drawable.krupuk_pangsit
                    else -> com.example.cemil_feels.R.drawable.basreng_stik
                }

                isSpiceVisible = snackName.equals("Basreng Stik", ignoreCase = true) ||
                        snackName.equals("Makaroni Bantet", ignoreCase = true)
                spiceText = "Level: ${orderRepository.getLastOrderSpiceLevel()}"

                // Dynamically sync order details with repository for success & dialog screens
                orderRepository.saveOrderDetails(
                    snackName = snackName,
                    qty = qty,
                    totalCost = grandTotal,
                    spiceLevel = orderRepository.getLastOrderSpiceLevel()
                )
            }

            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            _uiState.value = CheckoutUiState(
                subtotalText = "Rp. " + formatter.format(finalSubtotal.toInt()),
                totalText = "Rp. " + formatter.format(grandTotal.toInt()),
                firstItemName = itemName,
                firstItemPriceQty = itemPriceQty,
                firstItemImageResId = itemImageResId,
                isSpiceLevelVisible = isSpiceVisible,
                spiceLevelText = spiceText,
                totalCost = grandTotal
            )
        }
    }
}
