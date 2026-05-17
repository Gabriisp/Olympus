package com.example.olympus

// Activity para seleccionar y agregar una comida a un plan nutricional
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SeleccionarComidaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var recyclerComidas: RecyclerView
    private lateinit var tvTituloSeleccion: TextView
    private var planId: String = ""
    private var tipoComida: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_comida)

        planId = intent.getStringExtra("PLAN_ID").orEmpty()
        tipoComida = intent.getStringExtra("TIPO_COMIDA") ?: "Desayuno"

        tvTituloSeleccion = findViewById(R.id.tvTituloSeleccionComida)
        recyclerComidas = findViewById(R.id.recyclerComidasDisponibles)

        tvTituloSeleccion.text = "Selecciona $tipoComida"

        recyclerComidas.layoutManager = GridLayoutManager(this, 2)

        val todasLasComidas = ComidaData.getComidasDisponibles()
        val adapter = ComidasDisponiblesAdapter(todasLasComidas) { comidaDisponible ->
            val orden = when (tipoComida) {
                "Desayuno" -> 0
                "Almuerzo" -> 1
                "Merienda" -> 2
                "Cena" -> 3
                else -> 0
            }

            firebaseRepository.addMeal(
                planId = planId,
                tipo = tipoComida,
                nombre = comidaDisponible.nombre,
                descripcion = comidaDisponible.descripcion,
                calorias = comidaDisponible.caloriasBase,
                imagenId = comidaDisponible.id,
                orden = orden
            ) { result ->
                runOnUiThread {
                    result.onSuccess {
                        showToast("${comidaDisponible.nombre} añadido al $tipoComida")
                        finish()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo añadir la comida", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        recyclerComidas.adapter = adapter
    }
}
