package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EjerciciosDisponiblesAdapter(
    private val ejercicios: List<Ejercicio>,
    private val onEjercicioClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<EjerciciosDisponiblesAdapter.EjercicioViewHolder>() {

    inner class EjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgEjercicio: ImageView = view.findViewById(R.id.imgEjercicio)
        val tvNombreEjercicio: TextView = view.findViewById(R.id.tvNombreEjercicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_disponible, parent, false)
        return EjercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.tvNombreEjercicio.text = ejercicio.nombre
        holder.imgEjercicio.setImageResource(ejercicio.imagen)

        holder.itemView.setOnClickListener {
            onEjercicioClick(ejercicio)
        }
    }

    override fun getItemCount() = ejercicios.size
}
