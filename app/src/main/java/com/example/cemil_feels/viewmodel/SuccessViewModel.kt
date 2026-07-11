package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cemil_feels.data.repository.OrderRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SuccessViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    fun getFormattedTotalCost(intentTotal: Double): String {
        val total = if (intentTotal > 0) intentTotal else orderRepository.getLastOrderTotalCost()
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        return "Rp. " + formatter.format(total.toInt())
    }

    fun getPaymentMethod(intentMethod: String?): String {
        val method = intentMethod ?: orderRepository.getLastPaymentMethod()
        orderRepository.savePaymentMethod(method)
        return method
    }

    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH.mm", Locale.forLanguageTag("id-ID"))
        return sdf.format(Calendar.getInstance().time)
    }
}
