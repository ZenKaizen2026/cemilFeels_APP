package com.example.cemil_feels

/**
 * Data class yang merepresentasikan camilan (Snack).
 * Sesuai dengan Section 3 & 4 dari PRD, kelas ini menyimpan informasi dasar camilan.
 *
 * @property name Nama dari camilan (misalnya: "Basreng", "Papeda Gulunk").
 * @property price Harga camilan dalam Rupiah (misalnya: 16000.0).
 * @property rating Rating kualitas/rasa camilan (misalnya: 4.5).
 * @property description Deskripsi singkat mengenai keunggulan camilan tersebut.
 * @property imageResId ID referensi resource drawable gambar camilan (misalnya: R.drawable.basreng_stik).
 * @property stock Jumlah persediaan stok camilan yang tersedia secara lokal (FT-03).
 */
data class Snack(
    val name: String,
    val price: Double,
    val rating: Double,
    val description: String,
    val imageResId: Int,
    val stock: Int
)

