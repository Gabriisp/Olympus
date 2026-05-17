package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LoginActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var sessionManager: SessionManager
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            redirectToRoleActivity()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val fabProfessionalRequest = findViewById<FloatingActionButton>(R.id.fabProfessionalRequest)

        btnLogin.setOnClickListener { loginUser() }
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        tvForgotPassword.setOnClickListener { showResetPasswordDialog() }
        fabProfessionalRequest.setOnClickListener {
            startActivity(Intent(this, RoleRequestActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            etPassword.requestFocus()
            return
        }

        firebaseRepository.signIn(email, password) { result ->
            runOnUiThread {
                result.onSuccess { profile ->
                    sessionManager.saveUserSession(profile)
                    Toast.makeText(this, "Bienvenido, ${profile.name}", Toast.LENGTH_SHORT).show()
                    redirectToRoleActivity()
                }.onFailure { error ->
                    Toast.makeText(
                        this,
                        error.message ?: "No se pudo iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun redirectToRoleActivity() {
        val role = sessionManager.getUserRole()
        val intent = when (role) {
            "Usuario" -> Intent(this, HomeActivity::class.java)
            "Entrenador" -> Intent(this, EntrenadorActivity::class.java)
            "Nutricionista" -> Intent(this, NutricionistaActivity::class.java)
            else -> {
                Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        finish()
    }

    private fun showResetPasswordDialog() {
        val emailInput = TextInputEditText(this)
        emailInput.setText(etEmail.text?.toString()?.trim().orEmpty())
        emailInput.hint = getString(R.string.email_label)

        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.login_forgot_password))
            .setMessage("Te enviaremos un correo para restablecer tu contraseña.")
            .setView(emailInput)
            .setPositiveButton("Enviar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Ingresa un email válido"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            firebaseRepository.sendPasswordResetEmail(email) { result ->
                runOnUiThread {
                    result.onSuccess {
                        Toast.makeText(
                            this,
                            "Correo de recuperación enviado",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }.onFailure { error ->
                        Toast.makeText(
                            this,
                            error.message ?: "No se pudo enviar el correo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
