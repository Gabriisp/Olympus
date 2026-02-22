package com.example.olympus

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "OlympusSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_ROLE = "userRole"
    }

    // Guardar sesión de usuario
    fun saveUserSession(user: User) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.apply()
    }

    // Obtener el rol del usuario actual
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    // Verificar si el usuario está logueado
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Obtener nombre del usuario
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    // Obtener ID del usuario
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    // Cerrar sesión
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
