package com.example.olympus

object ComidaData {

    val TIPOS_COMIDA = listOf("Desayuno", "Almuerzo", "Merienda", "Cena")

    val EMOJIS_TIPO = mapOf(
        "Desayuno" to "🌅",
        "Almuerzo" to "☀️",
        "Merienda" to "🍎",
        "Cena"     to "🌙"
    )

    fun getComidasDisponibles(): List<ComidaDisponible> = listOf(
        // DESAYUNOS
        ComidaDisponible(1,  "Avena con Frutas",          "Avena cocida con plátano y fresas",            320, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(2,  "Tostadas con Aguacate",     "Pan integral con aguacate y huevo",            380, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(3,  "Huevos Revueltos",          "Huevos revueltos con vegetales",               290, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(4,  "Batido Proteico",           "Batido de proteína con leche y plátano",       350, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(5,  "Yogur con Granola",         "Yogur griego con granola y miel",              280, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(6,  "Pancakes Proteicos",        "Tortitas de avena con proteína",               400, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(7,  "Tortilla Francesa",         "Tortilla con espinacas y queso",               310, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(8,  "Müsli con Leche",           "Müsli integral con leche desnatada",           260, android.R.drawable.ic_menu_gallery),

        // ALMUERZOS
        ComidaDisponible(9,  "Pollo con Arroz",           "Pechuga de pollo a la plancha con arroz",      520, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(10, "Pasta con Atún",            "Pasta integral con atún y vegetales",          480, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(11, "Ensalada de Quinoa",        "Quinoa con vegetales y pollo",                 420, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(12, "Salmón al Horno",           "Salmón con patata y brócoli",                  550, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(13, "Lentejas con Verduras",     "Lentejas estofadas con zanahorias",            430, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(14, "Hamburguesa Saludable",     "Hamburguesa de pavo con ensalada",             490, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(15, "Bowl de Arroz Integral",    "Arroz integral con garbanzos y aguacate",      460, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(16, "Pescado con Patatas",       "Merluza al vapor con patatas hervidas",        410, android.R.drawable.ic_menu_gallery),

        // MERIENDAS
        ComidaDisponible(17, "Manzana con Almendras",     "Manzana y 20g de almendras crudas",            180, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(18, "Batido Verde",              "Batido de espinacas, plátano y proteína",      200, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(19, "Queso Cottage",             "Queso cottage con frutos del bosque",          150, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(20, "Tostada con Mantequilla",  "Tostada integral con mantequilla de almendra", 220, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(21, "Fruta Variada",             "Mix de frutas de temporada",                   120, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(22, "Barrita Proteica Casera",   "Barrita de avena, dátiles y proteína",         240, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(23, "Hummus con Zanahoria",      "Hummus casero con bastones de zanahoria",      160, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(24, "Pudin de Chía",             "Pudin de chía con leche vegetal y frutas",     190, android.R.drawable.ic_menu_gallery),

        // CENAS
        ComidaDisponible(25, "Crema de Verduras",         "Crema de calabacín con quesito",               280, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(26, "Pechuga a la Plancha",      "Pechuga con ensalada verde y tomate",          350, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(27, "Revuelto de Vegetales",     "Revuelto de huevos con espárragos",            300, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(28, "Merluza al Vapor",          "Merluza al vapor con judías verdes",           290, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(29, "Ensalada Completa",         "Ensalada de lechuga, atún y huevo duro",       320, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(30, "Wok de Pollo y Verduras",  "Pollo salteado con brócoli y pimiento",        380, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(31, "Tortilla de Claras",        "Tortilla de claras con champiñones",           220, android.R.drawable.ic_menu_gallery),
        ComidaDisponible(32, "Sopa de Pollo",             "Caldo casero de pollo con fideos",             260, android.R.drawable.ic_menu_gallery)
    )
}

data class ComidaDisponible(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val caloriasBase: Int,
    val imagenRes: Int
)
