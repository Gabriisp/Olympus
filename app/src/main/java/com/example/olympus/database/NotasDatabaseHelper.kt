package com.example.olympus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotasDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "NotasDB.db", null, 1) {
    
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE notas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                contenido TEXT,
                fecha TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS notas")
        onCreate(db)
    }

    fun crearNota(titulo: String, contenido: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", titulo)
            put("contenido", contenido)
            put("fecha", System.currentTimeMillis().toString())
        }
        return db.insert("notas", null, values).also { db.close() }
    }

    fun getAllNotas(): List<Nota> {
        val notas = mutableListOf<Nota>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM notas ORDER BY id DESC", null)
        
        if (cursor.moveToFirst()) {
            do {
                notas.add(Nota(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notas
    }

    fun eliminarNota(id: Int): Int {
        val db = writableDatabase
        return db.delete("notas", "id = ?", arrayOf(id.toString())).also { db.close() }
    }
}

data class Nota(val id: Int, val titulo: String, val contenido: String, val fecha: String)
