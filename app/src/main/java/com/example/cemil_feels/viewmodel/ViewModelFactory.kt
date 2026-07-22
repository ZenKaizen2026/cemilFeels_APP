package com.example.cemil_feels.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cemil_feels.data.repository.PaymentRepository // Import PaymentRepository jika belum ada
import com.example.cemil_feels.di.AppContainer

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(container.userRepository) as T
            }
            modelClass.isAssignableFrom(VentingViewModel::class.java) -> {
                VentingViewModel(container.userRepository) as T
            }
            modelClass.isAssignableFrom(RecommendationViewModel::class.java) -> {
                RecommendationViewModel(
                    container.snackRepository,
                    container.cartRepository,
                    container.orderRepository
                ) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(container.cartRepository) as T
            }
            modelClass.isAssignableFrom(CheckoutViewModel::class.java) -> {
                CheckoutViewModel(container.cartRepository, container.orderRepository) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                PaymentViewModel(
                    container.orderRepository,
                    PaymentRepository() // ✅ DITAMBAHKAN
                ) as T
            }
            modelClass.isAssignableFrom(QrisViewModel::class.java) -> {
                QrisViewModel(container.cartRepository, container.orderRepository) as T
            }
            modelClass.isAssignableFrom(PaymentConfirmationViewModel::class.java) -> {
                PaymentConfirmationViewModel(container.orderRepository) as T
            }
            modelClass.isAssignableFrom(SuccessViewModel::class.java) -> {
                SuccessViewModel(container.orderRepository) as T
            }
            modelClass.isAssignableFrom(OrderStatusViewModel::class.java) -> {
                OrderStatusViewModel(container.orderRepository) as T
            }
            modelClass.isAssignableFrom(OrderCompletedViewModel::class.java) -> {
                OrderCompletedViewModel(container.orderRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}