package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.OrderRepository
import com.example.cemil_feels.data.repository.OrderSimulationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderStatusViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _simulationState = MutableStateFlow(OrderSimulationState())
    val simulationState: StateFlow<OrderSimulationState> = _simulationState.asStateFlow()

    init {
        startSimulation()
    }

    private fun startSimulation() {
        viewModelScope.launch {
            orderRepository.getSimulationFlow().collect { state ->
                _simulationState.value = state
            }
        }
    }

    fun getPaymentMethod(): String = orderRepository.getLastPaymentMethod()

    fun getFormattedQuantity(): String = "${orderRepository.getLastOrderQty()} (100gr)"

    fun getSnackName(): String = orderRepository.getLastOrderSnackName()

    fun getFormattedTotalCost(): String {
        val cost = orderRepository.getLastOrderTotalCost()
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return "Rp. " + formatter.format(cost.toInt())
    }

    fun getFormattedTime(): String {
        val sdfTime = SimpleDateFormat("HH:mm", Locale.forLanguageTag("id-ID"))
        return sdfTime.format(Calendar.getInstance().time)
    }

    fun getFormattedDate(): String {
        val sdfDate = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        return sdfDate.format(Calendar.getInstance().time)
    }
}
