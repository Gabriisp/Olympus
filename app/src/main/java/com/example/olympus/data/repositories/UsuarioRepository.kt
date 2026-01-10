package com.example.olympus.data.repositories

import com.example.olympus.data.entities.Usuario

class UsuarioRepository {

    // Datos simulados de usuario
    private val usuariosSimulados = listOf(
        Usuario(1, "usuario@gmail.com", "123456", "usuario"),
    )

    fun login(email: String, password: String): Usuario? {
        return usuariosSimulados.find {
            it.email == email && it.password == password
        }
    }
}