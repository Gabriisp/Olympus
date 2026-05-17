package com.example.olympus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NutricionistaDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NutricionDB.db"
        private const val DATABASE_VERSION = 1

        // Tabla planes nutricionales
        const val TABLE_PLANES = "planes_nutricionales"
        const val COL_PLAN_ID = "id"
        const val COL_PLAN_NOMBRE = "nombre"
        const val COL_PLAN_DESCRIPCION = "descripcion"
        const val COL_PLAN_USER_ID = "user_id"

        // Tabla comidas del plan (exactamente 4: Desayuno, Almuerzo, Merienda, Cena)
        const val TABLE_COMIDAS = "comidas_plan"
        const val COL_COMIDA_ID = "id"
        const val COL_COMIDA_PLAN_ID = "plan_id"
        const val COL_COMIDA_TIPO = "tipo"          // Desayuno, Almuerzo, Merienda, Cena
        const val COL_COMIDA_NOMBRE = "nombre"
        const val COL_COMIDA_DESCRIPCION = "descripcion"
        const val COL_COMIDA_CALORIAS = "calorias"
        const val COL_COMIDA_IMAGEN_ID = "imagen_id" // ID de la comida base (de ComidaData)
        const val COL_COMIDA_ORDEN = "orden"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createPlanesTable = """
            CREATE TABLE $TABLE_PLANES (
                $COL_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLAN_NOMBRE TEXT NOT NULL,
                $COL_PLAN_DESCRIPCION TEXT,
                $COL_PLAN_USER_ID INTEGER NOT NULL
            )
        """.trimIndent()

        val createComidasTable = """
            CREATE TABLE $TABLE_COMIDAS (
                $COL_COMIDA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_COMIDA_PLAN_ID INTEGER NOT NULL,
                $COL_COMIDA_TIPO TEXT NOT NULL,
                $COL_COMIDA_NOMBRE TEXT NOT NULL,
                $COL_COMIDA_DESCRIPCION TEXT,
                $COL_COMIDA_CALORIAS INTEGER,
                $COL_COMIDA_IMAGEN_ID INTEGER NOT NULL DEFAULT 0,
                $COL_COMIDA_ORDEN INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COL_COMIDA_PLAN_ID) REFERENCES $TABLE_PLANES($COL_PLAN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db?.execSQL(createPlanesTable)
        db?.execSQL(createComidasTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_COMIDAS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLANES")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        db?.setForeignKeyConstraintsEnabled(true)
    }

    // ── Planes ──────────────────────────────────────────────────────────────

    fun crearPlan(nombre: String, descripcion: String, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAN_NOMBRE, nombre)
            put(COL_PLAN_DESCRIPCION, descripcion)
            put(COL_PLAN_USER_ID, userId)
        }
        val id = db.insert(TABLE_PLANES, null, values)
        db.close()
        return id
    }

    fun getPlanesPorUsuario(userId: Int): List<PlanNutricional> {
        val planes = mutableListOf<PlanNutricional>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PLANES, null,
            "$COL_PLAN_USER_ID = ?", arrayOf(userId.toString()),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            do {
                planes.add(
                    PlanNutricional(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAN_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLAN_NOMBRE)),
                        descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLAN_DESCRIPCION)) ?: "",
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAN_USER_ID))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return planes
    }

    fun getPlanById(planId: Int): PlanNutricional? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PLANES, null,
            "$COL_PLAN_ID = ?", arrayOf(planId.toString()),
            null, null, null
        )
        var plan: PlanNutricional? = null
        if (cursor.moveToFirst()) {
            plan = PlanNutricional(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLAN_NOMBRE)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLAN_DESCRIPCION)) ?: "",
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PLAN_USER_ID))
            )
        }
        cursor.close()
        db.close()
        return plan
    }

    fun actualizarPlan(planId: Int, nombre: String, descripcion: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAN_NOMBRE, nombre)
            put(COL_PLAN_DESCRIPCION, descripcion)
        }
        db.update(TABLE_PLANES, values, "$COL_PLAN_ID = ?", arrayOf(planId.toString()))
        db.close()
    }

    fun eliminarPlan(planId: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_PLANES, "$COL_PLAN_ID = ?", arrayOf(planId.toString()))
        db.close()
        return result
    }

    // ── Comidas ─────────────────────────────────────────────────────────────

    fun agregarComida(
        planId: Int,
        tipo: String,
        nombre: String,
        descripcion: String,
        calorias: Int,
        imagenId: Int,
        orden: Int
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COMIDA_PLAN_ID, planId)
            put(COL_COMIDA_TIPO, tipo)
            put(COL_COMIDA_NOMBRE, nombre)
            put(COL_COMIDA_DESCRIPCION, descripcion)
            put(COL_COMIDA_CALORIAS, calorias)
            put(COL_COMIDA_IMAGEN_ID, imagenId)
            put(COL_COMIDA_ORDEN, orden)
        }
        val id = db.insert(TABLE_COMIDAS, null, values)
        db.close()
        return id
    }

    fun getComdidaDePlan(planId: Int): List<ComidaPlan> {
        val comidas = mutableListOf<ComidaPlan>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COMIDAS, null,
            "$COL_COMIDA_PLAN_ID = ?", arrayOf(planId.toString()),
            null, null, "$COL_COMIDA_ORDEN ASC"
        )
        if (cursor.moveToFirst()) {
            do {
                comidas.add(
                    ComidaPlan(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMIDA_ID)),
                        planId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMIDA_PLAN_ID)),
                        tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMIDA_TIPO)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMIDA_NOMBRE)),
                        descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMIDA_DESCRIPCION)) ?: "",
                        calorias = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMIDA_CALORIAS)),
                        imagenId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMIDA_IMAGEN_ID)),
                        orden = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMIDA_ORDEN))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return comidas
    }

    fun actualizarComida(
        comidaId: Int,
        nombre: String,
        descripcion: String,
        calorias: Int,
        imagenId: Int
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COMIDA_NOMBRE, nombre)
            put(COL_COMIDA_DESCRIPCION, descripcion)
            put(COL_COMIDA_CALORIAS, calorias)
            put(COL_COMIDA_IMAGEN_ID, imagenId)
        }
        db.update(TABLE_COMIDAS, values, "$COL_COMIDA_ID = ?", arrayOf(comidaId.toString()))
        db.close()
    }

    fun eliminarComida(comidaId: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_COMIDAS, "$COL_COMIDA_ID = ?", arrayOf(comidaId.toString()))
        db.close()
        return result
    }

    fun tieneComidaTipo(planId: Int, tipo: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COMIDAS, arrayOf(COL_COMIDA_ID),
            "$COL_COMIDA_PLAN_ID = ? AND $COL_COMIDA_TIPO = ?",
            arrayOf(planId.toString(), tipo),
            null, null, null
        )
        val existe = cursor.count > 0
        cursor.close()
        db.close()
        return existe
    }
}

// ── Data classes ────────────────────────────────────────────────────────────

data class PlanNutricional(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val userId: Int
)

data class ComidaPlan(
    val id: Int,
    val planId: Int,
    val tipo: String,       // Desayuno, Almuerzo, Merienda, Cena
    val nombre: String,
    val descripcion: String,
    val calorias: Int,
    val imagenId: Int,      // índice en ComidaData
    val orden: Int
)
