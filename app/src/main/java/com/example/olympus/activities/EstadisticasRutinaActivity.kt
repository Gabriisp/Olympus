package com.example.olympus

// Activity para mostrar las estadisticas y metricas de una rutina
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EstadisticasRutinaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvNombreRutina: TextView
    private lateinit var tvResumenRutina: TextView
    private lateinit var tvMetricasRutina: TextView
    private lateinit var tvSinDatos: TextView
    private lateinit var recyclerStats: RecyclerView
    private lateinit var statsAdapter: ExerciseStatsAdapter
    private var routineId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas_rutina)

        routineId = intent.getStringExtra("ROUTINE_ID").orEmpty()

        tvNombreRutina = findViewById(R.id.tvNombreRutinaStats)
        tvResumenRutina = findViewById(R.id.tvResumenRutinaStats)
        tvMetricasRutina = findViewById(R.id.tvMetricasRutinaStats)
        tvSinDatos = findViewById(R.id.tvSinDatosRutinaStats)
        recyclerStats = findViewById(R.id.recyclerStatsRutina)

        recyclerStats.layoutManager = LinearLayoutManager(this)
        statsAdapter = ExerciseStatsAdapter(emptyList())
        recyclerStats.adapter = statsAdapter

        if (routineId.isBlank()) {
            tvNombreRutina.text = "Estadísticas: Rutina"
            showEmptyState("No se encontró la rutina seleccionada")
        } else {
            loadData()
        }
    }

    // Carga los datos de la rutina y sus estadisticas desde Firebase
    private fun loadData() {
        firebaseRepository.getRoutineById(routineId) { routineResult ->
            runOnUiThread {
                routineResult.onSuccess { routine ->
                    tvNombreRutina.text = "Estadísticas: ${routine.nombre}"

                    firebaseRepository.getRoutineSummary(routineId) { summaryResult ->
                        runOnUiThread {
                            summaryResult.onSuccess { resumen ->
                                tvResumenRutina.text =
                                    "Ejercicios analizados: ${resumen.totalEjercicios} · Series registradas: ${resumen.totalSeries}"
                                tvMetricasRutina.text =
                                    "Volumen total: ${formatDecimal(resumen.totalVolumen)} kg · Ejercicio destacado: ${resumen.ejercicioDestacado} · Mejor tendencia: ${resumen.mejorTendencia}"

                                firebaseRepository.getRoutineExerciseStats(routineId) { statsResult ->
                                    runOnUiThread {
                                        statsResult.onSuccess { stats ->
                                            val filteredStats = stats.filter { it.totalSeries > 0 }
                                            statsAdapter.updateStats(filteredStats)
                                            if (filteredStats.isEmpty()) {
                                                showEmptyState("Todavía no hay series registradas en esta rutina")
                                            } else {
                                                tvSinDatos.visibility = View.GONE
                                                recyclerStats.visibility = View.VISIBLE
                                            }
                                        }.onFailure { error ->
                                            showEmptyState(error.message ?: "No se pudieron cargar las estadísticas")
                                        }
                                    }
                                }
                            }.onFailure { error ->
                                showEmptyState(error.message ?: "No se pudo calcular el resumen de la rutina")
                            }
                        }
                    }
                }.onFailure { error ->
                    showEmptyState(error.message ?: "No se pudo cargar la rutina")
                }
            }
        }
    }

    private fun showEmptyState(message: String) {
        tvSinDatos.text = message
        tvSinDatos.visibility = View.VISIBLE
        recyclerStats.visibility = View.GONE
        statsAdapter.updateStats(emptyList())
    }

    private fun formatDecimal(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}
