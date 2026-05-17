package com.example.olympus

// Pantalla de inicio de sesion con email/password
// Permite a los usuarios autenticarse o solicitar ser profesionales

import android.content.Intent
import android.os.Bundle
import android.app.Dialog
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.example.olympus.utils.SessionManager

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
        val btnProfessionalRequest = findViewById<ImageButton>(R.id.btnProfessionalRequest)

        btnLogin.setOnClickListener { loginUser() }
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        tvForgotPassword.setOnClickListener { showResetPasswordDialog() }
        btnProfessionalRequest.setOnClickListener {
            startActivity(Intent(this, RoleRequestActivity::class.java))
        }
    }

    // Autentica al usuario con email y password, guarda sesion y redirige segun rol
    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu email"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contrasena"
            etPassword.requestFocus()
            return
        }

        firebaseRepository.signIn(email, password) { result ->
            runOnUiThread {
                result.onSuccess { profile ->
                    sessionManager.saveUserSession(profile)
                    showToast("Bienvenido, ${profile.name}")
                    redirectToRoleActivity()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo iniciar sesion")
                }
            }
        }
    }

    // Redirige al usuario a la pantalla correspondiente segun su rol
    private fun redirectToRoleActivity() {
        val role = sessionManager.getUserRole()
        val intent = when (role) {
            "Usuario" -> Intent(this, HomeActivity::class.java)
            "Entrenador" -> Intent(this, EntrenadorActivity::class.java)
            "Nutricionista" -> Intent(this, NutricionistaActivity::class.java)
            else -> {
                showToast("Rol no reconocido")
                return
            }
        }
        startActivity(intent)
        finish()
    }

    // Muestra un dialogo para recuperar la contrasena via email
    private fun showResetPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recuperar_contrasena, null)
        val etEmailRecuperar = dialogView.findViewById<TextInputEditText>(R.id.etEmailRecuperar)
        val btnCancelarRecuperar = dialogView.findViewById<AppCompatButton>(R.id.btnCancelarRecuperar)
        val btnEnviarRecuperar = dialogView.findViewById<AppCompatButton>(R.id.btnEnviarRecuperar)

        etEmailRecuperar.setText(etEmail.text?.toString()?.trim().orEmpty())

        val dialog = Dialog(this)
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        btnCancelarRecuperar.setOnClickListener {
            dialog.dismiss()
        }

        btnEnviarRecuperar.setOnClickListener {
            val email = etEmailRecuperar.text.toString().trim()
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailRecuperar.error = "Ingresa un email válido"
                etEmailRecuperar.requestFocus()
                return@setOnClickListener
            }

            firebaseRepository.sendPasswordResetEmail(email) { result ->
                runOnUiThread {
                    result.onSuccess {
                        showToast("Correo de recuperación enviado", Toast.LENGTH_LONG)
                        dialog.dismiss()
                    }.onFailure { error ->
                        showToast(error.message ?: "No se pudo enviar el correo", Toast.LENGTH_LONG)
                    }
                }
            }
        }

        dialog.show()
    }
}
