package com.example.cemil_feels.data.repository

import com.example.cemil_feels.data.model.CartItem
import com.example.cemil_feels.data.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartMap(): Map<String, Int>
    fun addToCart(snackName: String)
    fun removeFromCart(snackName: String)
    fun incrementCartItem(snackName: String)
    fun decrementCartItem(snackName: String)
    fun clearCart()
    fun getSubtotal(): Double
    fun isCartEmpty(): Boolean
}

class CartRepositoryImpl(
    private val snackRepository: SnackRepository
) : CartRepository {

    // local in-memory representation of cart matching AppState.cart structure
    private val _cartMap = MutableStateFlow<Map<String, Int>>(emptyMap())

    override fun getCartItems(): Flow<List<CartItem>> {
        return _cartMap.map { map ->
            map.mapNotNull { (snackName, qty) ->
                val snack = snackRepository.getSnackByName(snackName)
                if (snack != null) {
                    CartItem(snack, qty)
                } else {
                    null
                }
            }
        }
    }

    override fun getCartMap(): Map<String, Int> = _cartMap.value

    override fun addToCart(snackName: String) {
        val current = _cartMap.value.toMutableMap()
        val currentQty = current[snackName] ?: 0
        current[snackName] = currentQty + 1
        _cartMap.value = current
    }

    override fun removeFromCart(snackName: String) {
        val current = _cartMap.value.toMutableMap()
        current.remove(snackName)
        _cartMap.value = current
    }

    override fun incrementCartItem(snackName: String) {
        val current = _cartMap.value.toMutableMap()
        val currentQty = current[snackName] ?: 0
        current[snackName] = currentQty + 1
        _cartMap.value = current
    }

    override fun decrementCartItem(snackName: String) {
        val current = _cartMap.value.toMutableMap()
        val currentQty = current[snackName] ?: 0
        if (currentQty > 1) {
            current[snackName] = currentQty - 1
            _cartMap.value = current
        }
    }

    override fun clearCart() {
        _cartMap.value = emptyMap()
    }

    override fun getSubtotal(): Double {
        var subtotal = 0.0
        _cartMap.value.forEach { (snackName, qty) ->
            val snack = snackRepository.getSnackByName(snackName)
            if (snack != null) {
                subtotal += snack.price * qty
            }
        }
        return subtotal
    }

    override fun isCartEmpty(): Boolean {
        return _cartMap.value.isEmpty()
    }
}
