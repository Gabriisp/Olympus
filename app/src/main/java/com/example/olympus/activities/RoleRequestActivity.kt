package com.example.olympus

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RoleRequestActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etDetails: TextInputEditText
    private lateinit var actvRequestedRole: AutoCompleteTextView
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_request)

        etName = findViewById(R.id.etRequestName)
        etEmail = findViewById(R.id.etRequestEmail)
        etPhone = findViewById(R.id.etRequestPhone)
        etDetails = findViewById(R.id.etRequestDetails)
        actvRequestedRole = findViewById(R.id.actvRequestedRole)
        btnSubmit = findViewById(R.id.btnSubmitRoleRequest)

        val roleAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Entrenador", "Nutricionista")
        )
        actvRequestedRole.setAdapter(roleAdapter)

        btnSubmit.setOnClickListener { submitRequest() }
    }

    private fun submitRequest() {
        val fullName = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val requestedRole = actvRequestedRole.text.toString().trim()
        val details = etDetails.text.toString().trim()

        when {
            fullName.isEmpty() -> {
                etName.error = "Ingresa tu nombre completo"
                etName.requestFocus()
                return
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Ingresa un email válido"
                etEmail.requestFocus()
                return
            }
            phone.isEmpty() -> {
                etPhone.error = "Ingresa tu teléfono"
                etPhone.requestFocus()
                return
            }
            requestedRole !in listOf("Entrenador", "Nutricionista") -> {
                Toast.makeText(this, "Selecciona el rol solicitado", Toast.LENGTH_SHORT).show()
                return
            }
            details.isEmpty() -> {
                etDetails.error = "Añade algo de información para validar la solicitud"
                etDetails.requestFocus()
                return
            }
        }

        btnSubmit.isEnabled = false
        firebaseRepository.submitRoleRequest(
            userUid = firebaseRepository.getCurrentUserUid(),
            fullName = fullName,
            email = email,
            phone = phone,
            requestedRoles = listOf(requestedRole),
            details = details
        ) { result ->
            runOnUiThread {
                btnSubmit.isEnabled = true
                result.onSuccess {
                    Toast.makeText(
                        this,
                        "Solicitud enviada correctamente. El administrador la recibira por correo.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
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
}
