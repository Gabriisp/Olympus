package com.example.olympus.presentation.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.olympus.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // TextViews del menú
        val tvRutinas: TextView = findViewById(R.id.tvRutinas)
        val tvEjercicios: TextView = findViewById(R.id.tvEjercicios)
        val tvGyms: TextView = findViewById(R.id.tvGyms)
        val tvNotas: TextView = findViewById(R.id.tvNotas)
        val tvStats: TextView = findViewById(R.id.tvStats)
        val btnTienda: Button = findViewById(R.id.btnTienda)

        // Configurar listeners para el menú, ahora mismo solo muestran mensajes
        tvRutinas.setOnClickListener {
            mostrarMensaje("Abriendo Rutinas")
        }

        tvEjercicios.setOnClickListener {
            mostrarMensaje("Abriendo Ejercicios")
        }

        tvGyms.setOnClickListener {
            mostrarMensaje("Abriendo Gimnasios")
        }

        tvNotas.setOnClickListener {
            mostrarMensaje("Abriendo Notas")
        }

        tvStats.setOnClickListener {
            mostrarMensaje("Abriendo Estadísticas")
        }

        btnTienda.setOnClickListener {
            // Ahora mismo no hace nada el botón de la tienda
        }

    }
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}