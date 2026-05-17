package com.example.olympus

// Modelo de datos para representar una compra realizada por el usuario en la tienda
data class Compra(
    val id: String = "",
    val userUid: String = "",
    val items: List<CompraItem> = emptyList(),
    val total: Double = 0.0,
    val fecha: Long = System.currentTimeMillis()
)

// Modelo de un item dentro de una compra
data class CompraItem(
    val articuloId: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)