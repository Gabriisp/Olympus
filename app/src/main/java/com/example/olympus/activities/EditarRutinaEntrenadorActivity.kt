package com.example.olympus

// Activity para editar una rutina existente creada por un entrenador
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
    private lateinit var llDiasSemana: LinearLayout
    private lateinit var cbLunes: CheckBox
    private lateinit var cbMartes: CheckBox
    private lateinit var cbMiercoles: CheckBox
    private lateinit var cbJueves: CheckBox
    private lateinit var cbViernes: CheckBox
    private lateinit var cbSabado: CheckBox
    private lateinit var cbDomingo: CheckBox
    private var routineId: String = ""
    private var userUid: String = ""
    private var userName: String = ""
    private lateinit var ejerciciosAdapter: CloudRutinaEjerciciosAdapter
    private var diaSemanaSeleccionado: String = ""
    private var diaSemanaOriginal: String = ""

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
        llDiasSemana = findViewById(R.id.llDiasSemana)
        cbLunes = findViewById(R.id.cbLunes)
        cbMartes = findViewById(R.id.cbMartes)
        cbMiercoles = findViewById(R.id.cbMiercoles)
        cbJueves = findViewById(R.id.cbJueves)
        cbViernes = findViewById(R.id.cbViernes)
        cbSabado = findViewById(R.id.cbSabado)
        cbDomingo = findViewById(R.id.cbDomingo)

        recyclerEjercicios.layoutManager = LinearLayoutManager(this)

        tvTitulo.text = "Rutina de $userName"

        val dayCheckBoxes = listOf(cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo)
        val dayValues = listOf("L", "M", "X", "J", "V", "S", "D")

        dayCheckBoxes.forEachIndexed { index, checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dayCheckBoxes.filterIndexed { i, _ -> i != index }.forEach { it.isChecked = false }
                    diaSemanaSeleccionado = dayValues[index]
                } else {
                    if (diaSemanaSeleccionado == dayValues[index]) {
                        diaSemanaSeleccionado = ""
                    }
                }
            }
        }

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
                val diaChanged = diaSemanaSeleccionado != diaSemanaOriginal
                if (diaChanged) {
                    firebaseRepository.updateRoutineNameAndDay(routineId, nombre, diaSemanaSeleccionado) { result ->
                        runOnUiThread {
                            result.onSuccess {
                                showToast("Rutina actualizada")
                            }.onFailure { error ->
                                showToast(error.message ?: "No se pudo actualizar la rutina", Toast.LENGTH_LONG)
                            }
                        }
                    }
                } else {
                    firebaseRepository.updateRoutineName(routineId, nombre) { result ->
                        runOnUiThread {
                            result.onSuccess {
                                showToast("Rutina actualizada")
                            }.onFailure { error ->
                                showToast(error.message ?: "No se pudo actualizar la rutina", Toast.LENGTH_LONG)
                            }
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

    // Carga los ejercicios de la rutina desde Firebase
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
                        onReemplazar = ::onReemplazarEjercicio,
                        onEditarSerie = ::onEditarSerie
                    )
                    recyclerEjercicios.adapter = ejerciciosAdapter
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudieron cargar los ejercicios", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // Carga los datos de la rutina desde Firebase
    private fun loadRoutine() {
        if (routineId.isBlank()) return

        firebaseRepository.getRoutineById(routineId) { result ->
            runOnUiThread {
                result.onSuccess { routine ->
                    etNombreRutina.setText(routine.nombre)
                    diaSemanaOriginal = routine.diaSemana
                    diaSemanaSeleccionado = routine.diaSemana
                    val checkBoxId = when (routine.diaSemana) {
                        "L" -> R.id.cbLunes
                        "M" -> R.id.cbMartes
                        "X" -> R.id.cbMiercoles
                        "J" -> R.id.cbJueves
                        "V" -> R.id.cbViernes
                        "S" -> R.id.cbSabado
                        "D" -> R.id.cbDomingo
                        else -> -1
                    }
                    if (checkBoxId != -1) {
                        findViewById<CheckBox>(checkBoxId)?.isChecked = true
                    }
                }.onFailure { error ->
                    showToast("No se pudo cargar la rutina", Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun onAgregarSerie(routineExerciseId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_serie, null)
        val etPeso = dialogView.findViewById<EditText>(R.id.etPeso)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)
        val btnCancelar = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnCancelarAgregarSerie)
        val btnAgregar = dialogView.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnConfirmarAgregarSerie)

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnAgregar.setOnClickListener {
            val peso = etPeso.text.toString().toFloatOrNull()
            val reps = etReps.text.toString().toIntOrNull()

            firebaseRepository.addSetToExercise(routineId, routineExerciseId, peso, reps) { result ->
                runOnUiThread {
                    result.onSuccess {
                        dialog.dismiss()
                        loadEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo agregar la serie", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun onEliminarEjercicio(routineExerciseId: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirmar_eliminar_ejercicio)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEliminar)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnEliminar)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            dialog.dismiss()
            firebaseRepository.deleteExerciseFromRoutine(routineId, routineExerciseId) { result ->
                runOnUiThread {
                    result.onSuccess {
                        loadEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo eliminar el ejercicio", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun onReordenarEjercicio(routineExerciseId: String, moverArriba: Boolean) {
        firebaseRepository.reorderExerciseInRoutine(routineId, routineExerciseId, moverArriba) { result ->
            runOnUiThread {
                result.onSuccess {
                    loadEjercicios()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo reordenar el ejercicio", Toast.LENGTH_LONG)
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

    private fun onEditarSerie(routineId: String, routineExerciseId: String, set: CloudRoutineSet) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_editar_serie)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val etPeso = dialog.findViewById<EditText>(R.id.etPeso)
        val etReps = dialog.findViewById<EditText>(R.id.etReps)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarSerie)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardarEditarSerie)

        etPeso.setText(set.peso.toString())
        etReps.setText(set.repeticiones.toString())

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val peso = etPeso.text.toString().toFloatOrNull()
            val reps = etReps.text.toString().toIntOrNull()

            firebaseRepository.updateSet(routineId, routineExerciseId, set.id, peso, reps) { result ->
                runOnUiThread {
                    result.onSuccess {
                        dialog.dismiss()
                        loadEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo actualizar la serie", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }
}
