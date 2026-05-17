package com.example.olympus

// Modelo de datos de un articulo disponible en la tienda
data class Articulo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagenResId: Int = R.drawable.ic_tienda_placeholder
)

// Lista estatica de articulos disponibles en la tienda
object ArticulosData {
    val listaArticulos: List<Articulo> = listOf(
        Articulo(1, "Mancuernas 5kg", "Par de mancuernas ajustables 2.5-5kg", 29.99, R.drawable.mancuernas_5kg),
        Articulo(2, "Banda Elástica", "Kit de bandas resistencia media", 14.99, R.drawable.banda_elastica),
        Articulo(3, "Balón Medicinal 3kg", "Balón para Core y rehabilitación", 12.99, R.drawable.balon_medicinal_3kg),
        Articulo(4, "Guantes Gym", "Protección manos antideslizantes", 9.99, R.drawable.guantes_gym),
        Articulo(5, "Bolsa Deporte", "Bolsa táctica resistente", 19.99, R.drawable.bolsa_deporte),
        Articulo(6, "Camiseta Athletic", "Algodón deportivo fresco", 15.99, R.drawable.camiseta_athletic),
        Articulo(7, "Leggings Fitness", "Pantalón tensor cómodo", 24.99, R.drawable.leggings_fitness),
        Articulo(8, "Water Bottle", "Botella acero inoxidable 750ml", 11.99, R.drawable.water_bottle),
        Articulo(9, "Toalla Microfibra", "Secado rápido antibacterial", 8.99, R.drawable.toalla_microfibra),
        Articulo(10, "Rack Pesas", "Organizador pared para pesas", 34.99, R.drawable.rack_pesas)
    )
}