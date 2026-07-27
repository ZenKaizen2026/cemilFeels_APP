package com.example.cemil_feels.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class OrderStep {
    RECEIVED, PREPARING, SHIPPING, ARRIVED, COMPLETED
}

data class OrderSimulationState(
    val currentStep: OrderStep = OrderStep.RECEIVED,
    val timeReceived: String = "",
    val timePreparing: String = "",
    val timeShipping: String = "",
    val timeArrived: String = "",
    val chefStatus: String = "Pesanan Diterima",
    val etaCountdown: String = "Menunggu antrean...",
    val etaTime: String = "",
    val triggerNotification: Pair<String, String>? = null
)

interface OrderRepository {
    fun saveOrderDetails(snackName: String, qty: Int, totalCost: Double, spiceLevel: String)
    fun savePaymentMethod(method: String)
    fun saveOrderId(orderId: String)
    fun getLastOrderId(): String
    fun getLastOrderSnackName(): String
    fun getLastOrderQty(): Int
    fun getLastOrderTotalCost(): Double
    fun getLastOrderSpiceLevel(): String
    fun getLastPaymentMethod(): String
    fun getSimulationFlow(): Flow<OrderSimulationState>
}

class OrderRepositoryImpl : OrderRepository {
    private var lastOrderId: String = "ORDER-NONE"
    private var lastOrderSnackName: String = "Basreng Stik"
    private var lastOrderQty: Int = 1
    private var lastOrderTotalCost: Double = 23000.0
    private var lastOrderSpiceLevel: String = "Pedas"
    private var lastPaymentMethod: String = "ShopeePay"

    override fun saveOrderDetails(snackName: String, qty: Int, totalCost: Double, spiceLevel: String) {
        lastOrderSnackName = snackName
        lastOrderQty = qty
        lastOrderTotalCost = totalCost
        lastOrderSpiceLevel = spiceLevel
    }

    override fun savePaymentMethod(method: String) {
        lastPaymentMethod = method
    }

    override fun saveOrderId(orderId: String) {
        lastOrderId = orderId
    }

    override fun getLastOrderId(): String = lastOrderId
    override fun getLastOrderSnackName(): String = lastOrderSnackName
    override fun getLastOrderQty(): Int = lastOrderQty
    override fun getLastOrderTotalCost(): Double = lastOrderTotalCost
    override fun getLastOrderSpiceLevel(): String = lastOrderSpiceLevel
    override fun getLastPaymentMethod(): String = lastPaymentMethod

    override fun getSimulationFlow(): Flow<OrderSimulationState> = flow {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Step 1: RECEIVED
        val timeRec = Calendar.getInstance()
        val timeEta = Calendar.getInstance().apply { add(Calendar.MINUTE, 25) }
        var state = OrderSimulationState(
            currentStep = OrderStep.RECEIVED,
            timeReceived = sdf.format(timeRec.time),
            chefStatus = "Pesanan Diterima",
            etaCountdown = "Menunggu antrean...",
            etaTime = sdf.format(timeEta.time),
            triggerNotification = Pair("Pesanan Diterima", "Mitra kami telah menerima pesananmu.")
        )
        emit(state)
        
        delay(1500)

        // Step 2: PREPARING
        val timePrep = Calendar.getInstance()
        state = state.copy(
            currentStep = OrderStep.PREPARING,
            timePreparing = sdf.format(timePrep.time),
            chefStatus = "Sedang Disiapkan",
            etaCountdown = "Sedang dimasak (20 menit lagi)...",
            triggerNotification = Pair("Sedang Disiapkan", "Camilanmu sedang digoreng hangat-hangat.")
        )
        emit(state)

        delay(1500)

        // Step 3: SHIPPING
        val timeShip = Calendar.getInstance()
        state = state.copy(
            currentStep = OrderStep.SHIPPING,
            timeShipping = sdf.format(timeShip.time),
            chefStatus = "Sedang Dikirim",
            etaCountdown = "Driver sedang meluncur (10 menit lagi)...",
            triggerNotification = Pair("Sedang Dikirim", "Driver sedang mengantar pesananmu ke alamat.")
        )
        emit(state)

        delay(1500)

        // Step 4: ARRIVED
        val timeArr = Calendar.getInstance()
        state = state.copy(
            currentStep = OrderStep.ARRIVED,
            timeArrived = sdf.format(timeArr.time),
            chefStatus = "Pesanan Tiba!",
            etaCountdown = "Tiba di tujuan",
            triggerNotification = Pair("Pesanan Tiba", "Camilanmu sudah sampai! Nikmati selagi hangat.")
        )
        emit(state)

        delay(1000)

        // Step 5: COMPLETED
        state = state.copy(currentStep = OrderStep.COMPLETED, triggerNotification = null)
        emit(state)
    }
}
