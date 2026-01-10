package com.example.olympus.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.olympus.R
import com.example.olympus.presentation.viewmodels.LoginViewModel
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar ViewModel
        viewModel = LoginViewModel()

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validar los campos vacíos
            if (email.isEmpty() || password.isEmpty()) {
                mostrarError("Debe completar todos los campos")
                return@setOnClickListener
            }

            // Validar email
            if (!viewModel.validarEmail(email)) {
                mostrarError("El formato del correo no es correcto")
                return@setOnClickListener
            }

            // Validar la contraseña
            if (!viewModel.validarPassword(password)) {
                mostrarError("La contraseña debe tener al menos 6 caracteres")
                return@setOnClickListener
            }

            // Validar el login
            if (viewModel.validarLogin(email, password)) {
                mostrarMensaje("Has iniciado sesión")
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                mostrarError("Email o contraseña incorrectos")
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}