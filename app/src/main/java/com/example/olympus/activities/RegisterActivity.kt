package com.example.olympus

// Pantalla de registro de nuevo usuario
// Crea cuenta en Firebase Auth y guarda perfil en Firestore con rol "Usuario" por defecto

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener { registerUser() }
        findViewById<Button>(R.id.btnBackToLogin).setOnClickListener { finish() }
    }

    // Registra un nuevo usuario en Firebase Auth y guarda su perfil
    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val role = "Usuario"

        if (name.isEmpty()) {
            etName.error = "Ingresa tu nombre"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu email"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Ingresa un email valido"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contrasena"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Confirma tu contraseña"
            etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Las contraseñas no coinciden"
            etConfirmPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false

        firebaseRepository.registerAuthUser(email, password) { authResult ->
            runOnUiThread {
                authResult.onSuccess { uid ->
                    val profile = UserProfile(
                        uid = uid,
                        legacyLocalId = -1,
                        name = name,
                        email = email,
                        role = role
                    )

                    firebaseRepository.saveUserProfile(profile) { profileResult ->
                        runOnUiThread {
                            btnRegister.isEnabled = true
                            profileResult.onSuccess {
                                firebaseRepository.signOut()
                                showToast("Registro completado. Ya puedes iniciar sesion.", Toast.LENGTH_LONG)
                                finish()
                            }.onFailure { error ->
                                showToast(error.message ?: "No se pudo guardar el perfil en la nube", Toast.LENGTH_LONG)
                            }
                        }
                    }
                }.onFailure { error ->
                    btnRegister.isEnabled = true
                    showToast(error.message ?: "No se pudo completar el registro", Toast.LENGTH_LONG)
                }
            }
        }
    }
}
