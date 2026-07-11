package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.CartRepository
import com.example.cemil_feels.data.repository.OrderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class QrisViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _formattedTime = MutableStateFlow("00:00:15")
    val formattedTime: StateFlow<String> = _formattedTime.asStateFlow()

    private val _totalPaymentText = MutableStateFlow("Rp. 0")
    val totalPaymentText: StateFlow<String> = _totalPaymentText.asStateFlow()

    sealed class QrisEvent {
        object Timeout : QrisEvent()
        object NavigateToConfirmation : QrisEvent()
        object NavigateToHome : QrisEvent()
    }

    private val _eventFlow = MutableSharedFlow<QrisEvent>()
    val eventFlow: SharedFlow<QrisEvent> = _eventFlow.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        val totalPayment = orderRepository.getLastOrderTotalCost()
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        _totalPaymentText.value = "Rp. " + formatter.format(totalPayment.toInt())
        
        startTimer()
    }

    private fun startTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (seconds in 15 downTo 0) {
                _formattedTime.value = String.format(Locale.getDefault(), "00:00:%02d", seconds)
                if (seconds == 0) {
                    cartRepository.clearCart()
                    _eventFlow.emit(QrisEvent.Timeout)
                }
                delay(1000)
            }
        }
    }

    fun onDownloadQris() {
        countdownJob?.cancel()
        orderRepository.savePaymentMethod("QRIS")
        viewModelScope.launch {
            _eventFlow.emit(QrisEvent.NavigateToConfirmation)
        }
    }

    fun onHomeClicked() {
        countdownJob?.cancel()
        viewModelScope.launch {
            _eventFlow.emit(QrisEvent.NavigateToHome)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
