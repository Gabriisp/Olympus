package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditarRutinaEntrenadorActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvTitulo: TextView
    private lateinit var etNombreRutina: EditText
    private lateinit var btnAgregarEjercicio: Button
    private lateinit var btnActualizar: Button
    private lateinit var recyclerEjercicios: RecyclerView
    private var routineId: String = ""
    private var userUid: String = ""
    private var userName: String = ""
    private lateinit var ejerciciosAdapter: CloudRutinaEjerciciosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_rutina_entrenador)

        routineId = intent.getStringExtra("ROUTINE_ID").orEmpty()
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvTitulo = findViewById(R.id.tvTituloEditarRutina)
        etNombreRutina = findViewById(R.id.etNombreRutinaEditar)
        btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicioEntrenador)
        btnActualizar = findViewById(R.id.btnActualizarRutina)
        recyclerEjercicios = findViewById(R.id.recyclerEjerciciosEntrenador)

        recyclerEjercicios.layoutManager = LinearLayoutManager(this)

        tvTitulo.text = "Rutina de $userName"

        loadRoutine()
        loadEjercicios()

        btnAgregarEjercicio.setOnClickListener {
            val intent = Intent(this, SeleccionarEjercicioActivity::class.java)
            intent.putExtra("CLOUD_ROUTINE_ID", routineId)
            startActivity(intent)
        }

        btnActualizar.setOnClickListener {
            val nombre = etNombreRutina.text.toString().trim()
            if (nombre.isNotEmpty()) {
                firebaseRepository.updateRoutineName(routineId, nombre) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show()
                        }.onFailure { error ->
                            Toast.makeText(
                                this,
                                error.message ?: "No se pudo actualizar la rutina",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadEjercicios()
    }

    private fun loadEjercicios() {
        if (routineId.isBlank()) return

        firebaseRepository.getExercisesOfRoutine(routineId) { result ->
            runOnUiThread {
                result.onSuccess { ejercicios ->
                    ejerciciosAdapter = CloudRutinaEjerciciosAdapter(
                        ejercicios = ejercicios,
                        firebaseRepository = firebaseRepository,
                        routineId = routineId,
                        onAgregarSerie = ::onAgregarSerie,
                        onEliminarEjercicio = ::onEliminarEjercicio,
                        onReordenar = ::onReordenarEjercicio,
                        onReemplazar = ::onReemplazarEjercicio
                    )
                    recyclerEjercicios.adapter = ejerciciosAdapter
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar los ejercicios",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadRoutine() {
        if (routineId.isBlank()) return

        firebaseRepository.getRoutineById(routineId) { result ->
            runOnUiThread {
                result.onSuccess { routine ->
                    etNombreRutina.setText(routine.nombre)
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudo cargar la rutina",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun onAgregarSerie(routineExerciseId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_serie, null)
        val etPeso = dialogView.findViewById<EditText>(R.id.etPeso)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)

        AlertDialog.Builder(this)
            .setTitle("Agregar Serie")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val peso = etPeso.text.toString().toFloatOrNull()
                val reps = etReps.text.toString().toIntOrNull()

                firebaseRepository.addSetToExercise(routineId, routineExerciseId, peso, reps) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            loadEjercicios()
                        }.onFailure { error ->
                            Toast.makeText(
                                this,
                                error.message ?: "No se pudo agregar la serie",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun onEliminarEjercicio(routineExerciseId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Seguro?")
            .setPositiveButton("Eliminar") { _, _ ->
                firebaseRepository.deleteExerciseFromRoutine(routineId, routineExerciseId) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            loadEjercicios()
                        }.onFailure { error ->
                            Toast.makeText(
                                this,
                                error.message ?: "No se pudo eliminar el ejercicio",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun onReordenarEjercicio(routineExerciseId: String, moverArriba: Boolean) {
        firebaseRepository.reorderExerciseInRoutine(routineId, routineExerciseId, moverArriba) { result ->
            runOnUiThread {
                result.onSuccess {
                    loadEjercicios()
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudo reordenar el ejercicio",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun onReemplazarEjercicio(routineExerciseId: String) {
        val intent = Intent(this, ReemplazarEjercicioActivity::class.java)
        intent.putExtra("CLOUD_ROUTINE_EJERCICIO_ID", routineExerciseId)
        intent.putExtra("CLOUD_ROUTINE_ID", routineId)
        startActivity(intent)
    }
}
