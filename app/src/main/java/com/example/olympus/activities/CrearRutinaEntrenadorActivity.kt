package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CrearRutinaEntrenadorActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvTitulo: TextView
    private lateinit var etNombreRutina: EditText
    private lateinit var btnGuardar: Button
    private var userUid: String = ""
    private var userName: String = ""
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_rutina_entrenador)

        sessionManager = SessionManager(this)
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvTitulo = findViewById(R.id.tvTituloCrearRutina)
        etNombreRutina = findViewById(R.id.etNombreRutinaEntrenador)
        btnGuardar = findViewById(R.id.btnGuardarRutinaEntrenador)

        tvTitulo.text = "Nueva Rutina para $userName"

        btnGuardar.setOnClickListener {
            val nombre = etNombreRutina.text.toString().trim()
            
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userUid.isBlank()) {
                Toast.makeText(this, "No se encontró el usuario asignado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            firebaseRepository.createRoutine(
                nombre = nombre,
                userUid = userUid,
                trainerUid = sessionManager.getUserUid(),
                createdByRole = "Entrenador"
            ) { result ->
                runOnUiThread {
                    btnGuardar.isEnabled = true
                    result.onSuccess { routineId ->
                        Toast.makeText(this, "Rutina creada", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, EditarRutinaEntrenadorActivity::class.java)
                        intent.putExtra("ROUTINE_ID", routineId)
                        intent.putExtra("USER_UID", userUid)
                        intent.putExtra("USER_NAME", userName)
                        startActivity(intent)
                        finish()
                    }.onFailure { error ->
                        Toast.makeText(
                            this,
                            error.message ?: "No se pudo crear la rutina",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
