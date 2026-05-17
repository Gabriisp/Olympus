package com.example.olympus

// Modelo de un item en el carrito de compras con cantidad variable
data class CarritoItem(
    val articulo: Articulo,
    var cantidad: Int = 1
) {
    val subtotal: Double
        get() = articulo.precio * cantidad
}