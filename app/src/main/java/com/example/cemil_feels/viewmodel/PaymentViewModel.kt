package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.OrderRepository
import com.example.cemil_feels.data.repository.PaymentRepository
import com.example.cemil_feels.data.repository.PaymentResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class PaymentUiEvent {
    data class LaunchMidtransSnap(val snapToken: String) : PaymentUiEvent()
    data class ShowError(val message: String) : PaymentUiEvent()
    object ShowLoading : PaymentUiEvent()
    object HideLoading : PaymentUiEvent()
}

class PaymentViewModel(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _selectedMethod = MutableStateFlow("ShopeePay")
    val selectedMethod: StateFlow<String> = _selectedMethod.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableSharedFlow<PaymentUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun selectMethod(method: String) {
        _selectedMethod.value = method
    }

    fun savePaymentMethod() {
        orderRepository.savePaymentMethod(_selectedMethod.value)
    }

    fun getTargetPackageName(): String {
        return when (_selectedMethod.value) {
            "ShopeePay" -> "com.shopee.id"
            "GoPay"     -> "com.gojek.app"
            "DANA"      -> "id.dana"
            "OVO"       -> "id.ovo"   // ✅ FIX: gunakan "id.ovo" bukan "ovo.id"
            else        -> ""
        }
    }

    /**
     * Meminta Snap Token dari Merchant Server (Render.com),
     * lalu meluncurkan Midtrans Snap UI dengan token yang diterima.
     */
    fun requestSnapToken(totalAmount: Double, customerName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val orderId = "ORDER-${UUID.randomUUID().toString().take(8).uppercase()}"
            val amountInRupiah = totalAmount.toInt()

            val result = paymentRepository.getSnapToken(
                orderId = orderId,
                amount = amountInRupiah,
                customerName = customerName
            )

            _isLoading.value = false

            when (result) {
                is PaymentResult.Success -> {
                    _uiEvent.emit(PaymentUiEvent.LaunchMidtransSnap(result.token))
                }
                is PaymentResult.NetworkTimeout -> {
                    _uiEvent.emit(PaymentUiEvent.ShowError(
                        "Server timeout. Kemungkinan server sedang bangun (cold start Render). " +
                                "Tunggu 30 detik dan coba lagi."
                    ))
                }
                is PaymentResult.Error -> {
                    _uiEvent.emit(PaymentUiEvent.ShowError(result.message))
                }
            }
        }
    }
}