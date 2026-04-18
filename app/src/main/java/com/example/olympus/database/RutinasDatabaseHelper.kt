package com.example.olympus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RutinasDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RutinasDB.db"
        private const val DATABASE_VERSION = 1
        
        // Tabla rutinas
        private const val TABLE_RUTINAS = "rutinas"
        private const val COL_RUTINA_ID = "id"
        private const val COL_RUTINA_NOMBRE = "nombre"
        private const val COL_RUTINA_USER_ID = "user_id"
        
        // Tabla ejercicios en rutina
        private const val TABLE_RUTINA_EJERCICIOS = "rutina_ejercicios"
        private const val COL_RE_ID = "id"
        private const val COL_RE_RUTINA_ID = "rutina_id"
        private const val COL_RE_EJERCICIO_ID = "ejercicio_id"
        private const val COL_RE_EJERCICIO_NOMBRE = "ejercicio_nombre"
        private const val COL_RE_ORDEN = "orden"
        
        // Tabla series
        private const val TABLE_SERIES = "series"
        private const val COL_SERIE_ID = "id"
        private const val COL_SERIE_RE_ID = "rutina_ejercicio_id"
        private const val COL_SERIE_PESO = "peso"
        private const val COL_SERIE_REPS = "repeticiones"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear tabla rutinas
        val createRutinasTable = """
            CREATE TABLE $TABLE_RUTINAS (
                $COL_RUTINA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RUTINA_NOMBRE TEXT NOT NULL,
                $COL_RUTINA_USER_ID INTEGER NOT NULL
            )
        """.trimIndent()
        
        // Crear tabla ejercicios en rutina
        val createRutinaEjerciciosTable = """
            CREATE TABLE $TABLE_RUTINA_EJERCICIOS (
                $COL_RE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RE_RUTINA_ID INTEGER NOT NULL,
                $COL_RE_EJERCICIO_ID INTEGER NOT NULL,
                $COL_RE_EJERCICIO_NOMBRE TEXT NOT NULL,
                $COL_RE_ORDEN INTEGER NOT NULL,
                FOREIGN KEY($COL_RE_RUTINA_ID) REFERENCES $TABLE_RUTINAS($COL_RUTINA_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        
        // Crear tabla series
        val createSeriesTable = """
            CREATE TABLE $TABLE_SERIES (
                $COL_SERIE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SERIE_RE_ID INTEGER NOT NULL,
                $COL_SERIE_PESO REAL,
                $COL_SERIE_REPS INTEGER,
                FOREIGN KEY($COL_SERIE_RE_ID) REFERENCES $TABLE_RUTINA_EJERCICIOS($COL_RE_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        
        db?.execSQL(createRutinasTable)
        db?.execSQL(createRutinaEjerciciosTable)
        db?.execSQL(createSeriesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_SERIES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RUTINA_EJERCICIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RUTINAS")
        onCreate(db)
    }

    // Crear rutina
    fun crearRutina(nombre: String, userId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_NOMBRE, nombre)
            put(COL_RUTINA_USER_ID, userId)
        }
        val id = db.insert(TABLE_RUTINAS, null, values)
        db.close()
        return id
    }

    // Obtener todas las rutinas
    fun getAllRutinas(): List<Rutina> {
        val rutinas = mutableListOf<Rutina>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_RUTINAS, null, null, null, null, null, null)
        
        if (cursor.moveToFirst()) {
            do {
                val rutina = Rutina(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_RUTINA_NOMBRE)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_USER_ID))
                )
                rutinas.add(rutina)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return rutinas
    }

    // Obtener rutinas por usuario
    fun getRutinasPorUsuario(userId: Int): List<Rutina> {
        val rutinas = mutableListOf<Rutina>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RUTINAS,
            null,
            "$COL_RUTINA_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )
        
        if (cursor.moveToFirst()) {
            do {
                val rutina = Rutina(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_RUTINA_NOMBRE)),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_USER_ID))
                )
                rutinas.add(rutina)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return rutinas
    }

    // Obtener rutina por ID
    fun getRutinaById(id: Int): Rutina? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RUTINAS,
            null,
            "$COL_RUTINA_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        
        var rutina: Rutina? = null
        if (cursor.moveToFirst()) {
            rutina = Rutina(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_RUTINA_NOMBRE)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RUTINA_USER_ID))
            )
        }
        cursor.close()
        db.close()
        return rutina
    }

    // Agregar ejercicio a rutina
    fun agregarEjercicioARutina(rutinaId: Int, ejercicioId: Int, ejercicioNombre: String): Long {
        val db = this.writableDatabase
        
        // Obtener el orden siguiente
        val cursor = db.rawQuery(
            "SELECT MAX($COL_RE_ORDEN) FROM $TABLE_RUTINA_EJERCICIOS WHERE $COL_RE_RUTINA_ID = ?",
            arrayOf(rutinaId.toString())
        )
        var orden = 0
        if (cursor.moveToFirst()) {
            orden = cursor.getInt(0) + 1
        }
        cursor.close()
        
        val values = ContentValues().apply {
            put(COL_RE_RUTINA_ID, rutinaId)
            put(COL_RE_EJERCICIO_ID, ejercicioId)
            put(COL_RE_EJERCICIO_NOMBRE, ejercicioNombre)
            put(COL_RE_ORDEN, orden)
        }
        
        val id = db.insert(TABLE_RUTINA_EJERCICIOS, null, values)
        db.close()
        return id
    }

    // Obtener ejercicios de una rutina
    fun getEjerciciosDeRutina(rutinaId: Int): List<RutinaEjercicio> {
        val ejercicios = mutableListOf<RutinaEjercicio>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RUTINA_EJERCICIOS,
            null,
            "$COL_RE_RUTINA_ID = ?",
            arrayOf(rutinaId.toString()),
            null, null,
            "$COL_RE_ORDEN ASC"
        )
        
        if (cursor.moveToFirst()) {
            do {
                val ejercicio = RutinaEjercicio(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RE_ID)),
                    rutinaId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RE_RUTINA_ID)),
                    ejercicioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RE_EJERCICIO_ID)),
                    ejercicioNombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_RE_EJERCICIO_NOMBRE)),
                    orden = cursor.getInt(cursor.getColumnIndexOrThrow(COL_RE_ORDEN))
                )
                ejercicios.add(ejercicio)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ejercicios
    }

    // Agregar serie
    fun agregarSerie(rutinaEjercicioId: Int, peso: Float?, reps: Int?): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_SERIE_RE_ID, rutinaEjercicioId)
            put(COL_SERIE_PESO, peso)
            put(COL_SERIE_REPS, reps)
        }
        val id = db.insert(TABLE_SERIES, null, values)
        db.close()
        return id
    }

    // Obtener series de un ejercicio en rutina
    fun getSeriesDeEjercicio(rutinaEjercicioId: Int): List<Serie> {
        val series = mutableListOf<Serie>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_SERIES,
            null,
            "$COL_SERIE_RE_ID = ?",
            arrayOf(rutinaEjercicioId.toString()),
            null, null,
            "$COL_SERIE_ID ASC"
        )
        
        if (cursor.moveToFirst()) {
            do {
                val serie = Serie(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERIE_ID)),
                    rutinaEjercicioId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERIE_RE_ID)),
                    peso = cursor.getFloat(cursor.getColumnIndexOrThrow(COL_SERIE_PESO)),
                    repeticiones = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERIE_REPS))
                )
                series.add(serie)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return series
    }

    fun getStatsPorEjercicioDeRutina(rutinaId: Int): List<ExerciseProgressStat> {
        return getEjerciciosDeRutina(rutinaId).map { ejercicio ->
            val series = getSeriesDeEjercicio(ejercicio.id)
            buildExerciseProgressStat(ejercicio.id, ejercicio.ejercicioNombre, series)
        }
    }

    fun getResumenRutina(rutinaId: Int): RoutineProgressSummary {
        val stats = getStatsPorEjercicioDeRutina(rutinaId)
        return RoutineProgressSummary(
            totalEjercicios = stats.size,
            totalSeries = stats.sumOf { it.totalSeries },
            totalVolumen = stats.sumOf { it.totalVolumen.toDouble() }.toFloat(),
            ejercicioDestacado = stats.maxByOrNull { it.totalVolumen }?.ejercicioNombre ?: "Sin datos",
            mejorTendencia = stats.maxByOrNull { it.tendenciaPeso }?.ejercicioNombre ?: "Sin datos"
        )
    }

    fun getStatsGeneralesUsuario(userId: Int): List<ExerciseProgressStat> {
        val groupedSeries = linkedMapOf<String, MutableList<Serie>>()
        val groupedIds = linkedMapOf<String, Int>()

        getRutinasPorUsuario(userId).forEach { rutina ->
            getEjerciciosDeRutina(rutina.id).forEach { ejercicio ->
                groupedIds.putIfAbsent(ejercicio.ejercicioNombre, ejercicio.id)
                groupedSeries.getOrPut(ejercicio.ejercicioNombre) { mutableListOf() }
                    .addAll(getSeriesDeEjercicio(ejercicio.id))
            }
        }

        return groupedSeries.map { (nombre, series) ->
            buildExerciseProgressStat(groupedIds[nombre] ?: -1, nombre, series)
        }.sortedByDescending { it.totalVolumen }
    }

    fun getResumenGeneralUsuario(userId: Int): GeneralProgressSummary {
        val rutinas = getRutinasPorUsuario(userId)
        val stats = getStatsGeneralesUsuario(userId)
        return GeneralProgressSummary(
            totalRutinas = rutinas.size,
            totalEjercicios = stats.size,
            totalSeries = stats.sumOf { it.totalSeries },
            totalVolumen = stats.sumOf { it.totalVolumen.toDouble() }.toFloat(),
            ejercicioMasFuerte = stats.maxByOrNull { it.maxPeso }?.ejercicioNombre ?: "Sin datos",
            ejercicioConMejorTendencia = stats.maxByOrNull { it.tendenciaPeso }?.ejercicioNombre
                ?: "Sin datos"
        )
    }

    private fun buildExerciseProgressStat(
        rutinaEjercicioId: Int,
        ejercicioNombre: String,
        series: List<Serie>
    ): ExerciseProgressStat {
        val totalSeries = series.size
        val maxPeso = series.maxOfOrNull { it.peso } ?: 0f
        val promedioPeso = if (totalSeries > 0) series.map { it.peso }.average().toFloat() else 0f
        val maxRepeticiones = series.maxOfOrNull { it.repeticiones } ?: 0
        val totalRepeticiones = series.sumOf { it.repeticiones }
        val totalVolumen = series.sumOf { (it.peso * it.repeticiones).toDouble() }.toFloat()
        val tendenciaPeso = if (totalSeries >= 2) {
            series.last().peso - series.first().peso
        } else {
            0f
        }

        return ExerciseProgressStat(
            rutinaEjercicioId = rutinaEjercicioId,
            ejercicioNombre = ejercicioNombre,
            totalSeries = totalSeries,
            promedioPeso = promedioPeso,
            maxPeso = maxPeso,
            maxRepeticiones = maxRepeticiones,
            totalRepeticiones = totalRepeticiones,
            totalVolumen = totalVolumen,
            tendenciaPeso = tendenciaPeso,
            pesosPorSerie = series.map { it.peso },
            repsPorSerie = series.map { it.repeticiones }
        )
    }

    // Eliminar rutina
    fun eliminarRutina(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_RUTINAS, "$COL_RUTINA_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Eliminar ejercicio de rutina
    fun eliminarEjercicioDeRutina(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_RUTINA_EJERCICIOS, "$COL_RE_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Eliminar serie
    fun eliminarSerie(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_SERIES, "$COL_SERIE_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
    // Actualizar nombre de rutina
    fun actualizarNombreRutina(rutinaId: Int, nuevoNombre: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_NOMBRE, nuevoNombre)
        }
        db.update(TABLE_RUTINAS, values, "$COL_RUTINA_ID = ?", arrayOf(rutinaId.toString()))
        db.close()
    }

    fun reordenarEjercicio(rutinaEjercicioId: Int, moverArriba: Boolean) {
        val db = this.writableDatabase

        val currentCursor = db.query(
            TABLE_RUTINA_EJERCICIOS,
            arrayOf(COL_RE_RUTINA_ID, COL_RE_ORDEN),
            "$COL_RE_ID = ?",
            arrayOf(rutinaEjercicioId.toString()),
            null, null, null
        )

        if (!currentCursor.moveToFirst()) {
            currentCursor.close()
            db.close()
            return
        }

        val rutinaId = currentCursor.getInt(currentCursor.getColumnIndexOrThrow(COL_RE_RUTINA_ID))
        val ordenActual = currentCursor.getInt(currentCursor.getColumnIndexOrThrow(COL_RE_ORDEN))
        currentCursor.close()

        val ordenObjetivo = if (moverArriba) ordenActual - 1 else ordenActual + 1
        if (ordenObjetivo < 0) {
            db.close()
            return
        }

        val targetCursor = db.query(
            TABLE_RUTINA_EJERCICIOS,
            arrayOf(COL_RE_ID),
            "$COL_RE_RUTINA_ID = ? AND $COL_RE_ORDEN = ?",
            arrayOf(rutinaId.toString(), ordenObjetivo.toString()),
            null, null, null
        )

        if (!targetCursor.moveToFirst()) {
            targetCursor.close()
            db.close()
            return
        }

        val targetId = targetCursor.getInt(targetCursor.getColumnIndexOrThrow(COL_RE_ID))
        targetCursor.close()

        db.beginTransaction()
        try {
            db.update(
                TABLE_RUTINA_EJERCICIOS,
                ContentValues().apply { put(COL_RE_ORDEN, ordenObjetivo) },
                "$COL_RE_ID = ?",
                arrayOf(rutinaEjercicioId.toString())
            )
            db.update(
                TABLE_RUTINA_EJERCICIOS,
                ContentValues().apply { put(COL_RE_ORDEN, ordenActual) },
                "$COL_RE_ID = ?",
                arrayOf(targetId.toString())
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun reemplazarEjercicio(rutinaEjercicioId: Int, ejercicioId: Int, ejercicioNombre: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RE_EJERCICIO_ID, ejercicioId)
            put(COL_RE_EJERCICIO_NOMBRE, ejercicioNombre)
        }
        db.update(TABLE_RUTINA_EJERCICIOS, values, "$COL_RE_ID = ?", arrayOf(rutinaEjercicioId.toString()))
        db.close()
    }
}

// Data classes
data class Rutina(
    val id: Int,
    val nombre: String,
    val userId: Int
)

data class RutinaEjercicio(
    val id: Int,
    val rutinaId: Int,
    val ejercicioId: Int,
    val ejercicioNombre: String,
    val orden: Int
)

data class Serie(
    val id: Int,
    val rutinaEjercicioId: Int,
    val peso: Float,
    val repeticiones: Int
)

data class ExerciseProgressStat(
    val rutinaEjercicioId: Int,
    val ejercicioNombre: String,
    val totalSeries: Int,
    val promedioPeso: Float,
    val maxPeso: Float,
    val maxRepeticiones: Int,
    val totalRepeticiones: Int,
    val totalVolumen: Float,
    val tendenciaPeso: Float,
    val pesosPorSerie: List<Float>,
    val repsPorSerie: List<Int>
)

data class RoutineProgressSummary(
    val totalEjercicios: Int,
    val totalSeries: Int,
    val totalVolumen: Float,
    val ejercicioDestacado: String,
    val mejorTendencia: String
)

data class GeneralProgressSummary(
    val totalRutinas: Int,
    val totalEjercicios: Int,
    val totalSeries: Int,
    val totalVolumen: Float,
    val ejercicioMasFuerte: String,
    val ejercicioConMejorTendencia: String
)

// Clase para los ejercicios disponibles
data class Ejercicio(
    val id: Int,
    val nombre: String,
    val imagen: Int // Resource ID del drawable
)
