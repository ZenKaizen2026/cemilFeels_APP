package com.example.cemil_feels

/**
 * Singleton State Penyimpan Runtime Session & Detail Transaksi Dinamis.
 * Digunakan agar data tetap konsisten selama siklus hidup aplikasi dan mencegah reset acak.
 */
object AppState {
    // Kredensial Login Pengguna
    var loggedInEmail: String? = null
    var loggedInPassword: String? = null
    
    // Nama Panggilan Default yang Dikunci
    const val USER_NICKNAME = "Cemil"
    
    // Keranjang belanja riil lokal (Nama Camilan -> Quantity)
    val cart = mutableMapOf<String, Int>()
    
    // Informasi Pesanan Terakhir (Dinamis dari Rekomendasi/Keranjang)
    var lastOrderSnackName: String = "Basreng Stik"
    var lastOrderQty: Int = 1
    var lastOrderTotalCost: Double = 23000.0
    var lastOrderSpiceLevel: String = "Sedang"
    var lastPaymentMethod: String = "ShopeePay"
}
