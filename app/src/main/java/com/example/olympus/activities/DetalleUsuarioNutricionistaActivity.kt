package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalleUsuarioNutricionistaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvNombreUsuario: TextView
    private lateinit var btnCrearPlan: Button
    private lateinit var recyclerPlanes: RecyclerView
    private lateinit var sessionManager: SessionManager
    private var userUid: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_usuario_nutricionista)

        sessionManager = SessionManager(this)
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvNombreUsuario = findViewById(R.id.tvNombreUsuarioNutricionista)
        btnCrearPlan = findViewById(R.id.btnCrearPlanNutricionista)
        recyclerPlanes = findViewById(R.id.recyclerPlanesUsuario)

        tvNombreUsuario.text = "Planes de $userName"
        recyclerPlanes.layoutManager = LinearLayoutManager(this)

        btnCrearPlan.setOnClickListener {
            val intent = Intent(this, CrearPlanNutricionalActivity::class.java)
            intent.putExtra("USER_UID", userUid)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        loadPlanes()
    }

    override fun onResume() {
        super.onResume()
        loadPlanes()
    }

    private fun loadPlanes() {
        if (userUid.isBlank()) return

        firebaseRepository.getPlansByUser(userUid) { result ->
            runOnUiThread {
                result.onSuccess { planes ->
                    recyclerPlanes.adapter = PlanesNutricionalesAdapter(
                        planes = planes,
                        firebaseRepository = firebaseRepository,
                        onVerDetalles = { planId ->
                            val intent = Intent(this, EditarPlanNutricionalActivity::class.java)
                            intent.putExtra("PLAN_ID", planId)
                            intent.putExtra("USER_UID", userUid)
                            intent.putExtra("USER_NAME", userName)
                            startActivity(intent)
                        },
                        onEliminar = { planId ->
                            firebaseRepository.deletePlan(planId) { deleteResult ->
                                runOnUiThread {
                                    deleteResult.onSuccess {
                                        loadPlanes()
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            this,
                                            error.message ?: "No se pudo eliminar el plan",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar los planes",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
