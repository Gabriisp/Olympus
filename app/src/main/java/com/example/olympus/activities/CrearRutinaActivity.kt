package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CrearRutinaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var etNombreRutina: EditText
    private lateinit var btnAgregarEjercicio: Button
    private lateinit var btnGuardarRutina: Button
    private lateinit var recyclerEjercicios: RecyclerView
    private lateinit var sessionManager: SessionManager
    private lateinit var cloudEjerciciosAdapter: CloudRutinaEjerciciosAdapter
    private var cloudRoutineId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_rutina)

        sessionManager = SessionManager(this)

        etNombreRutina = findViewById(R.id.etNombreRutina)
        btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio)
        btnGuardarRutina = findViewById(R.id.btnGuardarRutina)
        recyclerEjercicios = findViewById(R.id.recyclerEjercicios)

        recyclerEjercicios.layoutManager = LinearLayoutManager(this)
        cloudEjerciciosAdapter = CloudRutinaEjerciciosAdapter(
            ejercicios = emptyList(),
            firebaseRepository = firebaseRepository,
            routineId = "",
            onAgregarSerie = { },
            onEliminarEjercicio = { },
            onReordenar = { _, _ -> },
            onReemplazar = { }
        )
        recyclerEjercicios.adapter = cloudEjerciciosAdapter

        cloudRoutineId = intent.getStringExtra("CLOUD_ROUTINE_ID").orEmpty()

        if (cloudRoutineId.isNotBlank()) {
            loadCloudRoutine()
            loadCloudEjercicios()
        }

        btnAgregarEjercicio.setOnClickListener {
            if (cloudRoutineId.isBlank()) {
                guardarRutina(openExerciseSelectorAfterCreate = true)
            } else {
                openExerciseSelector()
            }
        }

        btnGuardarRutina.setOnClickListener {
            guardarRutina(openExerciseSelectorAfterCreate = false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (cloudRoutineId.isNotBlank()) {
            loadCloudEjercicios()
        }
    }

    private fun guardarRutina(openExerciseSelectorAfterCreate: Boolean) {
        val nombre = etNombreRutina.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para la rutina", Toast.LENGTH_SHORT).show()
            return
        }

        val userUid = sessionManager.getUserUid().orEmpty()
        if (userUid.isBlank()) {
            Toast.makeText(this, "Necesitas iniciar sesión para gestionar rutinas", Toast.LENGTH_LONG).show()
            return
        }

        btnGuardarRutina.isEnabled = false

        if (cloudRoutineId.isBlank()) {
            firebaseRepository.createRoutine(
                nombre = nombre,
                userUid = userUid,
                trainerUid = null,
                createdByRole = "Usuario"
            ) { result ->
                runOnUiThread {
                    btnGuardarRutina.isEnabled = true
                    result.onSuccess { routineId ->
                        cloudRoutineId = routineId
                        Toast.makeText(this, "Rutina creada", Toast.LENGTH_SHORT).show()
                        loadCloudRoutine()
                        loadCloudEjercicios()
                        if (openExerciseSelectorAfterCreate) {
                            openExerciseSelector()
                        } else {
                            finish()
                        }
                    }.onFailure { error ->
                        Toast.makeText(
                            this,
                            error.message ?: "No se pudo crear la rutina",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            firebaseRepository.updateRoutineName(cloudRoutineId, nombre) { result ->
                runOnUiThread {
                    btnGuardarRutina.isEnabled = true
                    result.onSuccess {
                        Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show()
                        if (!openExerciseSelectorAfterCreate) {
                            finish()
                        }
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

    private fun loadCloudRoutine() {
        if (cloudRoutineId.isBlank()) return

        firebaseRepository.getRoutineById(cloudRoutineId) { result ->
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

    private fun loadCloudEjercicios() {
        if (cloudRoutineId.isBlank()) return

        firebaseRepository.getExercisesOfRoutine(cloudRoutineId) { result ->
            runOnUiThread {
                result.onSuccess { ejercicios ->
                    cloudEjerciciosAdapter = CloudRutinaEjerciciosAdapter(
                        ejercicios = ejercicios,
                        firebaseRepository = firebaseRepository,
                        routineId = cloudRoutineId,
                        onAgregarSerie = ::onAgregarCloudSerie,
                        onEliminarEjercicio = ::onEliminarCloudEjercicio,
                        onReordenar = ::onReordenarCloudEjercicio,
                        onReemplazar = ::onReemplazarCloudEjercicio
                    )
                    recyclerEjercicios.adapter = cloudEjerciciosAdapter
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

    private fun onAgregarCloudSerie(routineExerciseId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_serie, null)
        val etPeso = dialogView.findViewById<EditText>(R.id.etPeso)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)

        AlertDialog.Builder(this)
            .setTitle("Agregar Serie")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val peso = etPeso.text.toString().toFloatOrNull()
                val reps = etReps.text.toString().toIntOrNull()

                firebaseRepository.addSetToExercise(cloudRoutineId, routineExerciseId, peso, reps) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            loadCloudEjercicios()
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

    private fun onEliminarCloudEjercicio(routineExerciseId: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ejercicio")
            .setMessage("¿Estás seguro de que quieres eliminar este ejercicio de la rutina?")
            .setPositiveButton("Eliminar") { _, _ ->
                firebaseRepository.deleteExerciseFromRoutine(cloudRoutineId, routineExerciseId) { result ->
                    runOnUiThread {
                        result.onSuccess {
                            loadCloudEjercicios()
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

    private fun onReordenarCloudEjercicio(routineExerciseId: String, moverArriba: Boolean) {
        firebaseRepository.reorderExerciseInRoutine(cloudRoutineId, routineExerciseId, moverArriba) { result ->
            runOnUiThread {
                result.onSuccess {
                    loadCloudEjercicios()
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

    private fun onReemplazarCloudEjercicio(routineExerciseId: String) {
        val intent = Intent(this, ReemplazarEjercicioActivity::class.java)
        intent.putExtra("CLOUD_ROUTINE_EJERCICIO_ID", routineExerciseId)
        intent.putExtra("CLOUD_ROUTINE_ID", cloudRoutineId)
        startActivity(intent)
    }

    private fun openExerciseSelector() {
        if (cloudRoutineId.isBlank()) return
        val intent = Intent(this, SeleccionarEjercicioActivity::class.java)
        intent.putExtra("CLOUD_ROUTINE_ID", cloudRoutineId)
        startActivity(intent)
    }
}
