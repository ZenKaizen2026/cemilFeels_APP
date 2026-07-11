package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cemil_feels.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PaymentViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _selectedMethod = MutableStateFlow("ShopeePay")
    val selectedMethod: StateFlow<String> = _selectedMethod.asStateFlow()

    fun selectMethod(method: String) {
        _selectedMethod.value = method
    }

    fun getTargetPackageName(): String {
        return when (_selectedMethod.value) {
            "ShopeePay" -> "com.shopee.id"
            "GoPay" -> "com.gojek.app"
            "DANA" -> "id.dana"
            "OVO" -> "id.ovo"
            else -> ""
        }
    }

    fun savePaymentMethod() {
        orderRepository.savePaymentMethod(_selectedMethod.value)
    }
}
