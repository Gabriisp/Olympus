package com.example.olympus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RutinasEntrenadorAdapter(
    private val rutinas: List<CloudRoutine>,
    private val onVerDetalles: (String) -> Unit,
    private val onVerEstadisticas: (String) -> Unit,
    private val onEliminar: (String) -> Unit
) : RecyclerView.Adapter<RutinasEntrenadorAdapter.RutinaViewHolder>() {

    inner class RutinaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreRutina: TextView = view.findViewById(R.id.tvNombreRutinaEntrenador)
        val tvNumEjercicios: TextView = view.findViewById(R.id.tvNumEjercicios)
        val btnMenu: ImageButton = view.findViewById(R.id.btnMenuRutinaEntrenador)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rutina_entrenador, parent, false)
        return RutinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = rutinas[position]
        holder.tvNombreRutina.text = rutina.nombre

        holder.tvNumEjercicios.text = "Rutina sincronizada"

        holder.itemView.setOnClickListener {
            onVerDetalles(rutina.id)
        }

        holder.btnMenu.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnMenu)
            popup.inflate(R.menu.menu_rutina_entrenador)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_editar_rutina -> {
                        onVerDetalles(rutina.id)
                        true
                    }
                    R.id.action_ver_estadisticas -> {
                        onVerEstadisticas(rutina.id)
                        true
                    }
                    R.id.action_eliminar_rutina -> {
                        onEliminar(rutina.id)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = rutinas.size
}
