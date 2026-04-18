package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DetalleUsuarioActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvNombreUsuario: TextView
    private lateinit var btnCrearRutina: Button
    private lateinit var recyclerRutinasUsuario: RecyclerView
    private var userUid: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_usuario)

        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvNombreUsuario = findViewById(R.id.tvNombreUsuarioDetalle)
        btnCrearRutina = findViewById(R.id.btnCrearRutinaEntrenador)
        recyclerRutinasUsuario = findViewById(R.id.recyclerRutinasUsuario)

        tvNombreUsuario.text = "Rutinas de $userName"

        recyclerRutinasUsuario.layoutManager = LinearLayoutManager(this)

        btnCrearRutina.setOnClickListener {
            val intent = Intent(this, CrearRutinaEntrenadorActivity::class.java)
            intent.putExtra("USER_UID", userUid)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        loadRutinasUsuario()
    }

    override fun onResume() {
        super.onResume()
        loadRutinasUsuario()
    }

    private fun loadRutinasUsuario() {
        if (userUid.isBlank()) {
            recyclerRutinasUsuario.adapter = RutinasEntrenadorAdapter(
                emptyList(),
                onVerDetalles = {},
                onVerEstadisticas = {},
                onEliminar = {}
            )
            return
        }

        firebaseRepository.getRoutinesByUser(userUid) { result ->
            runOnUiThread {
                result.onSuccess { rutinas ->
                    val adapter = RutinasEntrenadorAdapter(
                        rutinas = rutinas,
                        onVerDetalles = { routineId ->
                            val intent = Intent(this, EditarRutinaEntrenadorActivity::class.java)
                            intent.putExtra("ROUTINE_ID", routineId)
                            intent.putExtra("USER_UID", userUid)
                            intent.putExtra("USER_NAME", userName)
                            startActivity(intent)
                        },
                        onVerEstadisticas = { routineId ->
                            val intent = Intent(this, EstadisticasRutinaActivity::class.java)
                            intent.putExtra("ROUTINE_ID", routineId)
                            startActivity(intent)
                        },
                        onEliminar = { routineId ->
                            firebaseRepository.deleteRoutine(routineId) { deleteResult ->
                                runOnUiThread {
                                    deleteResult.onSuccess {
                                        loadRutinasUsuario()
                                    }.onFailure { error ->
                                        android.widget.Toast.makeText(
                                            this,
                                            error.message ?: "No se pudo eliminar la rutina",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                    recyclerRutinasUsuario.adapter = adapter
                }.onFailure { error ->
                    android.widget.Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar las rutinas del usuario",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
