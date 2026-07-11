package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cemil_feels.data.repository.OrderRepository
import com.example.cemil_feels.data.repository.OrderSimulationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
}
