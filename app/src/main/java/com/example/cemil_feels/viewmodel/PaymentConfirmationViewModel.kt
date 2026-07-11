package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PaymentConfirmationViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    sealed class ConfirmationEvent {
        data class NavigateToSuccess(val totalPayment: Double, val paymentMethod: String) : ConfirmationEvent()
    }

    private val _eventFlow = MutableSharedFlow<ConfirmationEvent>()
    val eventFlow: SharedFlow<ConfirmationEvent> = _eventFlow.asSharedFlow()

    fun startConfirmationTimer(intentTotal: Double, intentMethod: String?) {
        viewModelScope.launch {
            delay(1200)
            val finalTotal = if (intentTotal > 0) intentTotal else orderRepository.getLastOrderTotalCost()
            val finalMethod = intentMethod ?: orderRepository.getLastPaymentMethod()
            
            _eventFlow.emit(ConfirmationEvent.NavigateToSuccess(finalTotal, finalMethod))
        }
    }
}
