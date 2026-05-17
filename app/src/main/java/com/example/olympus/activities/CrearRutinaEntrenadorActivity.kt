package com.example.olympus

// Activity para que el entrenador cree una rutina para un cliente asignado
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.olympus.utils.SessionManager

class CrearRutinaEntrenadorActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvTitulo: TextView
    private lateinit var etNombreRutina: EditText
    private lateinit var btnGuardar: Button
    private lateinit var llDiasSemana: LinearLayout
    private lateinit var cbLunes: CheckBox
    private lateinit var cbMartes: CheckBox
    private lateinit var cbMiercoles: CheckBox
    private lateinit var cbJueves: CheckBox
    private lateinit var cbViernes: CheckBox
    private lateinit var cbSabado: CheckBox
    private lateinit var cbDomingo: CheckBox
    private var userUid: String = ""
    private var userName: String = ""
    private lateinit var sessionManager: SessionManager
    private var diaSemanaSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_rutina_entrenador)

        sessionManager = SessionManager(this)
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvTitulo = findViewById(R.id.tvTituloCrearRutina)
        etNombreRutina = findViewById(R.id.etNombreRutinaEntrenador)
        btnGuardar = findViewById(R.id.btnGuardarRutinaEntrenador)
        llDiasSemana = findViewById(R.id.llDiasSemana)
        cbLunes = findViewById(R.id.cbLunes)
        cbMartes = findViewById(R.id.cbMartes)
        cbMiercoles = findViewById(R.id.cbMiercoles)
        cbJueves = findViewById(R.id.cbJueves)
        cbViernes = findViewById(R.id.cbViernes)
        cbSabado = findViewById(R.id.cbSabado)
        cbDomingo = findViewById(R.id.cbDomingo)

        tvTitulo.text = "Nueva Rutina para $userName"

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

        btnGuardar.setOnClickListener {
            val nombre = etNombreRutina.text.toString().trim()
            
            if (nombre.isEmpty()) {
                showToast("Ingresa un nombre")
                return@setOnClickListener
            }

            if (userUid.isBlank()) {
                showToast("No se encontró el usuario asignado", Toast.LENGTH_LONG)
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            firebaseRepository.createRoutine(
                nombre = nombre,
                userUid = userUid,
                trainerUid = sessionManager.getUserUid(),
                createdByRole = "Entrenador",
                diaSemana = diaSemanaSeleccionado
            ) { result ->
                runOnUiThread {
                    btnGuardar.isEnabled = true
                    result.onSuccess { routineId ->
                        showToast("Rutina creada")

                        val intent = Intent(this, EditarRutinaEntrenadorActivity::class.java)
                        intent.putExtra("ROUTINE_ID", routineId)
                        intent.putExtra("USER_UID", userUid)
                        intent.putExtra("USER_NAME", userName)
                        startActivity(intent)
                        finish()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo crear la rutina", Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }
}
