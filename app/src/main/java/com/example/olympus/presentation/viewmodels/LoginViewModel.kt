package com.example.olympus.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.olympus.data.repositories.UsuarioRepository

/**
 * ViewModel para gestionar la lógica de login
 */
class LoginViewModel : ViewModel() {

    // Acceso a datos de usuarios
    private val repository = UsuarioRepository()

    // Autentica al usuario con email y contraseña, retorna true si las credenciales son correctas
    fun validarLogin(email: String, password: String): Boolean {
        return repository.login(email, password) != null
    }

    // Valida el formato del email
    fun validarEmail(email: String): Boolean {
        val patronEmail = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return patronEmail.matches(email)
    }

    // Valida la longitud mínima de la contraseña que será de 6
    fun validarPassword(password: String): Boolean {
        return password.length >= 6
    }
}