package com.example.cemil_feels.di

import com.example.cemil_feels.data.repository.*

interface AppContainer {
    val snackRepository: SnackRepository
    val cartRepository: CartRepository
    val userRepository: UserRepository
    val orderRepository: OrderRepository
}

class AppContainerImpl : AppContainer {
    override val snackRepository: SnackRepository by lazy { SnackRepositoryImpl() }
    override val cartRepository: CartRepository by lazy { CartRepositoryImpl(snackRepository) }
    override val userRepository: UserRepository by lazy { UserRepositoryImpl() }
    override val orderRepository: OrderRepository by lazy { OrderRepositoryImpl() }
}

/**
 * Service Locator untuk memfasilitasi injeksi dependensi manual.
 * Mengurangi kebutuhan setup Application subclass yang berisiko merusak manifes Android.
 */
object ServiceLocator {
    val container: AppContainer by lazy { AppContainerImpl() }
}
