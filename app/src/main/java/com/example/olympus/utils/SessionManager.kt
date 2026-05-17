package com.example.olympus.utils

// Gestor de sesion de usuario usando SharedPreferences
// Almacena datos del usuario autenticado para acceso rapido en toda la app

import android.content.Context
import android.content.SharedPreferences
import com.example.olympus.UserProfile

class SessionManager(context: Context) {

    // SharedPreferences para persistir datos de sesion
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "OlympusSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_UID = "userUid"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
private const val KEY_USER_ROLE = "userRole"
    }

    // Guarda todos los datos del perfil de usuario en SharedPreferences
    fun saveUserSession(profile: UserProfile) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, profile.legacyLocalId)
        editor.putString(KEY_USER_UID, profile.uid)
        editor.putString(KEY_USER_NAME, profile.name)
        editor.putString(KEY_USER_EMAIL, profile.email)
        editor.putString(KEY_USER_ROLE, profile.role)
        editor.apply()
    }

    // Obtiene el rol del usuario actual
    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    // Obtiene el UID de Firebase del usuario actual
    fun getUserUid(): String? {
        return prefs.getString(KEY_USER_UID, null)
    }

    // Verifica si el usuario esta logueado
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Obtiene el nombre del usuario actual
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    // Obtiene el email del usuario actual
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    // Obtiene el ID local del usuario (legacy, puede ser -1 para usuarios nuevos)
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    // Cierra la sesion limpiando todos los datos guardados
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
