package com.example.olympus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si ya hay una sesión activa
        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            redirectToRoleActivity()
            return
        }

        setContentView(R.layout.activity_login)

        // Inicializar base de datos
        dbHelper = DatabaseHelper(this)

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Botón de login
        btnLogin.setOnClickListener {
            loginUser()
        }

        // Botón de registro
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validaciones
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

        // Verificar credenciales
        val user = dbHelper.loginUser(email, password)

        if (user != null) {
            // Guardar sesión
            sessionManager.saveUserSession(user)
            
            Toast.makeText(this, "Bienvenido, ${user.name}", Toast.LENGTH_SHORT).show()
            
            // Redirigir según el rol
            redirectToRoleActivity()
        } else {
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
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
        finish() // Para que no pueda volver atrás al login
    }
}
