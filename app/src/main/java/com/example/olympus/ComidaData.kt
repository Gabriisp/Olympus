package com.example.olympus

// Lista de tipos de comida y datos de comidas disponibles para planes nutricionales

object ComidaData {

    val TIPOS_COMIDA = listOf("Desayuno", "Almuerzo", "Merienda", "Cena")

    private val comidasMap = mapOf(
        1 to R.drawable.avena_con_frutas,
        2 to R.drawable.tostadas_con_aguacate,
        3 to R.drawable.huevos_revueltos,
        4 to R.drawable.batido_proteico,
        5 to R.drawable.yogur_con_granola,
        6 to R.drawable.pancakes_proteicos,
        7 to R.drawable.tortilla_francesa,
        8 to R.drawable.musli_con_leche,
        9 to R.drawable.pollo_con_arroz,
        10 to R.drawable.pasta_con_atun,
        11 to R.drawable.ensalada_de_quinoa,
        12 to R.drawable.salmon_al_horno,
        13 to R.drawable.lentejas_con_verduras,
        14 to R.drawable.hamburguesa_saludable,
        15 to R.drawable.bowl_de_arroz_integral,
        16 to R.drawable.pescado_con_patatas,
        17 to R.drawable.manzana_con_almendras,
        18 to R.drawable.batido_verde,
        19 to R.drawable.queso_cottage,
        20 to R.drawable.tostada_con_mantequilla,
        21 to R.drawable.fruta_variada,
        22 to R.drawable.barrita_proteica_casera,
        23 to R.drawable.hummus_con_zanahoria,
        24 to R.drawable.pudin_de_chia,
        25 to R.drawable.crema_de_verduras,
        26 to R.drawable.pechuga_a_la_plancha,
        27 to R.drawable.revuelto_de_vegetales,
        28 to R.drawable.merluza_al_vapor,
        29 to R.drawable.merluza_al_vapor,
        30 to R.drawable.work_de_pollo_y_verduras,
        31 to R.drawable.tortilla_de_claras,
        32 to R.drawable.sopa_de_pollo
    )

    fun getDrawableResId(imagenId: Int): Int {
        return comidasMap[imagenId] ?: android.R.drawable.ic_menu_gallery
    }

    fun getComidasDisponibles(): List<ComidaDisponible> = listOf(
        // DESAYUNOS
        ComidaDisponible(1,  "Avena con Frutas",          "Avena cocida con plátano y fresas",            320, R.drawable.avena_con_frutas),
        ComidaDisponible(2,  "Tostadas con Aguacate",     "Pan integral con aguacate y huevo",            380, R.drawable.tostadas_con_aguacate),
        ComidaDisponible(3,  "Huevos Revueltos",          "Huevos revueltos con vegetales",               290, R.drawable.huevos_revueltos),
        ComidaDisponible(4,  "Batido Proteico",           "Batido de proteína con leche y plátano",       350, R.drawable.batido_proteico),
        ComidaDisponible(5,  "Yogur con Granola",         "Yogur griego con granola y miel",              280, R.drawable.yogur_con_granola),
        ComidaDisponible(6,  "Pancakes Proteicos",        "Tortitas de avena con proteína",               400, R.drawable.pancakes_proteicos),
        ComidaDisponible(7,  "Tortilla Francesa",         "Tortilla con espinacas y queso",               310, R.drawable.tortilla_francesa),
        ComidaDisponible(8,  "Müsli con Leche",           "Müsli integral con leche desnatada",           260, R.drawable.musli_con_leche),

        // ALMUERZOS
        ComidaDisponible(9,  "Pollo con Arroz",           "Pechuga de pollo a la plancha con arroz",      520, R.drawable.pollo_con_arroz),
        ComidaDisponible(10, "Pasta con Atún",            "Pasta integral con atún y vegetales",          480, R.drawable.pasta_con_atun),
        ComidaDisponible(11, "Ensalada de Quinoa",        "Quinoa con vegetales y pollo",                 420, R.drawable.ensalada_de_quinoa),
        ComidaDisponible(12, "Salmón al Horno",           "Salmón con patata y brócoli",                  550, R.drawable.salmon_al_horno),
        ComidaDisponible(13, "Lentejas con Verduras",     "Lentejas estofadas con zanahorias",            430, R.drawable.lentejas_con_verduras),
        ComidaDisponible(14, "Hamburguesa Saludable",     "Hamburguesa de pavo con ensalada",             490, R.drawable.hamburguesa_saludable),
        ComidaDisponible(15, "Bowl de Arroz Integral",    "Arroz integral con garbanzos y aguacate",      460, R.drawable.bowl_de_arroz_integral),
        ComidaDisponible(16, "Pescado con Patatas",       "Merluza al vapor con patatas hervidas",        410, R.drawable.pescado_con_patatas),

        // MERIENDAS
        ComidaDisponible(17, "Manzana con Almendras",     "Manzana y 20g de almendras crudas",            180, R.drawable.manzana_con_almendras),
        ComidaDisponible(18, "Batido Verde",              "Batido de espinacas, plátano y proteína",      200, R.drawable.batido_verde),
        ComidaDisponible(19, "Queso Cottage",             "Queso cottage con frutos del bosque",          150, R.drawable.queso_cottage),
        ComidaDisponible(20, "Tostada con Mantequilla",  "Tostada integral con mantequilla de almendra",  220, R.drawable.tostada_con_mantequilla),
        ComidaDisponible(21, "Fruta Variada",             "Mix de frutas de temporada",                   120, R.drawable.fruta_variada),
        ComidaDisponible(22, "Barrita Proteica Casera",   "Barrita de avena, dátiles y proteína",         240, R.drawable.barrita_proteica_casera),
        ComidaDisponible(23, "Hummus con Zanahoria",      "Hummus casero con bastones de zanahoria",      160, R.drawable.hummus_con_zanahoria),
        ComidaDisponible(24, "Pudin de Chía",             "Pudin de chía con leche vegetal y frutas",     190, R.drawable.pudin_de_chia),

        // CENAS
        ComidaDisponible(25, "Crema de Verduras",         "Crema de calabacín con quesito",               280, R.drawable.crema_de_verduras),
        ComidaDisponible(26, "Pechuga a la Plancha",      "Pechuga con ensalada verde y tomate",          350, R.drawable.pechuga_a_la_plancha),
        ComidaDisponible(27, "Revuelto de Vegetales",     "Revuelto de huevos con espárragos",            300, R.drawable.revuelto_de_vegetales),
        ComidaDisponible(28, "Merluza al Vapor",          "Merluza al vapor con judías verdes",           290, R.drawable.merluza_al_vapor),
        ComidaDisponible(29, "Ensalada Completa",         "Ensalada de lechuga, atún y huevo duro",       320, R.drawable.merluza_al_vapor),
        ComidaDisponible(30, "Wok de Pollo y Verduras",  "Pollo salteado con brócoli y pimiento",        380, R.drawable.work_de_pollo_y_verduras),
        ComidaDisponible(31, "Tortilla de Claras",        "Tortilla de claras con champiñones",           220, R.drawable.tortilla_de_claras),
        ComidaDisponible(32, "Sopa de Pollo",             "Caldo casero de pollo con fideos",             260, R.drawable.sopa_de_pollo)
    )
}

data class ComidaDisponible(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val caloriasBase: Int,
    val imagenRes: Int
)
