package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cemil_feels.data.repository.OrderRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderCompletedViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    fun getPaymentMethod(): String = orderRepository.getLastPaymentMethod()
    
    fun getOrderId(): String = orderRepository.getLastOrderId()
    
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
