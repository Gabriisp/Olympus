package com.example.olympus

// Activity para cambiar la contrasena del usuario actual
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class ChangePasswordActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository.instance
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmNewPassword: TextInputEditText
    private lateinit var btnSavePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)

        btnSavePassword.setOnClickListener { changePassword() }
    }

    // Valida y actualiza la contrasena del usuario en Firebase
    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmNewPassword.text.toString().trim()

        when {
            currentPassword.isEmpty() -> {
                etCurrentPassword.error = "Ingresa tu contraseña actual"
                etCurrentPassword.requestFocus()
                return
            }
            newPassword.length < 6 -> {
                etNewPassword.error = "La nueva contraseña debe tener al menos 6 caracteres"
                etNewPassword.requestFocus()
                return
            }
            newPassword != confirmPassword -> {
                etConfirmNewPassword.error = "Las contraseñas no coinciden"
                etConfirmNewPassword.requestFocus()
                return
            }
        }

        btnSavePassword.isEnabled = false
        firebaseRepository.updateCurrentUserPassword(currentPassword, newPassword) { result ->
            runOnUiThread {
                btnSavePassword.isEnabled = true
                result.onSuccess {
                    showToast("Contraseña actualizada correctamente", Toast.LENGTH_LONG)
                    finish()
                }.onFailure { error ->
                    showToast(error.message ?: "No se pudo actualizar la contraseña", Toast.LENGTH_LONG)
                }
            }
        }
    }
}
