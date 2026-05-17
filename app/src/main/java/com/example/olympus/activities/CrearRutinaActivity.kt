package com.example.olympus

// Activity para crear y gestionar rutinas de ejercicios del usuario
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class CrearRutinaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var etNombreRutina: EditText
    private lateinit var btnAgregarEjercicio: Button
    private lateinit var btnGuardarRutina: Button
    private lateinit var recyclerEjercicios: RecyclerView
    private lateinit var llDiasSemana: LinearLayout
    private lateinit var cbLunes: CheckBox
    private lateinit var cbMartes: CheckBox
    private lateinit var cbMiercoles: CheckBox
    private lateinit var cbJueves: CheckBox
    private lateinit var cbViernes: CheckBox
    private lateinit var cbSabado: CheckBox
    private lateinit var cbDomingo: CheckBox
    private lateinit var sessionManager: SessionManager
    private lateinit var cloudEjerciciosAdapter: CloudRutinaEjerciciosAdapter
    private var cloudRoutineId: String = ""
    private var diaSemanaSeleccionado: String = ""
    private var diaSemanaOriginal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_rutina)

        sessionManager = SessionManager(this)

        etNombreRutina = findViewById(R.id.etNombreRutina)
        btnAgregarEjercicio = findViewById(R.id.btnAgregarEjercicio)
        btnGuardarRutina = findViewById(R.id.btnGuardarRutina)
        recyclerEjercicios = findViewById(R.id.recyclerEjercicios)
        llDiasSemana = findViewById(R.id.llDiasSemana)
        cbLunes = findViewById(R.id.cbLunes)
        cbMartes = findViewById(R.id.cbMartes)
        cbMiercoles = findViewById(R.id.cbMiercoles)
        cbJueves = findViewById(R.id.cbJueves)
        cbViernes = findViewById(R.id.cbViernes)
        cbSabado = findViewById(R.id.cbSabado)
        cbDomingo = findViewById(R.id.cbDomingo)

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

        recyclerEjercicios.layoutManager = LinearLayoutManager(this)
        cloudEjerciciosAdapter = CloudRutinaEjerciciosAdapter(
            ejercicios = emptyList(),
            firebaseRepository = firebaseRepository,
            routineId = "",
            onAgregarSerie = { },
            onEliminarEjercicio = { },
            onReordenar = { _, _ -> },
            onReemplazar = { },
            onEditarSerie = { _, _, _ -> }
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

    // Crea o actualiza la rutina en Firebase
    private fun guardarRutina(openExerciseSelectorAfterCreate: Boolean) {
        val nombre = etNombreRutina.text.toString().trim()
        if (nombre.isEmpty()) {
            showToast("Ingresa un nombre para la rutina")
            return
        }

        val userUid = sessionManager.getUserUid().orEmpty()
        if (userUid.isBlank()) {
            showToast("Necesitas iniciar sesión para gestionar rutinas", Toast.LENGTH_LONG)
            return
        }

        btnGuardarRutina.isEnabled = false

        if (cloudRoutineId.isBlank()) {
            firebaseRepository.createRoutine(
                nombre = nombre,
                userUid = userUid,
                trainerUid = null,
                createdByRole = "Usuario",
                diaSemana = diaSemanaSeleccionado
            ) { result ->
                runOnUiThread {
                    btnGuardarRutina.isEnabled = true
                    result.onSuccess { routineId ->
                        cloudRoutineId = routineId
                        showToast("Rutina creada")
                        loadCloudRoutine()
                        loadCloudEjercicios()
                        if (openExerciseSelectorAfterCreate) {
                            openExerciseSelector()
                        } else {
                            finish()
                        }
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo crear la rutina", Toast.LENGTH_LONG)
                    }
                }
            }
        } else {
            val diaChanged = diaSemanaSeleccionado != diaSemanaOriginal
            if (diaChanged) {
                firebaseRepository.updateRoutineNameAndDay(cloudRoutineId, nombre, diaSemanaSeleccionado) { result ->
                    runOnUiThread {
                        btnGuardarRutina.isEnabled = true
                        result.onSuccess {
                            showToast("Rutina actualizada")
                            if (!openExerciseSelectorAfterCreate) {
                                finish()
                            }
                        }.onFailure { error ->
                            showToast(error.message ?: "No se pudo actualizar la rutina", Toast.LENGTH_LONG)
                        }
                    }
                }
            } else {
                firebaseRepository.updateRoutineName(cloudRoutineId, nombre) { result ->
                    runOnUiThread {
                        btnGuardarRutina.isEnabled = true
                        result.onSuccess {
                            showToast("Rutina actualizada")
                            if (!openExerciseSelectorAfterCreate) {
                                finish()
                            }
                        }.onFailure { error ->
                            showToast(error.message ?: "No se pudo actualizar la rutina", Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        }
    }

    // Carga los datos de la rutina desde Firebase
    private fun loadCloudRoutine() {
        if (cloudRoutineId.isBlank()) return

        firebaseRepository.getRoutineById(cloudRoutineId) { result ->
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

    // Carga los ejercicios asociados a la rutina desde Firebase
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
                        onReemplazar = ::onReemplazarCloudEjercicio,
                        onEditarSerie = ::onEditarCloudSerie
                    )
                    recyclerEjercicios.adapter = cloudEjerciciosAdapter
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudieron cargar los ejercicios", Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun onAgregarCloudSerie(routineExerciseId: String) {
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

            firebaseRepository.addSetToExercise(cloudRoutineId, routineExerciseId, peso, reps) { result ->
                runOnUiThread {
                    result.onSuccess {
                        dialog.dismiss()
                        loadCloudEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo agregar la serie", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun onEliminarCloudEjercicio(routineExerciseId: String) {
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
            firebaseRepository.deleteExerciseFromRoutine(cloudRoutineId, routineExerciseId) { result ->
                runOnUiThread {
                    result.onSuccess {
                        loadCloudEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo eliminar el ejercicio", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun onReordenarCloudEjercicio(routineExerciseId: String, moverArriba: Boolean) {
        firebaseRepository.reorderExerciseInRoutine(cloudRoutineId, routineExerciseId, moverArriba) { result ->
            runOnUiThread {
                result.onSuccess {
                    loadCloudEjercicios()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo reordenar el ejercicio", Toast.LENGTH_LONG)
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

    private fun onEditarCloudSerie(routineId: String, routineExerciseId: String, set: CloudRoutineSet) {
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
                        loadCloudEjercicios()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo actualizar la serie", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun openExerciseSelector() {
        if (cloudRoutineId.isBlank()) return
        val intent = Intent(this, SeleccionarEjercicioActivity::class.java)
        intent.putExtra("CLOUD_ROUTINE_ID", cloudRoutineId)
        startActivity(intent)
    }
}
