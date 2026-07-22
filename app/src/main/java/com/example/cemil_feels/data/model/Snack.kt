package com.example.cemil_feels.data.model

/**
 * Data class yang merepresentasikan camilan (Snack).
 * Sesuai dengan Section 3 & 4 dari PRD, kelas ini menyimpan informasi dasar camilan.
 */
data class Snack(
    val id: Int,
    val name: String,
    val price: Double,
    val rating: Double,
    val description: String,
    val imageResId: Int,
    val stock: Int,
    val flavorTag: String
)
