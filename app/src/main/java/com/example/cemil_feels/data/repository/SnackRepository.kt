package com.example.cemil_feels.data.repository

import com.example.cemil_feels.R
import com.example.cemil_feels.data.model.Snack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface SnackRepository {
    fun getSnacks(): Flow<List<Snack>>
    fun reduceStock(snackName: String)
    fun resetStock()
    fun getSnackByName(name: String): Snack?
}

class SnackRepositoryImpl : SnackRepository {
    private val staticSnacks = listOf(
        Snack(
            name = "Basreng Stik",
            price = 16000.0,
            rating = 4.5,
            description = "Pedasnya basreng nagih, instan usir bad mood dan bikin happy!",
            imageResId = R.drawable.basreng_stik,
            stock = 5
        ),
        Snack(
            name = "Makaroni Bantet",
            price = 10000.0,
            rating = 4.4,
            description = "Makaroni bantet renyah gurih pedas khas jajanan pasar.",
            imageResId = R.drawable.makaroni_bantet,
            stock = 3
        ),
        Snack(
            name = "Cireng Sambal Rujak",
            price = 15000.0,
            rating = 4.7,
            description = "Cireng hangat renyah disajikan dengan saus rujak manis pedas.",
            imageResId = R.drawable.cireng_sambal_rujak,
            stock = 4
        ),
        Snack(
            name = "Tahu Walik",
            price = 12000.0,
            rating = 4.6,
            description = "Tahu goreng walik isi adonan bakso ayam gurih lezat.",
            imageResId = R.drawable.tahu_walik,
            stock = 2
        ),
        Snack(
            name = "Piscok Lumer Coklat",
            price = 12000.0,
            rating = 4.8,
            description = "Pisang coklat goreng renyah dengan coklat lumer melimpah.",
            imageResId = R.drawable.piscok_lumer_coklat,
            stock = 3
        ),
        Snack(
            name = "Bola Bola Coklat",
            price = 14000.0,
            rating = 4.5,
            description = "Kue manis berbentuk bola berbalut mesis coklat legit.",
            imageResId = R.drawable.bola_bola_coklat,
            stock = 0
        ),
        Snack(
            name = "Kulpi Balado",
            price = 11000.0,
            rating = 4.3,
            description = "Keripik kulit lumpia renyah bertabur bumbu balado manis gurih.",
            imageResId = R.drawable.kulpi_balado,
            stock = 4
        ),
        Snack(
            name = "Kerupuk Pangsit",
            price = 8000.0,
            rating = 4.2,
            description = "Kerupuk pangsit goreng renyah teman santai hari ini.",
            imageResId = R.drawable.krupuk_pangsit,
            stock = 6
        )
    )

    private val _snacks = MutableStateFlow(staticSnacks)

    override fun getSnacks(): Flow<List<Snack>> = _snacks.asStateFlow()

    override fun reduceStock(snackName: String) {
        val updated = _snacks.value.map { snack ->
            if (snack.name == snackName && snack.stock > 0) {
                snack.copy(stock = snack.stock - 1)
            } else {
                snack
            }
        }
        _snacks.value = updated
    }

    override fun resetStock() {
        _snacks.value = staticSnacks
    }

    override fun getSnackByName(name: String): Snack? {
        return _snacks.value.find { it.name == name }
    }
}
