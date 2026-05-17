package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RutinaEjerciciosAdapter(
    private var ejercicios: List<RutinaEjercicio>,
    private val onAgregarSerie: (Int) -> Unit,
    private val onEliminarEjercicio: (Int) -> Unit,
    private val onReordenar: (Int, Boolean) -> Unit,
    private val onReemplazar: (Int) -> Unit
) : RecyclerView.Adapter<RutinaEjerciciosAdapter.EjercicioViewHolder>() {

    inner class EjercicioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreEjercicio: TextView = view.findViewById(R.id.tvNombreEjercicio)
        val btnAgregarSerie: Button = view.findViewById(R.id.btnAgregarSerie)
        val btnMenuEjercicio: ImageButton = view.findViewById(R.id.btnMenuEjercicio)
        val recyclerSeries: RecyclerView = view.findViewById(R.id.recyclerSeries)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ejercicio_rutina, parent, false)
        return EjercicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.tvNombreEjercicio.text = ejercicio.ejercicioNombre

        holder.btnAgregarSerie.setOnClickListener {
            onAgregarSerie(ejercicio.id)
        }

        holder.btnMenuEjercicio.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnMenuEjercicio)
            popup.inflate(R.menu.menu_ejercicio_rutina)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_mover_arriba -> {
                        if (position > 0) {
                            onReordenar(ejercicio.id, true)
                        }
                        true
                    }
                    R.id.action_mover_abajo -> {
                        if (position < ejercicios.size - 1) {
                            onReordenar(ejercicio.id, false)
                        }
                        true
                    }
                    R.id.action_reemplazar -> {
                        onReemplazar(ejercicio.id)
                        true
                    }
                    R.id.action_eliminar_ejercicio -> {
                        onEliminarEjercicio(ejercicio.id)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        val dbHelper = RutinasDatabaseHelper(holder.itemView.context)
        val series = dbHelper.getSeriesDeEjercicio(ejercicio.id)
        
        holder.recyclerSeries.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerSeries.adapter = SeriesAdapter(series) { serieId ->
            dbHelper.eliminarSerie(serieId)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = ejercicios.size

    fun updateEjercicios(newList: List<RutinaEjercicio>) {
        ejercicios = newList
        notifyDataSetChanged()
    }
}
