package com.example.olympus

// Extension function para mostrar toasts personalizados con layout custom_toast

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = LayoutInflater.from(this)
    val layout = inflater.inflate(R.layout.custom_toast, null)
    layout.findViewById<TextView>(R.id.tvToastMessage).text = message
    Toast(this).apply {
        this.duration = duration
        view = layout
        show()
    }
}
