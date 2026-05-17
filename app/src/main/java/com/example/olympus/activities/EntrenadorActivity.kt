package com.example.olympus

// Activity principal del rol Entrenador para gestionar clientes y solicitudes
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class EntrenadorActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerUsuarios: RecyclerView
    private lateinit var recyclerRequests: RecyclerView
    private lateinit var tvEmptyRequests: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.getUserRole() != "Entrenador") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_entrenador)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeEntrenador)
        tvWelcome.text = "Bienvenido, ${sessionManager.getUserName()}"

        recyclerUsuarios = findViewById(R.id.recyclerUsuarios)
        recyclerUsuarios.layoutManager = LinearLayoutManager(this)
        recyclerRequests = findViewById(R.id.recyclerServiceRequestsEntrenador)
        recyclerRequests.layoutManager = LinearLayoutManager(this)
        tvEmptyRequests = findViewById(R.id.tvEmptyRequestsEntrenador)

        loadUsuarios()
        loadRequests()

        findViewById<Button>(R.id.btnLogoutEntrenador).setOnClickListener {
            firebaseRepository.signOut()
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnChangePasswordEntrenador).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    // Carga la lista de clientes asignados al entrenador
    private fun loadUsuarios() {
        val trainerUid = sessionManager.getUserUid()
        if (trainerUid.isNullOrBlank()) {
            showToast("Sesión inválida")
            return
        }

        firebaseRepository.getAssignedClientsForTrainer(trainerUid) { result ->
            runOnUiThread {
                result.onSuccess { usuarios ->
                    val adapter = UsuariosAdapter(
                        usuarios = usuarios,
                        actionText = "Ver rutinas"
                    ) { user ->
                        val intent = Intent(this, DetalleUsuarioActivity::class.java)
                        intent.putExtra("USER_UID", user.uid)
                        intent.putExtra("USER_NAME", user.name)
                        startActivity(intent)
                    }
                    recyclerUsuarios.adapter = adapter
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudieron cargar los clientes asignados", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // Carga las solicitudes de servicio pendientes desde Firebase
    private fun loadRequests() {
        val trainerUid = sessionManager.getUserUid().orEmpty()
        firebaseRepository.getPendingServiceRequestsForProfessional(trainerUid) { result ->
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
                    showToast(error.message ?: "No se pudieron cargar las notificaciones", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // Procesa la aceptacion de una solicitud de servicio
    private fun handleAcceptRequest(request: ServiceRequest) {
        firebaseRepository.acceptServiceRequest(request) { result ->
            runOnUiThread {
                result.onSuccess {
                    showToast("Solicitud aceptada y cliente asignado")
                    loadRequests()
                    loadUsuarios()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo aceptar la solicitud", Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun handleRejectRequest(request: ServiceRequest) {
        firebaseRepository.rejectServiceRequest(request.id) { result ->
            runOnUiThread {
                result.onSuccess {
                    showToast("Solicitud rechazada")
                    loadRequests()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo rechazar la solicitud", Toast.LENGTH_LONG)
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
