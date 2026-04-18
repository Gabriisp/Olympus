package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EjerciciosListAdapter(
    private var ejercicios: List<Ejercicio>,
    private val onEjercicioClick: (Ejercicio) -> Unit
) : RecyclerView.Adapter<EjerciciosListAdapter.EjercicioViewHolder>() {

    inner class EjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgEjercicio: ImageView = view.findViewById(R.id.imgEjercicioList)
        val tvNombreEjercicio: TextView = view.findViewById(R.id.tvNombreEjercicioList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_list, parent, false)
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

    fun updateList(newList: List<Ejercicio>) {
        ejercicios = newList
        notifyDataSetChanged()
    }
}
