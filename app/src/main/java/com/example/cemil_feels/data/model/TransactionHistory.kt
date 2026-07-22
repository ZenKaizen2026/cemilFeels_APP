package com.example.cemil_feels.data.model

data class TransactionHistory(
    val dateTime: String,
    val totalCost: Double,
    val paymentMethod: String,
    val items: List<HistoryItem>
)

data class HistoryItem(
    val snackName: String,
    val qty: Int,
    val price: Double
)
