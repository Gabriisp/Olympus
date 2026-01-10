package com.example.olympus.utils

object Validator {

    fun esEmailValido(email: String): Boolean {
        val patronEmail = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return patronEmail.matches(email)
    }

    fun esPasswordValido(password: String): Boolean {
        return password.length >= 6
    }

    fun camposNoVacios(vararg campos: String): Boolean {
        return campos.all { it.isNotBlank() }
    }
}