package com.example.olympus

// Pantalla para ver el historial de compras realizadas por el usuario
// Muestra una lista de compras con fecha, articulos y total desde Firebase Firestore

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.olympus.utils.SessionManager

class HistorialActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var tvHistorialVacio: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnVolver: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        initViews()
        setupBotones()
        cargarHistorial()
    }

    // Carga el historial de compras del usuario desde Firebase
    private fun cargarHistorial() {
        val sessionManager = SessionManager(this)
        val userUid = sessionManager.getUserUid()

        if (userUid.isNullOrBlank()) {
            showToast("Error: No se encontro el usuario")
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        rvHistorial.visibility = View.GONE
        tvHistorialVacio.visibility = View.GONE

        FirebaseRepository.instance.getPurchaseHistory(userUid) { result ->
            runOnUiThread {
                progressBar.visibility = View.GONE

                result.onSuccess { compras ->
                    if (compras.isEmpty()) {
                        tvHistorialVacio.visibility = View.VISIBLE
                    } else {
                        tvHistorialVacio.visibility = View.GONE
                        rvHistorial.visibility = View.VISIBLE
                        rvHistorial.layoutManager = LinearLayoutManager(this)
                        rvHistorial.adapter = HistorialAdapter(compras)
                    }
                }.onFailure { error ->
                    showToast("Error al cargar historial: ${error.message}")
                }
            }
        }
    }

    // Inicializa las vistas con sus IDs del layout
    private fun initViews() {
        rvHistorial = findViewById(R.id.rvHistorial)
        tvHistorialVacio = findViewById(R.id.tvHistorialVacio)
        progressBar = findViewById(R.id.progressBar)
        btnVolver = findViewById(R.id.btnVolver)
    }

    // Configura los listeners de los botones
    private fun setupBotones() {
        btnVolver.setOnClickListener { finish() }
    }
}