package com.example.olympus

// Activity para editar un plan nutricional y gestionar sus comidas
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditarPlanNutricionalActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var tvTitulo: TextView
    private lateinit var etNombrePlan: EditText
    private lateinit var etDescripcionPlan: EditText
    private lateinit var btnActualizar: Button
    private lateinit var recyclerComidas: RecyclerView
    private var planId: String = ""
    private var userUid: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_plan_nutricional)

        planId = intent.getStringExtra("PLAN_ID").orEmpty()
        userUid = intent.getStringExtra("USER_UID").orEmpty()
        userName = intent.getStringExtra("USER_NAME") ?: "Usuario"

        tvTitulo = findViewById(R.id.tvTituloEditarPlan)
        etNombrePlan = findViewById(R.id.etNombrePlanEditar)
        etDescripcionPlan = findViewById(R.id.etDescripcionPlanEditar)
        btnActualizar = findViewById(R.id.btnActualizarPlan)
        recyclerComidas = findViewById(R.id.recyclerComidasPlan)

        recyclerComidas.layoutManager = LinearLayoutManager(this)
        tvTitulo.text = "Plan de $userName"

        loadPlan()
        loadComidas()

        btnActualizar.setOnClickListener {
            val nombre = etNombrePlan.text.toString().trim()
            val descripcion = etDescripcionPlan.text.toString().trim()
            if (nombre.isEmpty()) return@setOnClickListener

            firebaseRepository.updatePlan(planId, nombre, descripcion) { result ->
                runOnUiThread {
                    result.onSuccess {
                        showToast("Plan actualizado")
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo actualizar el plan", Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadComidas()
    }

    // Carga los datos del plan nutricional desde Firebase
    private fun loadPlan() {
        firebaseRepository.getPlanById(planId) { result ->
            runOnUiThread {
                result.onSuccess { plan ->
                    etNombrePlan.setText(plan.nombre)
                    etDescripcionPlan.setText(plan.descripcion)
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo cargar el plan", Toast.LENGTH_LONG)
                }
            }
        }
    }

    // Carga las comidas del plan nutricional desde Firebase
    private fun loadComidas() {
        firebaseRepository.getMealsOfPlan(planId) { result ->
            runOnUiThread {
                result.onSuccess { comidas ->
                    recyclerComidas.adapter = ComidasPlanAdapter(
                        comidasAsignadas = comidas,
                        onAgregarComida = { tipo -> abrirSeleccionarComida(tipo) },
                        onEditarComida = { comida -> mostrarDialogoEditarComida(comida) },
                        onEliminarComida = { comidaId -> confirmarEliminarComida(comidaId) }
                    )
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudieron cargar las comidas", Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun abrirSeleccionarComida(tipo: String) {
        firebaseRepository.hasMealType(planId, tipo) { result ->
            runOnUiThread {
                result.onSuccess { exists ->
                    if (exists) {
                        AlertDialog.Builder(this)
                            .setTitle("Comida ya asignada")
                            .setMessage("Ya existe una comida para $tipo en este plan. Elimínala primero o edítala.")
                            .setPositiveButton("Entendido", null)
                            .show()
                    } else {
                        val intent = Intent(this, SeleccionarComidaActivity::class.java)
                        intent.putExtra("PLAN_ID", planId)
                        intent.putExtra("TIPO_COMIDA", tipo)
                        startActivity(intent)
                    }
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo validar el tipo de comida", Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun mostrarDialogoEditarComida(comida: CloudComidaPlan) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_comida, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreComidaEditar)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etDescripcionComidaEditar)
        val etCalorias = dialogView.findViewById<EditText>(R.id.etCaloriasComidaEditar)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarDialog)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarDialog)

        etNombre.setText(comida.nombre)
        etDescripcion.setText(comida.descripcion)
        etCalorias.setText(comida.calorias.toString())

        val dialog = Dialog(this, R.style.Theme_Olympus_Dialog)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnGuardar.setOnClickListener {
            val nuevasCal = etCalorias.text.toString().toIntOrNull() ?: comida.calorias

            firebaseRepository.updateMeal(
                planId = planId,
                mealId = comida.id,
                nombre = comida.nombre,
                descripcion = comida.descripcion,
                calorias = nuevasCal,
                imagenId = comida.imagenId
            ) { result ->
                runOnUiThread {
                    result.onSuccess {
                        loadComidas()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo actualizar la comida", Toast.LENGTH_LONG)
                    }
                }
            }
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmarEliminarComida(comidaId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmar_eliminar, null)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarEliminar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarEliminar)

        val dialog = Dialog(this, R.style.Theme_Olympus_Dialog)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            firebaseRepository.deleteMeal(planId, comidaId) { result ->
                runOnUiThread {
                    result.onSuccess {
                        loadComidas()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo eliminar la comida", Toast.LENGTH_LONG)
                    }
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}
