package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NutricionistaActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var recyclerRequests: RecyclerView
    private lateinit var tvEmptyRequests: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.getUserRole() != "Nutricionista") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_nutricionista)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeNutricionista)
        tvWelcome.text = "Bienvenido, ${sessionManager.getUserName()}"

        recyclerUsuarios = findViewById(R.id.recyclerUsuariosNutricionista)
        recyclerUsuarios.layoutManager = LinearLayoutManager(this)
        recyclerRequests = findViewById(R.id.recyclerServiceRequestsNutricionista)
        recyclerRequests.layoutManager = LinearLayoutManager(this)
        tvEmptyRequests = findViewById(R.id.tvEmptyRequestsNutricionista)

        loadUsuarios()
        loadRequests()

        findViewById<Button>(R.id.btnLogoutNutricionista).setOnClickListener {
            firebaseRepository.signOut()
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnChangePasswordNutricionista).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    private fun loadUsuarios() {
        val nutritionistUid = sessionManager.getUserUid()
        if (nutritionistUid.isNullOrBlank()) {
            Toast.makeText(this, "Sesión inválida", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseRepository.getAssignedClientsForNutritionist(nutritionistUid) { result ->
            runOnUiThread {
                result.onSuccess { usuarios ->
                    val adapter = UsuariosAdapter(
                        usuarios = usuarios,
                        actionText = "Ver comida"
                    ) { user ->
                        val intent = Intent(this, DetalleUsuarioNutricionistaActivity::class.java)
                        intent.putExtra("USER_UID", user.uid)
                        intent.putExtra("USER_NAME", user.name)
                        startActivity(intent)
                    }
                    recyclerUsuarios.adapter = adapter
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar los clientes asignados",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadRequests() {
        val nutritionistUid = sessionManager.getUserUid().orEmpty()
        firebaseRepository.getPendingServiceRequestsForProfessional(nutritionistUid) { result ->
            runOnUiThread {
                result.onSuccess { requests ->
                    recyclerRequests.adapter = ServiceRequestsAdapter(
                        requests = requests,
                        onAccept = { request -> handleAcceptRequest(request) },
                        onReject = { request -> handleRejectRequest(request) }
                    )
                    tvEmptyRequests.visibility =
                        if (requests.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudieron cargar las notificaciones",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleAcceptRequest(request: ServiceRequest) {
        firebaseRepository.acceptServiceRequest(request) { result ->
            runOnUiThread {
                result.onSuccess {
                    Toast.makeText(
                        this,
                        "Solicitud aceptada y cliente asignado",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadRequests()
                    loadUsuarios()
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudo aceptar la solicitud",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleRejectRequest(request: ServiceRequest) {
        firebaseRepository.rejectServiceRequest(request.id) { result ->
            runOnUiThread {
                result.onSuccess {
                    Toast.makeText(this, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    loadRequests()
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudo rechazar la solicitud",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsuarios()
        loadRequests()
    }
}
