package com.example.olympus

// Gestor singleton del carrito de compras de la tienda
// Mantiene el estado del carrito en memoria durante la sesion

class CarritoManager private constructor() {

    private val items: MutableList<CarritoItem> = mutableListOf()

    // Agrega un articulo al carrito
    // Si ya existe, incrementa la cantidad en 1
    fun agregarArticulo(articulo: Articulo) {
        val existente = items.find { it.articulo.id == articulo.id }
        if (existente != null) {
            existente.cantidad++
        } else {
            items.add(CarritoItem(articulo, 1))
        }
    }

    // Elimina una unidad del articulo del carrito
    // Si la cantidad es 1, elimina el articulo completamente
    fun eliminarArticulo(articulo: Articulo) {
        val existente = items.find { it.articulo.id == articulo.id }
        if (existente != null) {
            if (existente.cantidad > 1) {
                existente.cantidad--
            } else {
                items.remove(existente)
            }
        }
    }

    // Devuelve una copia de la lista de items del carrito
    fun getItems(): List<CarritoItem> = items.toList()

    // Calcula el total del carrito sumando subtotales de todos los items
    fun getTotal(): Double = items.sumOf { it.subtotal }

    // Cuenta el numero total de unidades en el carrito
    fun getCantidadTotal(): Int = items.sumOf { it.cantidad }

    // Vacia el carrito eliminando todos los items
    fun vaciarCarrito() {
        items.clear()
    }

    // Verifica si el carrito tiene algun articulo
    fun tieneArticulos(): Boolean = items.isNotEmpty()

    // Singleton con double-checked locking para seguridad en hilos
    companion object {
        @Volatile
        private var instancia: CarritoManager? = null

        fun getInstance(): CarritoManager {
            return instancia ?: synchronized(this) {
                instancia ?: CarritoManager().also { instancia = it }
            }
        }
    }
}