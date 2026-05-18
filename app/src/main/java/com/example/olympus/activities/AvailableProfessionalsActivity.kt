package com.example.olympus

// Activity para seleccionar y solicitar servicios de profesionales disponibles
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

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

    // Carga la lista de entrenadores y nutricionistas desde Firebase
    private fun loadProfessionals() {
        firebaseRepository.getUsersByRole("Entrenador") { trainerResult ->
            runOnUiThread {
                trainerResult.onSuccess { trainers ->
                    entrenadoresAdapter.updateData(trainers)
                    tvEmptyEntrenadores.visibility =
                        if (trainers.isEmpty()) View.VISIBLE else View.GONE
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudieron cargar los entrenadores", Toast.LENGTH_LONG)
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
                    showToast(error.message ?: "No se pudieron cargar los nutricionistas", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // Muestra el dialogo de confirmacion para enviar solicitud al profesional
    private fun confirmProfessionalSelection(professional: UserProfile) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_confirmar_solicitud)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitulo = dialog.findViewById<TextView>(R.id.tvTituloDialog)
        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeDialog)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarSolicitud)
        val btnEnviar = dialog.findViewById<Button>(R.id.btnEnviarSolicitud)

        tvTitulo.text = "Solicitar ${professional.role.lowercase()}"
        tvMensaje.text = "¿Quieres enviar una solicitud a ${professional.name}?"

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviar.setOnClickListener {
            dialog.dismiss()
            enviarSolicitud(professional)
        }

        dialog.show()
    }

    // Envia la solicitud de servicio al profesional seleccionado
    private fun enviarSolicitud(professional: UserProfile) {
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
                    showToast(if (created) getString(R.string.service_request_sent)
                        else getString(R.string.service_request_pending), Toast.LENGTH_LONG)
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo enviar la solicitud")
                }
            }
        }
    }
}
