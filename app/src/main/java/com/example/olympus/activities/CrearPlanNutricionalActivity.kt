package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CrearPlanNutricionalActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvTitulo: TextView
    private lateinit var etNombrePlan: EditText
    private lateinit var etDescripcionPlan: EditText
    private lateinit var btnGuardar: Button
    private lateinit var sessionManager: SessionManager
    private var userUid: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_plan_nutricional)

        sessionManager = SessionManager(this)
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvTitulo = findViewById(R.id.tvTituloCrearPlan)
        etNombrePlan = findViewById(R.id.etNombrePlan)
        etDescripcionPlan = findViewById(R.id.etDescripcionPlan)
        btnGuardar = findViewById(R.id.btnGuardarPlan)

        tvTitulo.text = "Nuevo Plan para $userName"

        btnGuardar.setOnClickListener {
            val nombre = etNombrePlan.text.toString().trim()
            val descripcion = etDescripcionPlan.text.toString().trim()
            val nutritionistUid = sessionManager.getUserUid().orEmpty()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingresa un nombre para el plan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firebaseRepository.createPlan(
                nombre = nombre,
                descripcion = descripcion,
                userUid = userUid,
                nutritionistUid = nutritionistUid
            ) { result ->
                runOnUiThread {
                    result.onSuccess { planId ->
                        Toast.makeText(this, "Plan creado", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, EditarPlanNutricionalActivity::class.java)
                        intent.putExtra("PLAN_ID", planId)
                        intent.putExtra("USER_UID", userUid)
                        intent.putExtra("USER_NAME", userName)
                        startActivity(intent)
                        finish()
                    }.onFailure { error ->
                        Toast.makeText(
                            this,
                            error.message ?: "No se pudo crear el plan",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
