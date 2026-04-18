package com.example.olympus

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AvailableProfessionalsActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerEntrenadores: RecyclerView
    private lateinit var recyclerNutricionistas: RecyclerView
    private lateinit var tvEmptyEntrenadores: TextView
    private lateinit var tvEmptyNutricionistas: TextView
    private lateinit var entrenadoresAdapter: ProfessionalsAdapter
    private lateinit var nutricionistasAdapter: ProfessionalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_available_professionals)
        sessionManager = SessionManager(this)

        recyclerEntrenadores = findViewById(R.id.recyclerEntrenadoresDisponibles)
        recyclerNutricionistas = findViewById(R.id.recyclerNutricionistasDisponibles)
        tvEmptyEntrenadores = findViewById(R.id.tvEmptyEntrenadores)
        tvEmptyNutricionistas = findViewById(R.id.tvEmptyNutricionistas)

        recyclerEntrenadores.layoutManager = LinearLayoutManager(this)
        recyclerNutricionistas.layoutManager = LinearLayoutManager(this)

        entrenadoresAdapter = ProfessionalsAdapter(emptyList(), ::confirmProfessionalSelection)
        nutricionistasAdapter = ProfessionalsAdapter(emptyList(), ::confirmProfessionalSelection)

        recyclerEntrenadores.adapter = entrenadoresAdapter
        recyclerNutricionistas.adapter = nutricionistasAdapter

        loadProfessionals()
    }

    private fun loadProfessionals() {
        firebaseRepository.getUsersByRole("Entrenador") { trainerResult ->
            runOnUiThread {
                trainerResult.onSuccess { trainers ->
                    entrenadoresAdapter.updateData(trainers)
                    tvEmptyEntrenadores.visibility =
                        if (trainers.isEmpty()) View.VISIBLE else View.GONE
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar los entrenadores",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        firebaseRepository.getUsersByRole("Nutricionista") { nutritionistResult ->
            runOnUiThread {
                nutritionistResult.onSuccess { nutritionists ->
                    nutricionistasAdapter.updateData(nutritionists)
                    tvEmptyNutricionistas.visibility =
                        if (nutritionists.isEmpty()) View.VISIBLE else View.GONE
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar los nutricionistas",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun confirmProfessionalSelection(professional: UserProfile) {
        AlertDialog.Builder(this)
            .setTitle("Solicitar ${professional.role.lowercase()}")
            .setMessage("¿Quieres enviar una solicitud a ${professional.name}? Solo le llegará a este profesional.")
            .setPositiveButton("Enviar") { _, _ ->
                firebaseRepository.submitServiceRequestToProfessional(
                    userUid = sessionManager.getUserUid().orEmpty(),
                    userName = sessionManager.getUserName().orEmpty(),
                    userEmail = sessionManager.getUserEmail().orEmpty(),
                    professionalUid = professional.uid,
                    professionalName = professional.name,
                    requestedRole = professional.role
                ) { result ->
                    runOnUiThread {
                        result.onSuccess { created ->
                            Toast.makeText(
                                this,
                                if (created) getString(R.string.service_request_sent)
                                else getString(R.string.service_request_pending),
                                Toast.LENGTH_LONG
                            ).show()
                        }.onFailure { error ->
                            Toast.makeText(
                                this,
                                error.message ?: "No se pudo enviar la solicitud",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
